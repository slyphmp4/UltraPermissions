package me.TechsCode.UltraPermissions.base.item;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.item.annotations.XChange;
import me.TechsCode.UltraPermissions.base.item.annotations.XInfo;
import me.TechsCode.UltraPermissions.base.item.annotations.XMerge;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@ApiStatus.Internal
public final class XRegistry<XForm extends XBase<XForm, BukkitForm>, BukkitForm> implements Iterable<XForm> {
   @ApiStatus.Internal
   private static boolean PERFORM_AUTO_ADD = true;
   @ApiStatus.Internal
   private static boolean DISCARD_METADATA = true;
   private static final boolean KEYED_EXISTS;
   private static final Map<Class<? extends XBase<?, ?>>, XRegistry<?, ?>> REGISTRIES;
   private static boolean ensureLoaded;
   private final Map<String, XForm> nameMappings = new HashMap<>(20);
   private final Map<BukkitForm, XForm> bukkitToX = new IdentityHashMap<>(20);
   private Map<XForm, XModuleMetadata> metadata;
   private Map<XForm, Field> backingFields;
   private final Class<BukkitForm> bukkitFormClass;
   private final Class<XForm> xFormClass;
   private final Supplier<Object> registrySupplier;
   private final BiFunction<BukkitForm, String[], XForm> creator;
   private final Function<Integer, XForm[]> createArray;
   private final String registryName;
   private final boolean supportsRegistry;
   private final XRegistry.ClassType bukkitClassType;
   private boolean pulled = false;
   private boolean alreadyDiscardedMetadata = false;

   private static void ensureLoadedRegistries() {
      if (!ensureLoaded) {
         XSound.REGISTRY.getClass();
         ensureLoaded = true;
      }
   }

   @Nullable
   @ApiStatus.Experimental
   public static XRegistry<?, ?> rawRegistryOf(Class<?> var0) {
      ensureLoadedRegistries();
      return REGISTRIES.get(var0);
   }

   @Nullable
   @ApiStatus.Experimental
   public static <XForm extends XBase<XForm, BukkitForm>, BukkitForm> XRegistry<XForm, BukkitForm> registryOf(Class<? extends XForm> var0) {
      ensureLoadedRegistries();
      return (XRegistry<XForm, BukkitForm>)REGISTRIES.get(var0);
   }

   protected static <XForm extends XBase<XForm, BukkitForm>, BukkitForm> void registerModule(XRegistry<XForm, BukkitForm> var0, Class<? extends XForm> var1) {
      REGISTRIES.put(var1, var0);
   }

   @ApiStatus.Internal
   public XRegistry(
      Class<BukkitForm> var1, Class<XForm> var2, Supplier<Object> var3, BiFunction<BukkitForm, String[], XForm> var4, Function<Integer, XForm[]> var5
   ) {
      super();

      boolean var6;
      try {
         var3.get();
         var6 = true;
      } catch (Throwable var8) {
         var6 = false;
      }

      this.bukkitFormClass = Objects.requireNonNull(var1);
      this.xFormClass = Objects.requireNonNull(var2);
      this.registryName = this.bukkitFormClass.getSimpleName();
      this.registrySupplier = var3;
      this.createArray = Objects.requireNonNull(var5);
      this.creator = var4;
      this.supportsRegistry = var6;
      if (var1.isEnum()) {
         this.bukkitClassType = XRegistry.ClassType.ENUM;
      } else if (Modifier.isAbstract(var1.getModifiers())) {
         this.bukkitClassType = XRegistry.ClassType.ABSTRACTION;
      } else {
         this.bukkitClassType = null;
      }

      if (!this.supportsRegistry && this.bukkitClassType == null) {
         throw new IllegalStateException("Bukkit form is not an enum, abstraction or a registry " + var1);
      }

      registerModule(this, var2);
   }

   @ApiStatus.Internal
   public XRegistry(Class<BukkitForm> var1, Class<XForm> var2, Function<Integer, XForm[]> var3) {
      this(var1, var2, null, null, var3);
   }

   @ApiStatus.Internal
   @NotNull
   public Map<String, XForm> nameMapping() {
      return this.nameMappings;
   }

   @ApiStatus.Internal
   @NotNull
   public Map<BukkitForm, XForm> bukkitMapping() {
      return this.bukkitToX;
   }

   public Class<BukkitForm> getBukkitFormClass() {
      return this.bukkitFormClass;
   }

   public Class<XForm> getXFormClass() {
      return this.xFormClass;
   }

   public String getName() {
      return this.registryName;
   }

   private void pullValues() {
      if (!this.pulled) {
         this.pulled = true;
         if (this.creator == null) {
            return;
         }

         this.pullFieldNames();
         if (PERFORM_AUTO_ADD) {
            this.pullSystemValues();
         }
      }
   }

   private static <T> void processEnumLikeFields(Class<T> var0, BiConsumer<Field, T> var1) {
      for (Field var5 : var0.getDeclaredFields()) {
         int var6 = var5.getModifiers();
         if (var5.getType() == var0 && Modifier.isPublic(var6) && Modifier.isStatic(var6) && Modifier.isFinal(var6)) {
            try {
               var1.accept(var5, (T)var5.get(null));
            } catch (IllegalAccessException var8) {
               throw new IllegalStateException("Cannot process enum-like fields of: " + var0, var8);
            }
         }
      }
   }

   @ApiStatus.Internal
   public void registerName(String var1, XForm var2) {
      this.nameMappings.put(normalizeName(var1), (XForm)var2);
   }

   private void pullFieldNames() {
      processEnumLikeFields(this.xFormClass, (var1, var2) -> this.registerMerged(var2, var1));
   }

   private void pullSystemValues() {
      if (this.bukkitClassType == XRegistry.ClassType.ENUM) {
         for (Object var4 : this.bukkitFormClass.getEnumConstants()) {
            this.std(((Enum)var4).name(), (BukkitForm)var4);
         }
      } else {
         processEnumLikeFields(this.bukkitFormClass, (var1, var2) -> {
            if (var2 != null) {
               this.std(var1.getName(), var2);
            }
         });
      }

      if (this.supportsRegistry) {
         for (Keyed var6 : this.bukkitRegistry()) {
            this.std((BukkitForm)var6);
         }
      }
   }

   private BukkitForm valueOf(String var1) {
      var1 = var1.toUpperCase(Locale.ENGLISH).replace('.', '_');
      Class var2 = this.bukkitFormClass;

      try {
         return (BukkitForm)Enum.valueOf(var2, var1);
      } catch (IllegalArgumentException var4) {
         return null;
      }
   }

   private BukkitForm fieldOf(String var1) {
      try {
         return (BukkitForm)this.bukkitFormClass.getDeclaredField(var1).get(null);
      } catch (IllegalAccessException | NoSuchFieldException var3) {
         return null;
      }
   }

   @NotNull
   private Registry<?> bukkitRegistry() {
      return (Registry<?>)this.registrySupplier.get();
   }

   @Nullable
   protected BukkitForm getBukkit(String[] var1) {
      for (String var5 : var1) {
         Object var6;
         if (this.supportsRegistry) {
            var5 = var5.toLowerCase(Locale.ENGLISH);
            NamespacedKey var7;
            if (var5.contains(":")) {
               var7 = NamespacedKey.fromString(var5);
            } else {
               var7 = NamespacedKey.minecraft(var5);
            }

            Keyed var8 = this.bukkitRegistry().get(var7);
            if (var8 != null) {
               var6 = var8;
            } else {
               var6 = null;
            }
         } else if (this.bukkitClassType == XRegistry.ClassType.ENUM) {
            var6 = this.valueOf(var5);
         } else {
            if (this.bukkitClassType != XRegistry.ClassType.ABSTRACTION) {
               throw new AssertionError("None of the class strategies worked for " + this);
            }

            var6 = this.fieldOf(var5);
         }

         if (var6 != null) {
            return (BukkitForm)var6;
         }
      }

      return null;
   }

   @ApiStatus.Internal
   public void discardMetadata() {
      if (DISCARD_METADATA) {
         this.backingFields = null;
         this.metadata = null;
      }
   }

   @NotNull
   public @Unmodifiable Collection<XForm> getValues() {
      this.pullValues();
      return Collections.unmodifiableCollection(this.bukkitToX.values());
   }

   @Deprecated
   public XForm[] values() {
      this.pullValues();
      Collection var1 = this.bukkitToX.values();
      return (XForm[])var1.toArray((XBase[])this.createArray.apply(var1.size()));
   }

   @NotNull
   @Override
   public Iterator<XForm> iterator() {
      return this.getValues().iterator();
   }

   @NotNull
   public XForm getByBukkitForm(BukkitForm var1) {
      Objects.requireNonNull(var1, () -> "Cannot match null " + this.registryName);
      XBase var2 = this.bukkitToX.get(var1);
      if (var2 == null) {
         if (!PERFORM_AUTO_ADD) {
            throw new UnsupportedOperationException("Unknown standard bukkit form (no auto-add) for " + this.registryName + ": " + var1);
         }

         if (this.creator == null) {
            throw new UnsupportedOperationException("Unsupported value for " + this.registryName + ": " + var1);
         }

         XBase var3 = this.std((BukkitForm)var1);
         if (var3 == null) {
            throw new IllegalStateException("Unknown " + this.registryName + ": " + var1);
         }
      }

      return (XForm)var2;
   }

   public Optional<XForm> getByName(@NotNull String var1) {
      Objects.requireNonNull(var1, () -> "Cannot match null " + this.registryName);
      if (var1.isEmpty()) {
         return Optional.empty();
      }

      this.pullValues();
      return Optional.ofNullable(this.nameMappings.get(normalizeName(var1)));
   }

   @ApiStatus.Internal
   @NotNull
   public static String getBukkitName(@NotNull Object var0) {
      Objects.requireNonNull(var0, "Cannot get name of a null bukkit form");
      if (var0 instanceof Enum) {
         return ((Enum)var0).name();
      } else if (KEYED_EXISTS && var0 instanceof Keyed) {
         return ((Keyed)var0).getKey().toString();
      } else if (var0 instanceof PotionEffectType) {
         return ((PotionEffectType)var0).getName();
      } else if (var0 instanceof Enchantment) {
         return ((Enchantment)var0).getName();
      } else {
         throw new AssertionError("Unknown xform type: " + var0 + " (" + var0.getClass() + ")");
      }
   }

   @NotNull
   private static String format(@NotNull String var0) {
      int var1 = var0.length();
      char[] var2 = new char[var1];
      int var3 = 0;
      boolean var4 = false;

      for (int var5 = 0; var5 < var1; var5++) {
         char var6 = var0.charAt(var5);
         if (!var4 && var3 != 0 && (var6 == '-' || var6 == ' ' || var6 == '_') && var2[var3] != '_') {
            var4 = true;
         } else {
            boolean var7 = false;
            if (var6 >= 'A' && var6 <= 'Z' || var6 >= 'a' && var6 <= 'z' || (var7 = var6 >= '0' && var6 <= '9')) {
               if (var4) {
                  var2[var3++] = '_';
                  var4 = false;
               }

               if (var7) {
                  var2[var3++] = var6;
               } else {
                  var2[var3++] = (char)(var6 & 95);
               }
            }
         }
      }

      return new String(var2, 0, var3);
   }

   private static String normalizeName(String var0) {
      var0 = var0.toLowerCase(Locale.ENGLISH);
      if (var0.startsWith("minecraft:")) {
         var0 = var0.substring("minecraft:".length());
      }

      return var0.replace('.', '_');
   }

   private XForm std(BukkitForm var1) {
      return this.std(null, (BukkitForm)var1);
   }

   private XForm std(@Nullable String var1, BukkitForm var2) {
      XBase var3 = this.bukkitToX.get(var2);
      if (var3 != null) {
         return (XForm)var3;
      }

      String var4 = getBukkitName(var2);
      if (this.getBukkit(new String[]{var4}) == null && var1 == null) {
         throw new IllegalArgumentException(
            "Unknown standard bukkit form for " + this.registryName + ": " + var2 + (var2.toString().equals(var4) ? "" : " (" + var4 + ")")
         );
      }

      var3 = this.creator.apply((BukkitForm)var2, var1 == null ? new String[]{var4} : new String[]{var1, var4});
      if (!PERFORM_AUTO_ADD) {
         return (XForm)var3;
      }

      this.registerName(var4, (XForm)var3);
      if (var1 != null) {
         this.registerName(var1, (XForm)var3);
      }

      this.bukkitToX.put((BukkitForm)var2, (XForm)var3);
      return (XForm)var3;
   }

   @ApiStatus.Internal
   public XForm std(String[] var1) {
      Object var2 = this.getBukkit(var1);
      XBase var3 = this.creator.apply((BukkitForm)var2, var1);
      return this.std((XForm)var3);
   }

   @ApiStatus.Internal
   public BukkitForm stdEnum(XForm var1, String[] var2) {
      String var3 = var1.name();
      boolean var4 = false;
      Object var5 = this.getBukkit(new String[]{var3});
      if (var5 == null) {
         var5 = this.getBukkit(var2);
      }

      if (var5 == null) {
         var5 = this.registerMerged((XForm)var1);
         var4 = true;
      }

      return this.stdEnum0((XForm)var1, var2, (BukkitForm)var5, var4);
   }

   public BukkitForm stdEnum(XForm var1, String[] var2, BukkitForm var3) {
      return this.stdEnum0((XForm)var1, var2, (BukkitForm)var3, false);
   }

   @ApiStatus.Internal
   private BukkitForm stdEnum0(XForm var1, String[] var2, BukkitForm var3, boolean var4) {
      String var5 = var1.name();
      if (!var4) {
         this.registerMerged((XForm)var1);
      }

      this.registerName(var5, (XForm)var1);

      for (String var9 : var2) {
         this.registerName(var9, (XForm)var1);
      }

      if (var3 != null) {
         this.bukkitToX.put((BukkitForm)var3, (XForm)var1);
      }

      return (BukkitForm)var3;
   }

   private BukkitForm registerMerged(XForm var1) {
      return this.registerMerged((XForm)var1, this.getBackingField((XForm)var1));
   }

   @NotNull
   @ApiStatus.Internal
   public Field getBackingField(XForm var1) {
      try {
         return var1.getClass().getDeclaredField(var1.name());
      } catch (NoSuchFieldException var6) {
         try {
            if (this.backingFields == null) {
               this.cacheBackingFields();
            }

            Field var3 = this.backingFields.get(var1);
            if (var3 != null) {
               return var3;
            }
         } catch (Throwable var5) {
            IllegalStateException var4 = new IllegalStateException("Cannot find field for XForm: " + var1 + " - " + var1.getClass(), var5);
            var4.addSuppressed(var6);
            throw var4;
         }

         throw new IllegalStateException("Cannot find field for XForm: " + var1 + " - " + var1.getClass(), var6);
      }
   }

   private void cacheBackingFields() {
      if (this.backingFields != null) {
         throw new IllegalStateException("Backing fields are already cached");
      }

      if (this.alreadyDiscardedMetadata) {
         throw new IllegalStateException("Metadata have already been used and discarded");
      }

      this.backingFields = new IdentityHashMap<>();
      this.alreadyDiscardedMetadata = true;

      for (Field var4 : this.xFormClass.getDeclaredFields()) {
         int var5 = var4.getModifiers();
         if (Modifier.isPublic(var5)
            && Modifier.isStatic(var5)
            && Modifier.isFinal(var5)
            && var4.getType() == this.xFormClass
            && !var4.isAnnotationPresent(XRegistry.Ignore.class)) {
            try {
               Object var6 = Objects.requireNonNull(var4.get(null), () -> "XForm backing field returned null: " + var4 + " for registry of " + this.xFormClass);
               XBase var7 = (XBase)var6;
               this.backingFields.put((XForm)var7, var4);
            } catch (IllegalAccessException var8) {
               throw new RuntimeException(var8);
            }
         }
      }
   }

   @ApiStatus.Internal
   public XModuleMetadata getOrRegisterMetadata(XForm var1, Field var2, boolean var3) {
      XModuleMetadata var4 = this.metadata == null ? null : this.metadata.get(var1);
      if (var4 != null) {
         return var4;
      }

      var4 = new XModuleMetadata(
         var2.isAnnotationPresent(Deprecated.class),
         var2.getAnnotationsByType(XChange.class),
         var2.getAnnotationsByType(XMerge.class),
         var2.getAnnotation(XInfo.class)
      );
      if (!var3) {
         if (this.metadata == null) {
            this.metadata = new IdentityHashMap<>(10);
         }

         this.metadata.put((XForm)var1, var4);
      }

      return var4;
   }

   private BukkitForm registerMerged(XForm var1, Field var2) {
      XMerge[] var3 = this.getOrRegisterMetadata((XForm)var1, var2, true).getMerges();
      Object var4 = null;

      for (XMerge var8 : var3) {
         var4 = this.getBukkit(new String[]{var8.name()});
         this.registerName(var8.name(), (XForm)var1);
         if (var4 != null) {
            this.bukkitToX.put((BukkitForm)var4, (XForm)var1);
         }
      }

      return (BukkitForm)var4;
   }

   @ApiStatus.Internal
   public XForm std(Function<BukkitForm, XForm> var1, String[] var2) {
      BukkitForm var3 = this.getBukkit(var2);
      return this.std((XForm)var1.apply(var3));
   }

   @ApiStatus.Internal
   public XForm std(Function<BukkitForm, XForm> var1, XForm var2, String[] var3) {
      BukkitForm var4 = this.getBukkit(var3);
      if (var4 == null) {
         var4 = var2.get();
      }

      return this.std((XForm)var1.apply(var4));
   }

   @ApiStatus.Internal
   public XForm std(XForm var1) {
      for (String var5 : var1.getNames()) {
         this.registerName(var5, (XForm)var1);
      }

      if (var1.isSupported()) {
         this.bukkitToX.put((BukkitForm)var1.get(), (XForm)var1);
      }

      return (XForm)var1;
   }

   @Override
   public String toString() {
      return "XRegistry<"
         + this.registryName
         + ">(nameMappings="
         + this.nameMappings.size()
         + ", bukkitToX="
         + this.bukkitToX.size()
         + ", bukkitFormClass="
         + this.bukkitFormClass.getName()
         + ", xFormClass="
         + this.xFormClass.getName()
         + ", supportsRegistry="
         + this.supportsRegistry
         + ", bukkitFormClassType="
         + this.bukkitClassType
         + ", pulled="
         + this.pulled
         + ", values=["
         + this.bukkitToX.values().stream().limit(10L).map(XBase::name).collect(Collectors.joining(", "))
         + "])";
   }

   static {
      boolean var0 = false;

      try {
         Class.forName("org.bukkit.Keyed");
         var0 = true;
      } catch (ClassNotFoundException var2) {
      }

      KEYED_EXISTS = var0;
      REGISTRIES = new IdentityHashMap<>();
      ensureLoaded = false;
   }

   private enum ClassType {
      ENUM,
      ABSTRACTION;

      ClassType() {
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.FIELD)
   @Documented
   @ApiStatus.Internal
   public @interface Ignore {
   }
}
