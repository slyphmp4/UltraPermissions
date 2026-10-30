package me.TechsCode.UltraPermissions.base.update;

public enum UpdateRequestStatus {
   WAITING_AUTHENTICATION,
   AUTHORIZED,
   UNAUTHORIZED,
   UPDATED,
   UNKNOWN;

   UpdateRequestStatus() {
   }

   public static UpdateRequestStatus fromString(String var0) {
      if (var0.equalsIgnoreCase("waiting_authentication")) {
         return WAITING_AUTHENTICATION;
      } else if (var0.equalsIgnoreCase("authorized")) {
         return AUTHORIZED;
      } else if (var0.equalsIgnoreCase("unauthorized")) {
         return UNAUTHORIZED;
      } else {
         return var0.equalsIgnoreCase("updated") ? UPDATED : UNKNOWN;
      }
   }
}
