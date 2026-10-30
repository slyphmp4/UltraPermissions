package me.TechsCode.UltraPermissions.base.reflection.indexer;

import java.util.Optional;

public class Node {
   private String path;

   public Node(String var1) {
      super();
      this.path = var1;
   }

   public boolean isClass() {
      return this.path.endsWith(".class");
   }

   public String getClassName() {
      return this.path.replace("/", ".").replace(".class", "");
   }

   public String getPath() {
      return this.path;
   }

   public Optional<Class<?>> getAsClass() {
      if (!this.isClass()) {
         return Optional.empty();
      }

      try {
         return Optional.of(Class.forName(this.getClassName()));
      } catch (NoClassDefFoundError | ClassNotFoundException var2) {
         return Optional.empty();
      }
   }
}
