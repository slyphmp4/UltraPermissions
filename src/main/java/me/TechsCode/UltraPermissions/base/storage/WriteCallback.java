package me.TechsCode.UltraPermissions.base.storage;

public interface WriteCallback {
   void onSuccess();

   void onFailure(Exception var1);

   class EmptyWriteCallback implements WriteCallback {
      public EmptyWriteCallback() {
         super();
      }

      @Override
      public void onSuccess() {
      }

      @Override
      public void onFailure(Exception var1) {
      }
   }
}
