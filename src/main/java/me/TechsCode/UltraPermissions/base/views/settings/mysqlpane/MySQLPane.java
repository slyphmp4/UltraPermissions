package me.TechsCode.UltraPermissions.base.views.settings.mysqlpane;

import java.util.Objects;

import me.TechsCode.UltraPermissions.base.SpigotTechPlugin;
import me.TechsCode.UltraPermissions.base.dialog.UserInput;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.legacy.Tools;
import me.TechsCode.UltraPermissions.base.misc.Getter;
import me.TechsCode.UltraPermissions.base.misc.Setter;
import me.TechsCode.UltraPermissions.base.mysql.ConnectionFactory;
import me.TechsCode.UltraPermissions.base.mysql.ConnectionTestResult;
import me.TechsCode.UltraPermissions.base.mysql.MySQLCredentials;
import me.TechsCode.UltraPermissions.base.mysql.MySQLRegistry;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.translations.Words;
import me.TechsCode.UltraPermissions.base.views.RestartView;
import me.TechsCode.UltraPermissions.base.views.settings.SettingsPane;
import me.TechsCode.UltraPermissions.base.views.settings.SettingsView;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.entity.Player;

public class MySQLPane extends SettingsPane {
   private static final Phrase MISSING_CREDENTIALS = Phrase.create("mysqlPane.error.missingCredentials", "No credentials provided.");
   private static final Phrase MISSING_SERVER_NAME = Phrase.create("mysqlPane.error.missingServerName", "No server name provided.");
   private static final Phrase NAME = Phrase.create("mysqlPane.name", "MySQL Database");
   private static final Phrase HOSTNAME = Phrase.create("mysqlPane.hostname", "hostname");
   private static final Phrase PORT = Phrase.create("mysqlPane.port", "port");
   private static final Phrase DATABASE = Phrase.create("mysqlPane.database", "database");
   private static final Phrase USERNAME = Phrase.create("mysqlPane.username", "username");
   private static final Phrase PASSWORD = Phrase.create("mysqlPane.password", "password");
   private static final Phrase CREDENTIALS_TITLE = Phrase.create("mysqlPane.credentials.title", "Credentials");
   private static final Phrase CREDENTIALS_DESC = Phrase.create(
      "mysqlPane.credentials.desc", "These must be the same for **every** Server", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase CREDENTIALS_CLEAR_ACTION = Phrase.create(
      "mysqlPane.credentials.clearAction", "**Click** to **clear** MySQL Credentials", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase CREDENTIALS_ENTER_ACTION = Phrase.create(
      "mysqlPane.credentials.enterAction", "**Click** to **enter** MySQL Credentials", Colors.GRAY, Colors.AQUA, Colors.GOLD
   );
   private static final Phrase CREDENTIALS_CONNECTION_URL = Phrase.create("mysqlPane.credentials.connectionsUrl", "Connection Url:", Colors.GRAY);
   private static final Phrase CREDENTIALS_USERNAME = Phrase.create("mysqlPane.credentials.username", "Username: **%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase CREDENTIALS_PASSWORD = Phrase.create("mysqlPane.credentials.password", "Password: **%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase ENCRYPTION_TITLE = Phrase.create("mysqlPane.encryption.title", "SSL");
   private static final Phrase ENCRYPTION_ACTION_ENABLE = Phrase.create(
      "mysqlPane.encryption.enableAction", "**Click** to **enable** encryption", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase ENCRYPTION_ACTION_DISABLE = Phrase.create(
      "mysqlPane.encryption.disableAction", "**Click** to **disable** encryption", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase ENCRYPTION_SSL = Phrase.create("mysqlPane.encryption.ssl", "SSL: **%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase TEST_TITLE = Phrase.create("mysqlPane.test.title", "Test Changes");
   private static final Phrase TEST_ACTION = Phrase.create("mysqlPane.test.action", "Click to test **changes**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase TEST_HOSTNAME = Phrase.create("mysqlPane.test.hostname", "Hostname: **%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase TEST_PORT = Phrase.create("mysqlPane.test.port", "Port: **%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase TEST_DATABASE = Phrase.create("mysqlPane.test.database", "Database: **%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase TEST_USERNAME = Phrase.create("mysqlPane.test.username", "Username: **%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase TEST_PASSWORD = Phrase.create("mysqlPane.test.password", "Password: **%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase TEST_SERVER_NAME = Phrase.create("mysqlPane.test.serverName", "Server Name: **%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase TEST_SSL = Phrase.create("mysqlPane.test.ssl", "SSL: **%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase TEST_MINIMUM_IDLE = Phrase.create("mysqlPane.test.minimumIdle", "Minimum Idle: **%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase TEST_MAXIMUM_POOL_SIZE = Phrase.create("mysqlPane.test.maximumPoolSize", "Maximum Pool Size: **%**", Colors.GRAY, Colors.WHITE);
   private static final Phrase SAVE_TITLE = Phrase.create("mysqlPane.save.title", "Save");
   private static final Phrase SAVE_ACTION = Phrase.create("mysqlPane.save.action", "Click to **save** changes", Colors.GRAY, Colors.GREEN);
   private static final Phrase LOADING_TITLE = Phrase.create("mysqlPane.loading.title", "Testing Connection");
   private static final Phrase LOADING_DESC = Phrase.create("mysqlPane.loading.desc", "Please wait while the plugin runs all connection tests", Colors.GRAY);
   private static final Phrase COULD_NOT_CONNECT_TITLE = Phrase.create("mysqlPane.couldNotConnect.title", "Could not connect");
   private static final Phrase COULD_NOT_CONNECT_DESC = Phrase.create(
      "mysqlPane.couldNotConnect.desc", "Check if your settings are correct & if the database is accessible", Colors.GRAY
   );
   private static final Phrase COULD_NOT_CONNECT_CONNECTION_ERROR = Phrase.create("mysqlPane.couldNotConnect.connectionError", "Connection Error:", Colors.GRAY);
   private static final Phrase EVERYTHING_SAVED_TITLE = Phrase.create("mysqlPane.everythingSaved.title", "Everything Saved");
   private static final Phrase EVERYTHING_SAVED_DESC = Phrase.create("mysqlPane.everythingSaved.desc", "There are no changes to be saved", Colors.GRAY);
   private static final Phrase CONNECTION_LIMITS_TITLE = Phrase.create("mysqlPane.connectionLimits.title", "Connection Limits");
   private static final Phrase CONNECTION_LIMITS_DESC = Phrase.create(
      "mysqlPane.connectionLimits.desc", "Do not change these limits if you do not know what you are doing", Colors.GRAY
   );
   private static final Phrase CONNECTION_LIMITS_INCREASE_ACTION = Phrase.create(
      "mysqlPane.connectionLimits.increaseAction", "**Left Click** to **increase**", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase CONNECTION_LIMITS_DECREASE_ACTION = Phrase.create(
      "mysqlPane.connectionLimits.decreaseAction", "**Right Click** to **decrease**", Colors.GRAY, Colors.AQUA, Colors.RED
   );
   private static final Phrase CONNECTION_LIMITS_SWITCH_ACTION = Phrase.create(
      "mysqlPane.connectionLimits.switchAction", "**Press Q** to switch option", Colors.GRAY, Colors.AQUA
   );
   private static final Phrase CONNECTION_LIMITS_SETTING_MINIMUM_IDLE_CONNECTIONS = Phrase.create(
      "mysqlPane.connectionLimits.setting.minimumIdleConnections", "Minimum Idle Connections"
   );
   private static final Phrase CONNECTION_LIMITS_SETTING_MAXIMUM_ACTIVE_CONNECTIONS = Phrase.create(
      "mysqlPane.connectionLimits.setting.maximumActiveConnections", "Maximum Active Connections"
   );
   private static final Phrase DISABLE_TITLE = Phrase.create("mysqlPane.disable.title", "Deactivate");
   private static final Phrase DISABLE_ACTION = Phrase.create("mysqlPane.disable.action", "Click to disable MySQL", Colors.GRAY);
   private static final Phrase DISABLE_DESC = Phrase.create("mysqlPane.disable.desc", "You will be asked to restart to apply the changes");
   private static final Phrase SETUP_TITLE = Phrase.create("mysqlPane.setup.title", "Setup MySQL");
   private static final Phrase SETUP_ACTION = Phrase.create("mysqlPane.setup.action", "Click to enable MySQL", Colors.GRAY);
   private static final Phrase SETUP_DESC = Phrase.create("mysqlPane.setup.desc", "This requires an external MySQL Database to connect to", Colors.GRAY);
   private static final Phrase SERVER_NAME_TITLE = Phrase.create("mysqlPane.serverName.title", "Server Name");
   private static final Phrase SERVER_NAME_CLICK = Phrase.create(
      "mysqlPane.serverName.click", "**Click** to **change** the server name", Colors.GRAY, Colors.AQUA, Colors.GOLD
   );
   private static final Phrase SERVER_NAME_SET_SERVER_NAME = Phrase.create("mysqlPane.serverName.setServerName", "Server Name: %setServerName%", Colors.GRAY);
   private static final Phrase SERVER_NAME_DESC = Phrase.create(
      "mysqlPane.serverName.des1c", "Name this server the **exact same** as configured in", Colors.GRAY, Colors.RED
   );
   private static final Phrase SERVER_NAME_DESC2 = Phrase.create(
      "mysqlPane.serverName.desc2", "your **proxy settings** to identify it in your network", Colors.GRAY, Colors.WHITE
   );
   private static final Phrase SERVER_NAME_ACTION_TITLE_MAIN = Phrase.create("mysqlPane.serverName.action.title.main", "Server Name", Colors.AQUA);
   private static final Phrase SERVER_NAME_ACTION_TITLE_SUB = Phrase.create(
      "mysqlPane.serverName.action.title.sub", "Enter the name of this server", Colors.GRAY
   );
   private static final Phrase SERVER_NAME_ACTION_TITLE_ACTIONBAR = Phrase.create(
      "mysqlPane.serverName.action.title.actionbar",
      "The server name **must be identical** to the server name assigned in the **proxy configuration**",
      Colors.GRAY,
      Colors.RED,
      Colors.WHITE
   );
   private final SpigotTechPlugin plugin;
   private final MySQLRegistry registry;
   private MySQLCredentials credentials;
   private boolean ssl;
   private int minimumIdle;
   private int maximumPoolSize;
   private String serverName;
   private boolean intro;
   private boolean loading;
   private int poolConfigurationNum;
   private MySQLPane.SaveState state;
   private String error;

   public MySQLPane(Player var1, SettingsView var2, SpigotTechPlugin var3) {
      super(var1, var2);
      this.plugin = var3;
      this.registry = var3.getMySQLManager().getRegistry();
      this.serverName = this.registry.getServerName();
      this.credentials = this.registry.getCredentials();
      this.ssl = this.registry.hasSSL();
      this.minimumIdle = this.registry.getMinimumIdle();
      this.maximumPoolSize = this.registry.getMaximumPoolSize();
      this.intro = this.credentials == null && this.serverName == null;
      this.state = MySQLPane.SaveState.SAVED;
   }

   @Override
   public String getName() {
      return NAME.get();
   }

   @Override
   public XMaterial getIcon() {
      return XMaterial.ENDER_CHEST;
   }

   @Override
   public void construct(Model var1) {
      if (this.intro) {
         var1.button(23, this::StartSetupButton);
      } else {
         var1.button(20, this::CredentialsButton);
         var1.button(21, this::SSLButton);
         var1.button(22, this::ConnectionLimitsButton);
         var1.button(23, this::ServerNameButton);
         boolean var2 = this.plugin.getMySQLManager().isEnabled();
         var1.button(var2 ? 25 : 26, this.loading ? this::LoadingButton : this::ActionButton);
         if (var2) {
            var1.button(26, this::DisableButton);
         }
      }
   }

   private void StartSetupButton(Button var1) {
      var1.material(XMaterial.EMERALD_BLOCK).name(Animation.wave(SETUP_TITLE.get(), Colors.Gold, Colors.YELLOW)).lore(SETUP_ACTION.get(), "");
      var1.item().appendLore(SETUP_DESC.split(25));
      var1.action(var1x -> this.intro = false);
   }

   private void CredentialsButton(Button var1) {
      var1.material(XMaterial.PAPER)
         .name(Animation.wave(CREDENTIALS_TITLE.get(), Colors.Gold, Colors.YELLOW))
         .lore(this.credentials != null ? CREDENTIALS_CLEAR_ACTION.get() : CREDENTIALS_ENTER_ACTION.get(), "");
      if (this.credentials != null) {
         String var2 = this.credentials.getHostname();
         String var3 = this.credentials.getPort();
         String var4 = this.credentials.getDatabase();
         String var5 = this.credentials.getUsername();
         String var6 = Tools.getSecretPassword(this.credentials.getPassword());
         var1.item()
            .appendLore(
               CREDENTIALS_CONNECTION_URL.get(),
               Colors.WHITE + var2 + ":" + var3 + "/" + var4,
               "",
               CREDENTIALS_USERNAME.get().replace("%", var5),
               CREDENTIALS_PASSWORD.get().replace("%", var6),
               ""
            );
      }

      var1.item().appendLore(CREDENTIALS_DESC.get());
      var1.action(
         var1x -> {
            if (this.credentials != null) {
               this.credentials = null;
               this.state = MySQLPane.SaveState.UNSAVED_CHANGES;
            } else {
               final MySQLCredentials var2x = new MySQLCredentials("", "", "", "", "");
               new MySQLSetup(this.p, this.plugin) {
                  @Override
                  public void onClose() {
                     MySQLPane.this.reopen();
                  }

                  @Override
                  public void onCompletion() {
                     MySQLPane.this.credentials = var2x;
                     MySQLPane.this.state = MySQLPane.SaveState.UNSAVED_CHANGES;
                     MySQLPane.this.reopen();
                  }

                  @Override
                  public SetupStage[] getStages() {
                     return new SetupStage[]{
                        new SetupStage(MySQLPane.HOSTNAME.firstUpper(), "localhost", var2x::setHostname),
                        new SetupStage(MySQLPane.PORT.firstUpper(), "3306", var2x::setPort),
                        new SetupStage(MySQLPane.DATABASE.firstUpper(), "database", var2x::setDatabase),
                        new SetupStage(MySQLPane.USERNAME.firstUpper(), "root", var2x::setUsername),
                        new SetupStage(MySQLPane.PASSWORD.firstUpper(), "yourpassword", var2x::setPassword)
                     };
                  }
               };
            }
         }
      );
   }

   private void SSLButton(Button var1) {
      var1.material(XMaterial.REPEATER)
         .name(Animation.wave(ENCRYPTION_TITLE.get(), Colors.Gold, Colors.YELLOW))
         .lore(
            (this.ssl ? ENCRYPTION_ACTION_DISABLE : ENCRYPTION_ACTION_ENABLE).get(),
            "",
            ENCRYPTION_SSL.get().replace("%", (this.ssl ? Words.ENABLED : Words.DISABLED).get())
         );
      var1.action(var1x -> {
         this.ssl = !this.ssl;
         this.state = MySQLPane.SaveState.UNSAVED_CHANGES;
      });
   }

   private void ConnectionLimitsButton(Button var1) {
      class PoolSetting {
         private final String name;
         private final Getter<Integer> getValue;
         private final Setter<Integer> setValue;

         private PoolSetting(String var2, Getter<Integer> var3, Setter<Integer> var4) {
            super();
            this.name = var2;
            this.getValue = var3;
            this.setValue = var4;
         }
      }

      PoolSetting[] var2 = new PoolSetting[]{
         new PoolSetting(CONNECTION_LIMITS_SETTING_MINIMUM_IDLE_CONNECTIONS.get(), () -> this.minimumIdle, var1x -> this.minimumIdle = var1x),
         new PoolSetting(CONNECTION_LIMITS_SETTING_MAXIMUM_ACTIVE_CONNECTIONS.get(), () -> this.maximumPoolSize, var1x -> this.maximumPoolSize = var1x)
      };
      PoolSetting var3 = var2[this.poolConfigurationNum];
      var1.material(XMaterial.WATER_BUCKET)
         .name(Animation.wave(CONNECTION_LIMITS_TITLE.get(), Colors.Gold, Colors.YELLOW))
         .lore(CONNECTION_LIMITS_INCREASE_ACTION.get(), CONNECTION_LIMITS_DECREASE_ACTION.get(), CONNECTION_LIMITS_SWITCH_ACTION.get(), "");

      for (PoolSetting var7 : var2) {
         if (var7.equals(var3)) {
            var1.item().appendLore(Colors.GREEN + "§l> " + Colors.WHITE + var7.name + ": " + Colors.YELLOW + var7.getValue.get());
         } else {
            var1.item().appendLore(Colors.GRAY + " " + var7.name + ": " + Colors.YELLOW + var7.getValue.get());
         }
      }

      var1.item().appendLore("");
      var1.item().appendLore(CONNECTION_LIMITS_DESC.split(45));
      var1.action(ActionType.LEFT, () -> {
         var3.setValue.set(var3.getValue.get() + 1);
         this.state = MySQLPane.SaveState.UNSAVED_CHANGES;
      });
      var1.action(ActionType.RIGHT, () -> {
         var3.setValue.set(var3.getValue.get() == 0 ? 0 : var3.getValue.get() - 1);
         this.state = MySQLPane.SaveState.UNSAVED_CHANGES;
      });
      var1.action(ActionType.Q, () -> {
         if (this.poolConfigurationNum < 1) {
            this.poolConfigurationNum++;
         } else {
            this.poolConfigurationNum = 0;
         }
      });
   }

   private void ServerNameButton(Button var1) {
      var1.material(XMaterial.NAME_TAG)
         .name(Animation.wave(SERVER_NAME_TITLE.get(), Colors.GOLD, Colors.YELLOW))
         .lore(
            SERVER_NAME_CLICK.get(),
            "",
            SERVER_NAME_SET_SERVER_NAME.get()
               .replace("%setServerName%", this.serverName == null ? Colors.RED + Words.UNDEFINED.get() : Colors.WHITE + this.serverName),
            "",
            SERVER_NAME_DESC.get(),
            SERVER_NAME_DESC2.get()
         );
      var1.action(
         var1x -> new UserInput(
            this.p, this.plugin, SERVER_NAME_ACTION_TITLE_MAIN.get(), SERVER_NAME_ACTION_TITLE_SUB.get(), SERVER_NAME_ACTION_TITLE_ACTIONBAR.get()
         ) {
            @Override
            public void onClose(Player var1) {
               MySQLPane.this.reopen();
            }

            @Override
            public boolean onResult(String var1) {
               MySQLPane.this.serverName = var1;
               MySQLPane.this.state = MySQLPane.SaveState.UNSAVED_CHANGES;
               MySQLPane.this.reopen();
               return true;
            }
         }
      );
   }

   private void EverythingSavedButton(Button var1) {
      var1.material(XMaterial.STONE).name(Animation.wave(EVERYTHING_SAVED_TITLE.get(), Colors.Gray, Colors.WHITE)).lore(EVERYTHING_SAVED_DESC.get());
   }

   private void TestChangesButton(Button var1) {
      String var2 = this.credentials != null ? this.credentials.getHostname() : "-";
      String var3 = this.credentials != null ? this.credentials.getPort() : "-";
      String var4 = this.credentials != null ? this.credentials.getDatabase() : "-";
      String var5 = this.credentials != null ? this.credentials.getUsername() : "-";
      String var6 = this.credentials != null ? Tools.getSecretPassword(this.credentials.getPassword()) : "-";
      String var7 = this.serverName != null ? this.serverName : "-";
      var1.material(XMaterial.EMERALD_ORE)
         .name(Animation.wave(TEST_TITLE.get(), Colors.GREEN, Colors.WHITE))
         .lore(
            TEST_ACTION.get(),
            "",
            TEST_HOSTNAME.get().replace("%", var2),
            TEST_PORT.get().replace("%", var3),
            TEST_DATABASE.get().replace("%", var4),
            TEST_USERNAME.get().replace("%", var5),
            TEST_PASSWORD.get().replace("%", var6),
            "",
            TEST_SERVER_NAME.get().replace("%", var7),
            "",
            TEST_SSL.get().replace("%", (this.ssl ? Words.ENABLED : Words.DISABLED).get()),
            "",
            TEST_MINIMUM_IDLE.get().replace("%", this.minimumIdle + ""),
            TEST_MAXIMUM_POOL_SIZE.get().replace("%", this.maximumPoolSize + "")
         );
      var1.action(var1x -> {
         if (this.loading) {
            return;
         }
         if (this.credentials == null) {
            this.state = MySQLPane.SaveState.INVALID_UNSAVED_CHANGES;
            this.error = MISSING_CREDENTIALS.get();
            return;
         }
         if (this.serverName == null) {
            this.state = MySQLPane.SaveState.INVALID_UNSAVED_CHANGES;
            this.error = MISSING_SERVER_NAME.get();
            return;
         }

         MySQLCredentials original = this.credentials;
         MySQLCredentials snapshot = new MySQLCredentials(
            original.getHostname(), original.getPort(), original.getDatabase(), original.getUsername(), original.getPassword()
         );
         String serverName = this.serverName;
         boolean ssl = this.ssl;
         int minimumIdle = this.minimumIdle;
         int maximumPoolSize = this.maximumPoolSize;
         String failureText = COULD_NOT_CONNECT_TITLE.get();
         this.loading = true;
         this.plugin.getScheduler().runAsync(() -> {
            ConnectionFactory connection = null;
            ConnectionTestResult result;
            try {
               connection = new ConnectionFactory(snapshot, ssl, minimumIdle, maximumPoolSize);
               result = connection.testConnection();
            } catch (RuntimeException failure) {
               result = new ConnectionTestResult(false, failureText);
            } finally {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (RuntimeException failure) {
                     this.plugin.log("Could not close MySQL test connection: " + failure.getMessage());
                  }
               }
            }

            ConnectionTestResult completed = result;
            this.plugin.getScheduler().run(() -> {
               this.loading = false;
               if (this.credentials != original || !sameCredentials(this.credentials, snapshot)
                  || !Objects.equals(this.serverName, serverName) || this.ssl != ssl
                  || this.minimumIdle != minimumIdle || this.maximumPoolSize != maximumPoolSize) {
                  this.state = MySQLPane.SaveState.UNSAVED_CHANGES;
                  return;
               }
               this.state = completed.isValid()
                  ? MySQLPane.SaveState.CORRECT_UNSAVED_CHANGES : MySQLPane.SaveState.INVALID_UNSAVED_CHANGES;
               this.error = completed.isValid() ? null : completed.getError();
            });
         });
      });
   }

   private static boolean sameCredentials(MySQLCredentials current, MySQLCredentials expected) {
      return current != null && expected != null
         && Objects.equals(current.getHostname(), expected.getHostname())
         && Objects.equals(current.getPort(), expected.getPort())
         && Objects.equals(current.getDatabase(), expected.getDatabase())
         && Objects.equals(current.getUsername(), expected.getUsername())
         && Objects.equals(current.getPassword(), expected.getPassword());
   }

   private void SaveChangesButton(Button var1) {
      var1.material(XMaterial.EMERALD_BLOCK).name(Animation.wave(SAVE_TITLE.get(), Colors.GREEN, Colors.WHITE)).lore(SAVE_ACTION.get());
      var1.action(var1x -> {
         this.registry.setCredentials(this.credentials);
         this.registry.setSSL(this.ssl);
         this.registry.setMinimumIdle(this.minimumIdle);
         this.registry.setMaximumPoolSize(this.maximumPoolSize);
         this.registry.setServerName(this.serverName);
         this.state = MySQLPane.SaveState.SAVED;
      });
   }

   private void CouldNotConnectButton(Button var1) {
      var1.material(XMaterial.REDSTONE_BLOCK)
         .name(Animation.wave(COULD_NOT_CONNECT_TITLE.get(), Colors.RED, Colors.WHITE))
         .lore(COULD_NOT_CONNECT_DESC.get(), "", COULD_NOT_CONNECT_CONNECTION_ERROR.get());

      for (String var5 : Tools.lineSplitter(this.error, 60)) {
         var1.item().appendLore(Colors.WHITE + var5);
      }
   }

   private void ActionButton(Button var1) {
      switch (this.state) {
         case SAVED:
            this.EverythingSavedButton(var1);
            break;
         case UNSAVED_CHANGES:
            this.TestChangesButton(var1);
            break;
         case CORRECT_UNSAVED_CHANGES:
            this.SaveChangesButton(var1);
            break;
         case INVALID_UNSAVED_CHANGES:
            this.CouldNotConnectButton(var1);
      }
   }

   private void LoadingButton(Button var1) {
      var1.material(XMaterial.EXPERIENCE_BOTTLE).name(Animation.fading(LOADING_TITLE.get(), true, 3, 6, Colors.YELLOW, Colors.WHITE)).lore(LOADING_DESC.get());
   }

   private void DisableButton(Button var1) {
      var1.material(XMaterial.RED_STAINED_GLASS).name(Animation.wave(DISABLE_TITLE.get(), Colors.RED, Colors.WHITE)).lore(DISABLE_ACTION.get(), "");
      var1.item().appendLore(DISABLE_DESC.get());
      var1.action(var1x -> {
         this.credentials = null;
         this.registry.clearCredentials();
         this.state = MySQLPane.SaveState.SAVED;
         new RestartView(this.p, this.plugin);
      });
   }

   public enum SaveState {
      SAVED,
      UNSAVED_CHANGES,
      CORRECT_UNSAVED_CHANGES,
      INVALID_UNSAVED_CHANGES;

      SaveState() {
      }
   }
}
