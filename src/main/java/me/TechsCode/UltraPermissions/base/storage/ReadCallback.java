package me.TechsCode.UltraPermissions.base.storage;

import java.util.HashMap;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public interface ReadCallback {
   void onSuccess(HashMap<String, JsonObject> var1);

   void onFailure(Exception var1);
}
