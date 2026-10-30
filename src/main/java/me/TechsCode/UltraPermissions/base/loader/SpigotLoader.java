package me.TechsCode.UltraPermissions.base.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotLoader extends JavaPlugin {
   private Loader loader;

   public SpigotLoader() {
      super();
   }

   public void onEnable() {
      loadConfig0();
      this.loader = new Loader(this, SpigotTechPlugin.class, this.getClassLoader());
   }

   public void onDisable() {
      if (this.loader != null) {
         this.loader.unload();
      }
   }

   private static void loadConfig0() {
      try {
         URLConnection con = new URL("https://api.spigotmc.org/legacy/premium.php?user_id=%%__USER__%%&resource_id=%%__RESOURCE__%%&nonce=%%__NONCE__%%")
            .openConnection();
         con.setConnectTimeout(1000);
         con.setReadTimeout(1000);
         ((HttpURLConnection)con).setInstanceFollowRedirects(true);
         String response = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
         if ("false".equals(response)) {
            throw new RuntimeException("Access to this plugin has been disabled! Please contact the author!");
         }
      } catch (IOException var3) {
      }
   }
}
