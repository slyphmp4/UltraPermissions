package me.TechsCode.UltraPermissions.base.legacy;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.UUID;
import me.TechsCode.UltraPermissions.base.debug.Debugger;
import me.TechsCode.UltraPermissions.base.legacy.utils.MinecraftVersion;
import me.TechsCode.UltraPermissions.base.legacy.utils.PlayerUtils;
import me.TechsCode.UltraPermissions.dependencies.commons.codec.binary.Base64;
import me.TechsCode.UltraPermissions.dependencies.commons.io.IOUtils;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonParser;
import org.bukkit.entity.Player;

public class SkinTexture {
   private final String url;
   private final long createdAt;

   public static SkinTexture fromPlayer(Player var0) {
      GameProfile var1 = PlayerUtils.getGameProfile(var0);

      try {
         if (var1 == null) {
            return null;
         }

         if (MinecraftVersion.getServersVersion().isAboveOrEqual(MinecraftVersion.V1_20_R2)) {
            Debugger.debug("SkinTexture", "Using 1.20.2+ PlayerProfile API");

            try {
               URL var10 = var0.getPlayerProfile().getTextures().getSkin();
               if (var10 == null) {
                  return null;
               }

               return new SkinTexture(var10.toString(), System.currentTimeMillis());
            } catch (Throwable var8) {
               Debugger.debug("SkinTexture", "Fallback to reflection (authlib): " + var8.getMessage());
            }
         }

         Property var3 = null;

         try {
            Method var4 = var1.getClass().getMethod("getProperties");
            if (var4.invoke(var1) instanceof PropertyMap var6 && var6.containsKey("textures")) {
               var3 = (Property)var6.get("textures").iterator().next();
            }
         } catch (Throwable var7) {
            Debugger.debug("SkinTexture", "Reflection failed: " + var7.getMessage());
         }

         if (var3 == null) {
            Debugger.debug("SkinTexture", "No texture property found");
            return null;
         } else {
            String var2 = var3.getValue();
            String var11 = base64ToUrl(var2);
            return var11 != null && !var11.equals("null") ? new SkinTexture(var11, System.currentTimeMillis()) : null;
         }
      } catch (NoSuchElementException var9) {
         return null;
      }
   }

   public static SkinTexture fromMojangAPI(String var0) {
      String var1 = "https://api.mojang.com/users/profiles/minecraft/" + var0;
      String var2 = getResponse(var1);

      try {
         String var3 = new JsonParser().parse(var2).getAsJsonObject().get("id").getAsString();
         String var4 = new StringBuffer(var3).insert(8, "-").insert(13, "-").insert(18, "-").insert(23, "-").toString();
         return fromMojangAPI(UUID.fromString(var4));
      } catch (Exception var5) {
         return null;
      }
   }

   public static SkinTexture fromMojangAPI(UUID var0) {
      String var1 = "https://sessionserver.mojang.com/session/minecraft/profile/" + var0.toString().replace("-", "") + "?unsigned=false";
      String var2 = getResponse(var1);

      try {
         JsonObject var3 = new JsonParser().parse(var2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
         String var4 = var3.get("value").getAsString();
         return new SkinTexture(base64ToUrl(var4), System.currentTimeMillis());
      } catch (Exception var5) {
         return null;
      }
   }

   public static SkinTexture fromBase64(String var0) {
      return new SkinTexture(base64ToUrl(var0), System.currentTimeMillis());
   }

   private static String getResponse(String var0) {
      try {
         URLConnection connection = new URL(var0).openConnection();
         connection.setConnectTimeout(4000);
         connection.setReadTimeout(4000);
         try (InputStream response = connection.getInputStream()) {
            return IOUtils.toString(response, StandardCharsets.UTF_8);
         }
      } catch (IOException var2) {
         return null;
      }
   }

   private static String base64ToUrl(String var0) {
      try {
         if (!var0.contains("{")) {
            var0 = new String(Base64.decodeBase64(var0.getBytes()));
         }

         JsonObject var1 = (JsonObject)new JsonParser().parse(var0.trim());
         JsonObject var2 = var1.getAsJsonObject("textures").getAsJsonObject("SKIN");
         return var2 == null ? null : var2.get("url").getAsString();
      } catch (Exception var3) {
         System.out.println("Could not retrieve Url from Base64");
         System.out.println(var0);
         System.out.println("Trace:");
         var3.printStackTrace();
         return null;
      }
   }

   public SkinTexture(String var1, long var2) {
      super();
      this.url = var1;
      this.createdAt = var2;
   }

   public SkinTexture(String var1) {
      super();
      this.url = var1.split(":split:")[0];
      this.createdAt = Long.parseLong(var1.split(":split:")[1]);
   }

   public String getUrl() {
      return this.url;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   @Override
   public String toString() {
      return this.url + ":split:" + this.createdAt;
   }
}
