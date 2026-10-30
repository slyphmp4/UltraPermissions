package me.TechsCode.UltraPermissions.base.command;

public class ExecuteCallback {
   private CommandNode<?, ?> lastNode;
   private ExecuteResult result;

   public ExecuteCallback(CommandNode<?, ?> var1, ExecuteResult var2) {
      super();
      this.lastNode = var1;
      this.result = var2;
   }

   public CommandNode<?, ?> getLastNode() {
      return this.lastNode;
   }

   public ExecuteResult getResult() {
      return this.result;
   }
}
