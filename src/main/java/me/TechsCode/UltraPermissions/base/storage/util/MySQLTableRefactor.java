package me.TechsCode.UltraPermissions.base.storage.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import me.TechsCode.UltraPermissions.base.TechPlugin;

public abstract class MySQLTableRefactor {
   public MySQLTableRefactor(TechPlugin var1) {
      super();
      if (var1.getMySQLManager().isEnabled()) {
         try {
            Connection var2 = var1.getMySQLManager().getConnection();

            for (MySQLTableRefactor.RefactorTable var6 : this.getRefactors()) {
               if (this.tableExist(var2, var6.from)) {
                  if (this.tableExist(var2, var6.to)) {
                     var1.log("Archiving Table " + var6.to);
                     this.tableRename(var2, var6.to, "OLD_" + var6.to);
                  }

                  var1.log("Renaming Table " + var6.from + " to " + var6.to);
                  this.tableRename(var2, var6.from, var6.to);
               }
            }

            var2.close();
         } catch (SQLException var7) {
            var7.printStackTrace();
         }
      }
   }

   private boolean tableExist(Connection var1, String var2) throws SQLException {
      boolean var3 = false;

      try (ResultSet var4 = var1.getMetaData().getTables(null, null, var2, null)) {
         while (var4.next()) {
            String var5 = var4.getString("TABLE_NAME");
            if (var5 != null && var5.equals(var2)) {
               var3 = true;
               break;
            }
         }
      }

      return var3;
   }

   private void tableDelete(Connection var1, String var2) throws SQLException {
      Statement var3 = var1.createStatement();
      var3.executeUpdate("DROP TABLE " + var2 + ";");
      var3.close();
   }

   private void tableRename(Connection var1, String var2, String var3) throws SQLException {
      Statement var4 = var1.createStatement();
      var4.executeUpdate("RENAME TABLE " + var2 + " TO " + var3 + ";");
      var4.close();
   }

   public abstract MySQLTableRefactor.RefactorTable[] getRefactors();

   public class RefactorTable {
      private String from;
      private String to;

      public RefactorTable(String var2, String var3) {
         super();
         this.from = var2;
         this.to = var3;
      }
   }
}
