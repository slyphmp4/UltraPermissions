package me.TechsCode.UltraPermissions.base.storage.implementations;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.regex.Pattern;
import me.TechsCode.UltraPermissions.base.TechPlugin;
import me.TechsCode.UltraPermissions.base.storage.*;
import me.TechsCode.UltraPermissions.base.security.SafeJson;
import me.TechsCode.UltraPermissions.dependencies.commons.codec.binary.Base64;
import me.TechsCode.UltraPermissions.dependencies.commons.codec.binary.Hex;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

public class MySQL extends StorageImplementation {
   private static final Pattern REGEX_PATTERN = Pattern.compile("^\\p{XDigit}+$");
   private String tableName;
   private static final int QUERY_TIMEOUT_SECONDS = 5;
   private static final int MAX_VALUE_BYTES = 4194304;
   private static final int MAX_ROWS = 100000;

   public MySQL(TechPlugin plugin, String model) {
      super(plugin, model, true);
      this.tableName = plugin.getName() + "_" + model;
      table(); // Fixed application identifiers, never interpolated from message data.
      try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS " + table() + " (`key` VARCHAR(64) NOT NULL UNIQUE, `value` MEDIUMBLOB)")) {
         statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
         statement.execute();
      } catch (SQLException failure) {
         throw new IllegalStateException("Cannot initialize MySQL storage " + model, failure);
      }
   }

   private String table() {
      if (tableName == null || !tableName.matches("[A-Za-z0-9_]{1,64}")) throw new IllegalArgumentException("Invalid SQL table identifier");
      return '`' + tableName + '`';
   }

   private Connection connection() throws SQLException {
      Connection connection = plugin.getMySQLManager().getConnection();
      if (connection == null) throw new SQLException("MySQL is unavailable");
      return connection;
   }

   private static void key(String key) {
      if (key == null || key.isEmpty() || key.length() > 64) throw new IllegalArgumentException("Invalid storage key");
   }

   @Override
   public void destroy(String key, WriteCallback callback) {
      key(key);
      write("DELETE FROM " + table() + " WHERE `key`=?", callback, key);
   }

   @Override
   public void update(String key, JsonObject state, WriteCallback callback) {
      key(key);
      write("UPDATE " + table() + " SET `value`=? WHERE `key`=?", callback, jsonToString(state), key);
   }

   @Override
   public void create(String key, JsonObject state, WriteCallback callback) {
      key(key);
      write("INSERT INTO " + table() + " (`key`,`value`) VALUES (?,?) ON DUPLICATE KEY UPDATE `value`=VALUES(`value`)",
            callback, key, jsonToString(state));
   }

   private String jsonToString(JsonObject state) {
      byte[] bytes = state.toString().getBytes(StandardCharsets.UTF_8);
      if (bytes.length > MAX_VALUE_BYTES * 3 / 4) throw new IllegalArgumentException("Storage value exceeds limit");
      return Base64.encodeBase64String(bytes);
   }

   @Override
   public void read(String key, ReadCallback callback) {
      key(key);
      HashMap<String, JsonObject> rows = new HashMap<>();
      boolean all = key.equals("*");
      String sql = "SELECT `key`,`value` FROM " + table() + (all ? "" : " WHERE `key`=?") + " LIMIT " + (MAX_ROWS + 1);
      // Hikari owns the bounded connection wait. Never spin, poll or spawn per-request threads.
      try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
         statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
         if (!all) statement.setString(1, key);
         try (ResultSet result = statement.executeQuery()) {
            while (result.next()) {
               if (rows.size() >= MAX_ROWS) throw new SQLException("Storage row limit exceeded");
               byte[] value = result.getBytes("value");
               if (value == null || value.length > MAX_VALUE_BYTES) throw new SQLException("Storage value limit exceeded");
               String text = new String(value, StandardCharsets.UTF_8);
               if (Base64.isBase64(text)) text = new String(Base64.decodeBase64(text), StandardCharsets.UTF_8);
               if (REGEX_PATTERN.matcher(text).matches()) text = new String(Hex.decodeHex(text.toCharArray()), StandardCharsets.UTF_8);
               rows.put(result.getString("key"), SafeJson.object(text, MAX_VALUE_BYTES));
            }
         }
      } catch (Exception failure) {
         callback.onFailure(failure);
         return;
      }
      callback.onSuccess(rows);
   }

   private void write(String sql, WriteCallback callback, String... values) {
      try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
         statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
         for (int i = 0; i < values.length; i++) statement.setString(i + 1, values[i]);
         statement.executeUpdate();
      } catch (SQLException failure) {
         callback.onFailure(failure);
         return;
      }
      callback.onSuccess();
   }

   private void update(String sql, WriteCallback callback) {
      // Retained binary contract: reject legacy callers that supply executable SQL text.
      callback.onFailure(new SQLException("Raw SQL updates are disabled; use parameterized storage operations"));
   }
}
