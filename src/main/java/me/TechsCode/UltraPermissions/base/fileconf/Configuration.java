package me.TechsCode.UltraPermissions.base.fileconf;

import java.util.Set;

public interface Configuration {
   Object get(String var1);

   int getInt(String var1);

   String getString(String var1);

   boolean getBoolean(String var1);

   Set<String> getKeys(boolean var1);

   boolean contains(String var1);

   void set(String var1, Object var2);

   void save();
}
