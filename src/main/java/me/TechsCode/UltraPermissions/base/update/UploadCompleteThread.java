package me.TechsCode.UltraPermissions.base.update;

import java.io.File;

public abstract class UploadCompleteThread implements Runnable {
   private File file;
   private Thread thread;

   public UploadCompleteThread(File var1) {
      super();
      this.file = var1;
      this.thread = new Thread(this);
      this.thread.start();
   }

   @Override
   public void run() {
      boolean var1 = true;

      for (long var2 = this.file.length(); var1; var2 = this.file.length()) {
         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var5) {
            var5.printStackTrace();
         }

         if (var2 == this.file.length()) {
            var1 = false;
         }
      }

      this.onComplete();
   }

   public abstract void onComplete();
}
