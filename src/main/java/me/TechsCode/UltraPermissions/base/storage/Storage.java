package me.TechsCode.UltraPermissions.base.storage;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntFunction;
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
   private long revision;
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
         throw new StorageStartupException("Cannot initialize storage " + var2, var7);
      }

      this.syncingAgent = this.implementation.hasMultiServerSupport() && var5 ? var1.getSyncingAgent() : new SyncingAgent.EmptyAgent();
      this.observers = new CopyOnWriteArrayList<>();
      this.instances = new java.util.concurrent.ConcurrentHashMap<>();
      this.instancesView = Collections.unmodifiableMap(this.instances);
      try {
         this.implementation.read("*", new ReadCallback() {
            @Override
            public void onSuccess(HashMap<String, JsonObject> var1x) {
               try {
                  Storage.this.createInstances(var1x);
               } catch (RuntimeException invalidState) {
                  throw new StorageStartupException("Invalid persisted state in storage " + var2, invalidState);
               }
            }

            @Override
            public void onFailure(Exception var1x) {
               throw new StorageStartupException("Cannot read configured storage " + var2, var1x);
            }
         });
      } catch (StorageStartupException failure) {
         throw failure;
      } catch (RuntimeException failure) {
         throw new StorageStartupException("Cannot load storage " + var2, failure);
      }
      this.syncingAgent.register(this);
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

   public void syncFromDatabase(String key) {
      if (!me.TechsCode.UltraPermissions.base.security.MessagePolicy.storageKey(key)) return;
      this.syncBudget.submit(this.plugin.getScheduler()::runAsync, key, () -> {
         long beforeRead = this.plugin.writeModelResult(() -> this.revision);
         this.implementation.read(key, new ReadCallback() {
            @Override
            public void onSuccess(HashMap<String, JsonObject> rows) {
               Storage.this.plugin.getScheduler().run(() -> Storage.this.applyRefresh(key, rows, beforeRead));
            }

            @Override
            public void onFailure(Exception failure) {
               Storage.this.plugin.log("Could not update Storage Instances of Storage '" + Storage.this.storable.getName() + "' :");
               Storage.this.plugin.log(failure.getMessage());
            }
         });
      });
   }

   private void applyRefresh(String key, Map<String, JsonObject> rows, long beforeRead) {
      this.plugin.writeModel(() -> {
         if (this.revision != beforeRead) {
            // A write or another refresh overlapped the read. Fetch the latest state before applying it.
            this.syncFromDatabase(key);
            return;
         }

         for (Entry<String, JsonObject> row : rows.entrySet()) {
            STORABLE instance = this.instances.get(row.getKey());
            if (instance == null) {
               this.createInstances(Map.of(row.getKey(), row.getValue()));
            } else if (!instance.getState().equals(row.getValue())) {
               instance.setState(row.getValue(), this.plugin);
               instance.setLastSyncedState(row.getValue());
            }
         }
         if ("*".equals(key)) {
            for (String current : List.copyOf(this.instances.keySet())) {
               if (!rows.containsKey(current)) this.removeRefreshedRow(current);
            }
         } else if (rows.isEmpty()) {
            this.removeRefreshedRow(key);
         }

         this.revision++;
         this.onDataSynchronization();
      });
   }

   private void removeRefreshedRow(String key) {
      STORABLE removed = this.instances.remove(key);
      if (removed != null) {
         removed.setStorage(null);
         this.instancesChanged(StorageChangeType.REMOVED, key, removed);
      }
   }

   public Collection<STORABLE> get() {
      return this.instances.values();
   }

   void update(final Storable var1, JsonObject var2) {
      this.revision++;
      JsonObject lastSynced = var1.getLastSyncedState();
      STORABLE previous = null;
      if (lastSynced != null) {
         Storable copy = ClassInstanceCreator.create(this.storable);
         copy.setState(lastSynced, this.plugin);
         copy.setKey(var1.getKey());
         copy.onMount(this.plugin);
         previous = (STORABLE)copy;
      }
      final STORABLE beforeChange = previous;

      this.implementation.update(var1.getKey(), var2, new WriteCallback() {
         @Override
         public void onSuccess() {
            // Never acknowledge a stale asynchronous write as the newest state.
            if (var1.getState().toString().equals(var2.toString())) {
               var1.setLastSyncedState(var2);
            }
            if (beforeChange != null) {
               Storage.this.onChange(beforeChange, (STORABLE)var1);
            }
            Storage.this.syncingAgent.notifyNewDataChanges(Storage.this, var1.getKey());
         }

         @Override
         public void onFailure(Exception failure) {
            Storage.this.plugin.log("Could not persist '" + var1.getKey() + "' of '" + Storage.this.storable.getName() + "': " + failure.getMessage());
            Storage.this.plugin.log("Changes remain in memory but are NOT saved. Fix storage and retry; do not restart until backed up.");
         }
      });
   }

   void destroy(final Storable var1) {
      this.revision++;
      this.implementation.destroy(var1.getKey(), new WriteCallback() {
         @Override
         public void onSuccess() {
            Storage.this.instances.remove(var1.getKey(), (STORABLE)var1);
            var1.setStorage(null);
            Storage.this.instancesChanged(StorageChangeType.REMOVED, var1.getKey(), null);
            Storage.this.onDestroy((STORABLE)var1);
            Storage.this.syncingAgent.notifyNewDataChanges(Storage.this, var1.getKey());
         }

         @Override
         public void onFailure(Exception failure) {
            Storage.this.plugin.log("Could not delete '" + var1.getKey() + "' of '" + Storage.this.storable.getName() + "': " + failure.getMessage());
            Storage.this.plugin.log("The object remains available in memory; no deletion was acknowledged.");
         }
      });
   }

   public STORABLE create(final STORABLE var1) {
      return this.plugin.writeModelResult(() -> this.createLocked(var1));
   }

   public STORABLE createWithNumericId(IntFunction<STORABLE> factory) {
      return this.plugin.writeModelResult(() -> this.createLocked(factory.apply(this.getNextNumericId())));
   }

   private STORABLE createLocked(final STORABLE var1) {
      this.revision++;
      var1.onMount(this.plugin);
      var1.setStorage(this);
      this.onMount(var1);
      final String key = var1.getKey();
      final JsonObject state = var1.getState();
      this.implementation.create(key, state, new WriteCallback() {
         @Override
         public void onSuccess() {
            var1.setLastSyncedState(state);
            STORABLE previous = Storage.this.instances.put(key, var1);
            if (previous != null && previous != var1) previous.setStorage(null);
            Storage.this.instancesChanged(StorageChangeType.ADDED, key, var1);
            Storage.this.onCreation(var1);
            Storage.this.syncingAgent.notifyNewDataChanges(Storage.this, key);
         }

         @Override
         public void onFailure(Exception failure) {
            var1.setStorage(null);
            Storage.this.plugin.log("Could not create '" + key + "' of '" + Storage.this.storable.getName() + "': " + failure.getMessage());
            throw new IllegalStateException("Storage creation failed for " + key, failure);
         }
      });
      return var1;
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
