package me.TechsCode.UltraPermissions.base.storage;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.reflection.ClassInstanceCreator;
import me.TechsCode.UltraPermissions.base.storage.syncing.SyncingAgent;
import me.TechsCode.UltraPermissions.dependencies.commons.lang3.function.TriConsumer;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Storage<STORABLE extends Storable> {
   protected TechPlugin<?> plugin;
   private final me.TechsCode.UltraPermissions.base.security.SyncBudget syncBudget = new me.TechsCode.UltraPermissions.base.security.SyncBudget();
   private final Class<STORABLE> storable;
   private StorageImplementation implementation;
   private SyncingAgent syncingAgent;
   protected Map<String, STORABLE> instances;
   protected Map<String, STORABLE> instancesView;
   protected List<TriConsumer<@NotNull StorageChangeType, String, @Nullable STORABLE>> observers;

   public Storage(final TechPlugin<?> var1, String var2, final Class<STORABLE> var3, Class<? extends StorageImplementation> var4, boolean var5) {
      super();
      this.plugin = var1;
      this.storable = var3;

      try {
         this.implementation = (StorageImplementation)var4.getConstructor(TechPlugin.class, String.class).newInstance(var1, var2);
      } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException var7) {
         var7.printStackTrace();
         return;
      }

      this.syncingAgent = this.implementation.hasMultiServerSupport() && var5 ? var1.getSyncingAgent() : new SyncingAgent.EmptyAgent();
      this.syncingAgent.register(this);
      this.observers = new ArrayList<>();
      this.instances = new java.util.concurrent.ConcurrentHashMap<>();
      this.instancesView = Collections.unmodifiableMap(this.instances);
      this.implementation.read("*", new ReadCallback() {
         @Override
         public void onSuccess(HashMap<String, JsonObject> var1x) {
            Storage.this.createInstances(var1x);
         }

         @Override
         public void onFailure(Exception var1x) {
            var1.log("Could not retrieve data from Storage '" + var3.getName() + "' :");
            var1.log(var1x.getMessage());
         }
      });
   }

   protected void instancesChanged(StorageChangeType var1, String var2, @Nullable STORABLE var3) {
      for (TriConsumer var5 : this.observers) {
         var5.accept(var1, var2, var3);
      }
   }

   private void createInstances(Map<String, JsonObject> var1) {
      for (Entry var3 : var1.entrySet()) {
         Storable var4 = ClassInstanceCreator.create(this.storable);

         try {
            var4.onMount(this.plugin);
            var4.setKey((String)var3.getKey());
            var4.setState((JsonObject)var3.getValue(), this.plugin);
            var4.setLastSyncedState((JsonObject)var3.getValue());
            var4.setStorage(this);
            this.onMount((STORABLE)var4);
            String var5 = (String)var3.getKey();
            this.instances.put(var5, (STORABLE)var4);
            this.instancesChanged(StorageChangeType.ADDED, var5, (STORABLE)var4);
         } catch (Exception var6) {
            // Never dump storage state (which may include secrets) into diagnostics.
            this.plugin.log("Error with Storage " + this.getModelName() + " with key " + (String)var3.getKey() + ":");
            throw var6;
         }
      }
   }

   public void syncFromDatabase(String var1) {
      if (!me.TechsCode.UltraPermissions.base.security.MessagePolicy.storageKey(var1)) return;
      this.syncBudget.submit(this.plugin.getScheduler()::runAsync, var1,
            () -> this.implementation
               .read(
                  var1,
                  new ReadCallback() {
                     @Override
                     public void onSuccess(HashMap<String, JsonObject> var1x) {
                        if (var1.equalsIgnoreCase("*")) {
                           Map<String, JsonObject> var2 = var1x.entrySet()
                              .stream()
                              .filter(var1xxx -> !Storage.this.instancesView.containsKey(var1xxx.getKey()))
                              .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
                           Storage.this.createInstances(var2);
                           Map<String, JsonObject> var3 = var1x.entrySet()
                              .stream()
                              .filter(var1xxx -> Storage.this.instancesView.containsKey(var1xxx.getKey()))
                              .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

                           for (Entry<String, JsonObject> var5 : var3.entrySet()) {
                              Storable var6 = Storage.this.instancesView.get(var5.getKey());
                              if (!var6.getState().toString().equalsIgnoreCase(((JsonObject)var5.getValue()).toString())) {
                                 var6.setState((JsonObject)var5.getValue(), Storage.this.plugin);
                                 var6.setLastSyncedState((JsonObject)var5.getValue());
                              }
                           }

                           Map<String, STORABLE> var8 = Storage.this.instancesView
                              .entrySet()
                              .stream()
                              .filter(var1xxx -> !var1x.containsKey(var1xxx.getKey()))
                              .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
                           var8.forEach((var2x, var3x) -> {
                              Storage.this.instances.remove(var2x);
                              Storage.this.instancesChanged(StorageChangeType.REMOVED, var1, var3x);
                           });
                        } else if (!var1x.isEmpty() && !Storage.this.instancesView.containsKey(var1)) {
                           Storage.this.createInstances(var1x);
                        } else if (!var1x.isEmpty() && Storage.this.instancesView.containsKey(var1)) {
                           Storage.this.instancesView.get(var1).setState((JsonObject)var1x.get(var1), Storage.this.plugin);
                           Storage.this.instancesView.get(var1).setLastSyncedState((JsonObject)var1x.get(var1));
                        } else if (var1x.isEmpty()) {
                           STORABLE var7 = Storage.this.instances.remove(var1);
                           Storage.this.instancesChanged(StorageChangeType.REMOVED, var1, var7);
                        }

                        Storage.this.onDataSynchronization();
                     }

                     @Override
                     public void onFailure(Exception var1x) {
                        Storage.this.plugin.log("Could not update Storage Instances of Storage '" + Storage.this.storable.getName() + "' :");
                        Storage.this.plugin.log(var1x.getMessage());
                     }
                  }
               )
         );
   }

   public Collection<STORABLE> get() {
      return this.instances.values();
   }

   void update(final Storable var1, JsonObject var2) {
      JsonObject var3 = var1.getLastSyncedState();
      if (var3 != null) {
         Storable var4 = ClassInstanceCreator.create(this.storable);
         var4.setState(var3, this.plugin);
         var4.setKey(var1.getKey());
         var4.onMount(this.plugin);
         this.onChange((STORABLE)var4, (STORABLE)var1);
      }

      this.implementation.update(var1.getKey(), var2, new WriteCallback() {
         @Override
         public void onSuccess() {
            Storage.this.syncingAgent.notifyNewDataChanges(Storage.this, var1.getKey());
         }

         @Override
         public void onFailure(Exception var1x) {
            Storage.this.plugin.log("Could not sync '" + var1.getKey() + "' of '" + Storage.this.storable.getName() + "' :");
            Storage.this.plugin.log(var1x.getMessage() + "");
            Storage.this.plugin.log("Json Data:");
            Storage.this.plugin.log(var1.getState().toString());
            Storage.this.plugin.log("It is strongly advised to restart the Server!");
         }
      });
   }

   void destroy(final Storable var1) {
      this.instances.remove(var1.getKey());
      this.instancesChanged(StorageChangeType.REMOVED, var1.getKey(), null);
      this.onDestroy((STORABLE)var1);
      this.implementation.destroy(var1.getKey(), new WriteCallback() {
         @Override
         public void onSuccess() {
            Storage.this.syncingAgent.notifyNewDataChanges(Storage.this, var1.getKey());
         }

         @Override
         public void onFailure(Exception var1x) {
            Storage.this.plugin.log("Could not destroy '" + var1.getKey() + "' of '" + Storage.this.storable.getName() + "' :");
            Storage.this.plugin.log(var1x.getMessage() + "");
            Storage.this.plugin.log("Json Data:");
            Storage.this.plugin.log(var1.getState().toString());
            Storage.this.plugin.log("It is strongly advised to restart the Server!");
         }
      });
   }

   public STORABLE create(final STORABLE var1) {
      var1.onMount(this.plugin);
      var1.setStorage(this);
      this.onMount((STORABLE)var1);
      final String var2 = var1.getKey();
      this.instances.put(var2, (STORABLE)var1);
      this.instancesChanged(StorageChangeType.ADDED, var2, (STORABLE)var1);
      this.onCreation((STORABLE)var1);
      this.implementation.create(var2, var1.getState(), new WriteCallback() {
         @Override
         public void onSuccess() {
            Storage.this.syncingAgent.notifyNewDataChanges(Storage.this, var2);
         }

         @Override
         public void onFailure(Exception var1x) {
            Storage.this.plugin.log("Could not create '" + var2 + "' of '" + Storage.this.storable.getName() + "' :");
            Storage.this.plugin.log(var1x.getMessage() + "");
            Storage.this.plugin.log("Json Data:");
            Storage.this.plugin.log(var1.getState().toString());
            Storage.this.plugin.log("It is strongly advised to restart the Server!");
         }
      });
      return (STORABLE)var1;
   }

   public int getNextNumericId() {
      return this.instancesView.keySet().stream().mapToInt(Integer::parseInt).max().orElse(0) + 1;
   }

   public Class<STORABLE> getModel() {
      return this.storable;
   }

   public String getModelName() {
      return this.getModel().getName();
   }

   public abstract void onMount(STORABLE var1);

   public abstract void onCreation(STORABLE var1);

   public abstract void onChange(STORABLE var1, STORABLE var2);

   public abstract void onDestroy(STORABLE var1);

   public abstract void onDataSynchronization();
}
