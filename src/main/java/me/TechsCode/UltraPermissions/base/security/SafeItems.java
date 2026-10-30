package me.TechsCode.UltraPermissions.base.security;

import java.io.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import me.TechsCode.UltraPermissions.dependencies.gson.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

/** New writes are bounded JSON. Legacy reads permit data containers and fixed Bukkit aliases only. */
public final class SafeItems {
    public static final String PREFIX = "UPI1:";
    private static final int MAX_BYTES = 262144;
    private static final Set<String> ALIASES = Set.of(
        "ItemStack", "ItemMeta", "Color", "Firework", "PotionEffect", "Vector", "BlockVector", "BoundingBox", "Location", "AttributeModifier", "Pattern",
        "org.bukkit.inventory.ItemStack", "org.bukkit.Color", "org.bukkit.FireworkEffect", "org.bukkit.potion.PotionEffect",
        "org.bukkit.util.Vector", "org.bukkit.util.BlockVector", "org.bukkit.util.BoundingBox", "org.bukkit.Location",
        "org.bukkit.attribute.AttributeModifier", "org.bukkit.block.banner.Pattern");
    private static final Set<String> DATA_CLASSES = Set.of(
        "java.lang.Object", "java.lang.String", "java.lang.Number", "java.lang.Integer", "java.lang.Long", "java.lang.Double",
        "java.lang.Float", "java.lang.Short", "java.lang.Byte", "java.lang.Boolean", "java.lang.Character",
        "java.util.HashMap", "java.util.LinkedHashMap", "java.util.ArrayList", "java.util.LinkedList", "java.util.HashSet", "java.util.LinkedHashSet",
        "java.util.Map$Entry", "java.util.CollSer", "java.util.Collections$EmptyMap", "java.util.Collections$EmptyList", "java.util.Collections$EmptySet",
        "java.util.Collections$SingletonMap", "java.util.Collections$SingletonList", "java.util.Collections$SingletonSet",
        "java.util.Collections$UnmodifiableMap", "java.util.Collections$UnmodifiableCollection", "java.util.Collections$UnmodifiableList",
        "java.util.Collections$UnmodifiableRandomAccessList", "java.util.Collections$UnmodifiableSet", "org.bukkit.util.io.Wrapper",
        "com.google.common.collect.ImmutableMap$SerializedForm", "com.google.common.collect.ImmutableList$SerializedForm",
        "com.google.common.collect.ImmutableSet$SerializedForm", "com.google.common.collect.ImmutableBiMap$SerializedForm");
    private SafeItems() {}

    public static String encode(ItemStack item) {
        try {
            Object data = plain(item, 0);
            byte[] bytes = new Gson().toJson(data).getBytes(StandardCharsets.UTF_8);
            if (bytes.length > MAX_BYTES) throw new IOException("Item exceeds size limit");
            return PREFIX + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException invalid) { throw new IllegalStateException("Cannot encode item safely", invalid); }
    }

    public static ItemStack decode(String text) throws IOException {
        if (text == null || text.length() > MAX_BYTES * 2) throw new IOException("Item exceeds size limit");
        try {
            boolean modern = text.startsWith(PREFIX);
            // Legacy Base64Coder inserted line breaks; accept whitespace only, no other ignored characters.
            String encoded = (modern ? text.substring(PREFIX.length()) : text).replaceAll("[\\r\\n\\t ]", "");
            byte[] bytes = Base64.getDecoder().decode(encoded);
            if (bytes.length > MAX_BYTES) throw new IOException("Item exceeds size limit");
            Object result;
            if (modern) result = fromJson(SafeJson.object(new String(bytes, StandardCharsets.UTF_8), MAX_BYTES), 0);
            else try (LegacyInput in = new LegacyInput(new ByteArrayInputStream(bytes))) { result = in.readObject(); }
            if (!(result instanceof ItemStack)) throw new IOException("Expected an ItemStack");
            return (ItemStack) result;
        } catch (ReflectiveOperationException | RuntimeException invalid) {
            throw new IOException("Invalid/unsupported serialized item", invalid);
        }
    }

    private static Object plain(Object value, int depth) throws IOException {
        if (depth > 24) throw new IOException("Item nesting exceeds limit");
        if (value == null || value instanceof String || value instanceof Boolean || value instanceof Number) return value;
        if (value instanceof ConfigurationSerializable) {
            ConfigurationSerializable serializable = (ConfigurationSerializable) value;
            String alias = ConfigurationSerialization.getAlias(value.getClass().asSubclass(ConfigurationSerializable.class));
            if (!ALIASES.contains(alias)) throw new IOException("Unsupported Bukkit item component");
            Map<String, Object> map = new LinkedHashMap<>(serializable.serialize());
            map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, alias);
            return plain(map, depth + 1);
        }
        if (value instanceof Map<?, ?>) {
            Map<?, ?> source = (Map<?, ?>) value;
            if (source.size() > 8192) throw new IOException("Item map exceeds limit");
            Map<String, Object> target = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                if (!(entry.getKey() instanceof String)) throw new IOException("Expected string key");
                target.put((String) entry.getKey(), plain(entry.getValue(), depth + 1));
            }
            return target;
        }
        if (value instanceof Collection<?>) {
            if (((Collection<?>) value).size() > 8192) throw new IOException("Item list exceeds limit");
            List<Object> target = new ArrayList<>();
            for (Object child : (Collection<?>) value) target.add(plain(child, depth + 1));
            return target;
        }
        if (value.getClass().isArray() && value.getClass().getComponentType().isPrimitive()) {
            int length = Array.getLength(value);
            if (length > 8192) throw new IOException("Item array exceeds limit");
            List<Object> elements = new ArrayList<>();
            for (int i = 0; i < length; i++) elements.add(Array.get(value, i));
            return Map.of("@array", value.getClass().getComponentType().getName(), "data", elements);
        }
        throw new IOException("Unsupported item data type");
    }

    private static Object fromJson(JsonElement value, int depth) throws IOException {
        if (depth > 24) throw new IOException("Item nesting exceeds limit");
        if (value.isJsonNull()) return null;
        if (value.isJsonPrimitive()) {
            JsonPrimitive primitive = value.getAsJsonPrimitive();
            if (primitive.isBoolean()) return primitive.getAsBoolean();
            if (primitive.isString()) return primitive.getAsString();
            BigDecimal number = primitive.getAsBigDecimal();
            try { return number.intValueExact(); } catch (ArithmeticException ignored) {}
            try { return number.longValueExact(); } catch (ArithmeticException ignored) {}
            double result = number.doubleValue();
            if (!Double.isFinite(result)) throw new IOException("Invalid numeric item value");
            return result;
        }
        if (value.isJsonArray()) {
            if (value.getAsJsonArray().size() > 8192) throw new IOException("Item list exceeds limit");
            List<Object> result = new ArrayList<>();
            for (JsonElement child : value.getAsJsonArray()) result.add(fromJson(child, depth + 1));
            return result;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) map.put(entry.getKey(), fromJson(entry.getValue(), depth + 1));
        if (map.containsKey("@array")) return primitiveArray(map);
        return restore(map);
    }

    private static Object primitiveArray(Map<String, Object> map) throws IOException {
        if (map.size() != 2 || !(map.get("data") instanceof List<?>)) throw new IOException("Invalid item array");
        List<?> values = (List<?>) map.get("data");
        String kind = String.valueOf(map.get("@array"));
        if (kind.equals("byte")) { byte[] array = new byte[values.size()]; for (int i=0;i<array.length;i++) array[i] = ((Number)values.get(i)).byteValue(); return array; }
        if (kind.equals("int")) { int[] array = new int[values.size()]; for (int i=0;i<array.length;i++) array[i] = ((Number)values.get(i)).intValue(); return array; }
        if (kind.equals("long")) { long[] array = new long[values.size()]; for (int i=0;i<array.length;i++) array[i] = ((Number)values.get(i)).longValue(); return array; }
        throw new IOException("Unsupported item array type");
    }

    private static Object restore(Map<String, Object> map) throws IOException {
        Object type = map.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY);
        if (type == null) return map;
        if (!(type instanceof String) || !ALIASES.contains(type)) throw new IOException("Unapproved Bukkit item alias");
        if (type.equals("ItemStack") || type.equals("org.bukkit.inventory.ItemStack")) return ItemStack.deserialize(map);
        Class<? extends ConfigurationSerializable> implementation = ConfigurationSerialization.getClassByAlias((String) type);
        if (implementation == null || !implementation.getName().startsWith("org.bukkit.")) throw new IOException("Unsupported Bukkit item alias");
        Object restored = ConfigurationSerialization.deserializeObject(map, implementation);
        if (restored == null) throw new IOException("Invalid Bukkit item component");
        return restored;
    }

    private static Class<?> component(Class<?> type) {
        while (type.isArray()) type = type.getComponentType();
        return type;
    }

    private static final class LegacyInput extends ObjectInputStream {
        LegacyInput(InputStream input) throws IOException {
            super(input); enableResolveObject(true);
            setObjectInputFilter(info -> {
                if (info.depth() > 32 || info.references() > 16384 || info.streamBytes() > MAX_BYTES || info.arrayLength() > 8192) return ObjectInputFilter.Status.REJECTED;
                if (info.serialClass() == null) return ObjectInputFilter.Status.UNDECIDED;
                Class<?> type = component(info.serialClass());
                String name = type.getName();
                // Resolved immutable containers/Bukkit values may be checked again by ObjectInputStream.
                boolean resolved = name.startsWith("com.google.common.collect.RegularImmutable")
                    || name.startsWith("com.google.common.collect.SingletonImmutable") || name.startsWith("java.util.ImmutableCollections$")
                    || ConfigurationSerializable.class.isAssignableFrom(type);
                return type.isPrimitive() || DATA_CLASSES.contains(name) || resolved ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
            });
        }

        @Override protected Class<?> resolveClass(ObjectStreamClass descriptor) throws IOException, ClassNotFoundException {
            Class<?> type = super.resolveClass(descriptor);
            // Do not permit directly serialized Bukkit classes, plugin classes, proxies, gadgets or comparators.
            if (!component(type).isPrimitive() && !DATA_CLASSES.contains(component(type).getName())) throw new InvalidClassException("Unapproved legacy item class");
            return type;
        }

        @Override protected Class<?> resolveProxyClass(String[] interfaces) throws IOException {
            throw new InvalidClassException("Proxy classes are forbidden in item data");
        }

        @Override protected Object resolveObject(Object object) throws IOException {
            if (object != null && object.getClass().getName().equals("org.bukkit.util.io.Wrapper")) {
                try {
                    Field field = object.getClass().getDeclaredField("map"); field.setAccessible(true);
                    Object map = field.get(object);
                    if (!(map instanceof Map<?, ?>)) throw new IOException("Invalid legacy wrapper");
                    @SuppressWarnings("unchecked") Map<String, Object> state = (Map<String, Object>) map;
                    return restore(state);
                } catch (ReflectiveOperationException invalid) { throw new IOException("Unsupported legacy wrapper", invalid); }
            }
            return object;
        }
    }
}
