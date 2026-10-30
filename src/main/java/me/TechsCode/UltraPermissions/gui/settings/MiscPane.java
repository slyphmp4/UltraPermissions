package me.TechsCode.UltraPermissions.gui.settings;

import me.TechsCode.UltraPermissions.DefaultGroupAssignOption;
import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.base.gui.ActionType;
import me.TechsCode.UltraPermissions.base.gui.Button;
import me.TechsCode.UltraPermissions.base.gui.Model;
import me.TechsCode.UltraPermissions.base.item.XMaterial;
import me.TechsCode.UltraPermissions.base.translations.Phrase;
import me.TechsCode.UltraPermissions.base.translations.Words;
import me.TechsCode.UltraPermissions.base.views.settings.SettingsPane;
import me.TechsCode.UltraPermissions.base.views.settings.SettingsView;
import me.TechsCode.UltraPermissions.base.visual.Animation;
import me.TechsCode.UltraPermissions.base.visual.Colors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MiscPane extends SettingsPane {
   private final UltraPermissions plugin;
   private DefaultGroupAssignOption selected;
   private static final Phrase NAME = Phrase.create("miscPane.name", "Miscellaneous");
   private static final Phrase NEGATIVE_PERMISSION_OVERRIDE_TITLE = Phrase.create("miscPane.negativePermissionOverride.title", "Negative Permission Override");
   private static final Phrase NEGATIVE_PERMISSION_OVERRIDE_ENABLE_ACTION = Phrase.create(
      "miscPane.negativePermissionOverride.action.enable", "Click to **enable** negative permission override", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase NEGATIVE_PERMISSION_OVERRIDE_DISABLE_ACTION = Phrase.create(
      "miscPane.negativePermissionOverride.action.disable", "Click to **disable** negative permission override", Colors.GRAY, Colors.RED
   );
   private static final Phrase NEGATIVE_PERMISSION_OVERRIDE_STATE = Phrase.create(
      "miscPane.negativePermissionOverride.lore.state", "State: **%status%**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase NEGATIVE_PERMISSION_OVERRIDE_DESC1 = Phrase.create(
      "miscPane.negativePermissionOverride.lore.desc1", "If enabled, negative permissions will", Colors.GRAY
   );
   private static final Phrase NEGATIVE_PERMISSION_OVERRIDE_DESC2 = Phrase.create(
      "miscPane.negativePermissionOverride.lore.desc2", "override operator rights", Colors.GRAY
   );
   private static final Phrase DEFAULT_PERMS_TITLE = Phrase.create("miscPane.defaultPerms.title", "Default Permissions");
   private static final Phrase DEFAULT_PERMS_ENABLE_ACTION = Phrase.create(
      "miscPane.defaultPerms.enableAction", "Click to **enable** default permissions", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase DEFAULT_PERMS_DISABLE_ACTION = Phrase.create(
      "miscPane.defaultPerms.disableAction", "Click to **disable** default permissions", Colors.GRAY, Colors.RED
   );
   private static final Phrase DEFAULT_PERMS_STATE = Phrase.create("miscPane.defaultPerms.state", "State: **%status%**", Colors.GRAY, Colors.YELLOW);
   private static final Phrase DEFAULT_PERMS_EXPLANATION = Phrase.create(
      "miscPane.defaultPerms.explanation",
      "It is recommended to keep it **disabled** to avoid unwanted side effects and to maintain full control",
      Colors.GRAY,
      Colors.RED
   );
   private static final Phrase DEFAULT_PERMS_DESC = Phrase.create("miscPane.defaultPerms.desc", "Changes take effect on reconnect", Colors.DARK_GRAY);
   private static final Phrase DEFAULT_GROUP_ASSIGN_TITLE = Phrase.create("miscPane.defaultGroupAssign.title", "Default Group Assigning");
   private static final Phrase DEFAULT_GROUP_ASSIGN_TOGGLE_ACTION = Phrase.create(
      "miscPane.defaultGroupAssign.toggleAction", "Click to **toggle**", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase DEFAULT_GROUP_ASSIGN_SAVE_ACTION = Phrase.create(
      "miscPane.defaultGroupAssign.saveAction", "**Press Q** to **save** changes", Colors.GRAY, Colors.AQUA, Colors.GREEN
   );
   private static final Phrase DEFAULT_GROUP_ASSIGN_DESC = Phrase.create(
      "miscPane.defaultGroupAssign.desc", "Be careful, this could be a **destructive action**!", Colors.GRAY, Colors.RED
   );
   private static final Phrase NAME_TAGS_TITLE = Phrase.create("miscPane.nameTags.title", "Name Tags");
   private static final Phrase NAME_TAGS_DISABLE_ACTION = Phrase.create(
      "miscPane.nameTags.disableAction", "Click to **disable** this feature", Colors.GRAY, Colors.RED
   );
   private static final Phrase NAME_TAGS_ENABLE_ACTION = Phrase.create(
      "miscPane.nameTags.enableAction", "Click to **enable** this feature", Colors.GRAY, Colors.YELLOW
   );
   private static final Phrase NAME_TAGS_DISABLED = Phrase.create(
      "miscPane.nameTags.disabled", "The Plugin **NameTagEdit** is required for this feature", Colors.GRAY, Colors.RED
   );
   private static final Phrase NAME_TAGS_ENABLED_INDICATOR = Phrase.create(
      "miscPane.nameTags.enabledIndicator", "Name Tags will be **shown** above player heads", Colors.GRAY, Colors.GREEN
   );
   private static final Phrase NAME_TAGS_DISABLED_INDICATOR = Phrase.create(
      "miscPane.nameTags.disabledIndicator", "Name Tags are currently **not showing**", Colors.GRAY, Colors.RED
   );
   private static final Phrase NAME_TAGS_DESC = Phrase.create(
      "miscPane.nameTags.desc", "Show **Prefix**/**Suffix** in the Name Tag above other players", Colors.GRAY, Colors.YELLOW, Colors.YELLOW
   );

   public MiscPane(Player var1, SettingsView var2, UltraPermissions var3) {
      super(var1, var2);
      this.plugin = var3;
   }

   @Override
   public String getName() {
      return NAME.get();
   }

   @Override
   public XMaterial getIcon() {
      return XMaterial.BOWL;
   }

   @Override
   public void construct(Model var1) {
      var1.button(20, this::DefaultPermissionToggle);
      var1.button(22, this::DefaultGroupAssignToggle);
      var1.button(24, this::NegativePermissionOverride);
      var1.button(26, this::NameTagsButton);
   }

   private void NegativePermissionOverride(Button var1) {
      var1.material(XMaterial.GOLDEN_CHESTPLATE)
         .name(Animation.wave(NEGATIVE_PERMISSION_OVERRIDE_TITLE.get(), Colors.GOLD, Colors.WHITE))
         .lore(
            (this.plugin.isNegativePermsOverrideEnabled() ? NEGATIVE_PERMISSION_OVERRIDE_DISABLE_ACTION : NEGATIVE_PERMISSION_OVERRIDE_ENABLE_ACTION).get(),
            "",
            NEGATIVE_PERMISSION_OVERRIDE_STATE.get().replace("%status%", (this.plugin.isNegativePermsOverrideEnabled() ? Words.ENABLED : Words.DISABLED).get()),
            "",
            NEGATIVE_PERMISSION_OVERRIDE_DESC1.get(),
            NEGATIVE_PERMISSION_OVERRIDE_DESC2.get()
         );
      var1.action(var1x -> this.plugin.setNegativePermsOverrides(!this.plugin.isNegativePermsOverrideEnabled()));
   }

   private void DefaultPermissionToggle(Button var1) {
      var1.material(XMaterial.BEDROCK)
         .name(Animation.wave(DEFAULT_PERMS_TITLE.get(), Colors.GOLD, Colors.WHITE))
         .lore(
            (this.plugin.isDefaultPermissionsEnabled() ? DEFAULT_PERMS_DISABLE_ACTION : DEFAULT_PERMS_ENABLE_ACTION).get(),
            "",
            DEFAULT_PERMS_STATE.get().replace("%status%", (this.plugin.isDefaultPermissionsEnabled() ? Words.ENABLED : Words.DISABLED).get()),
            ""
         );
      var1.item().appendLore(DEFAULT_PERMS_EXPLANATION.split(60));
      var1.item().appendLore("", DEFAULT_PERMS_DESC.get());
      var1.action(var1x -> this.plugin.setDefaultPermissions(!this.plugin.isDefaultPermissionsEnabled()));
   }

   private void DefaultGroupAssignToggle(Button var1) {
      var1.material(XMaterial.PISTON)
         .name(Animation.wave(DEFAULT_GROUP_ASSIGN_TITLE.get(), Colors.GOLD, Colors.WHITE))
         .lore(DEFAULT_GROUP_ASSIGN_TOGGLE_ACTION.get());
      if (this.selected != null) {
         var1.item().appendLore(DEFAULT_GROUP_ASSIGN_SAVE_ACTION.get());
         var1.action(var1x -> {
            if (var1x == ActionType.Q) {
               this.plugin.setDefaultGroupAssignOption(this.selected);
               this.selected = null;
            }
         });
      }

      var1.item().appendLore("");
      DefaultGroupAssignOption var2 = this.plugin.getDefaultGroupAssignOption();

      for (DefaultGroupAssignOption var6 : DefaultGroupAssignOption.values()) {
         boolean var7 = this.selected == null && var2 == var6;
         boolean var8 = this.selected == var6;
         var1.item().appendLore((var7 ? Colors.GREEN : (var8 ? Colors.YELLOW : Colors.WHITE)) + "- " + Colors.GRAY + var6.getDescription());
      }

      var1.item().appendLore("", DEFAULT_GROUP_ASSIGN_DESC.get());
      var1.action(var2x -> {
         if (var2x != ActionType.Q) {
            this.selected = (this.selected != null ? this.selected : var2).next();
         }
      });
   }

   private void NameTagsButton(Button var1) {
      boolean var2 = Bukkit.getPluginManager().isPluginEnabled("NametagEdit");
      boolean var3 = this.plugin.getVisualRegistry().get().isEditingNametags();
      var1.material(XMaterial.NAME_TAG)
         .name(Animation.wave(NAME_TAGS_TITLE.get(), Colors.GOLD, Colors.WHITE))
         .lore(
            (var2 ? (var3 ? NAME_TAGS_DISABLE_ACTION : NAME_TAGS_ENABLE_ACTION) : NAME_TAGS_DISABLED).get(),
            "",
            (var3 ? NAME_TAGS_ENABLED_INDICATOR : NAME_TAGS_DISABLED_INDICATOR).get(),
            "",
            NAME_TAGS_DESC.get()
         );
      var1.action(var3x -> {
         if (var2) {
            this.plugin.getVisualRegistry().get().setEditingNametags(!var3);
         }
      });
   }
}
