package me.TechsCode.UltraPermissions;

import me.TechsCode.UltraPermissions.base.translations.Phrase;

public enum DefaultGroupAssignOption {
   FIRST_JOIN,
   NO_GROUP,
   ALWAYS_ASSIGN;

   private static final Phrase FIRST_JOIN_DESC = Phrase.create("defaultGroupAssign.firstJoin.description", "Only add default groups on the first join");
   private static final Phrase FIRST_JOIN_EXPLAIN = Phrase.create("defaultGroupAssign.firstJoin.explanation", "Default Groups will be only added on first join");
   private static final Phrase NO_GROUP_DESC = Phrase.create("defaultGroupAssign.noGroup.description", "If a player has no groups, assign all default groups");
   private static final Phrase NO_GROUP_EXPLAIN = Phrase.create("defaultGroupAssign.noGroup.explanation", "Default Groups will be assigned if a player has no groups");
   private static final Phrase ALWAYS_DESC = Phrase.create("defaultGroupAssign.always.description", "Always force every player to all default groups");
   private static final Phrase ALWAYS_EXPLAIN = Phrase.create("defaultGroupAssign.always.explanation", "Default Groups will always be assigned to all players");

   public DefaultGroupAssignOption next() {
      switch (this) {
         case FIRST_JOIN:
            return NO_GROUP;
         case NO_GROUP:
            return ALWAYS_ASSIGN;
         case ALWAYS_ASSIGN:
            return FIRST_JOIN;
         default:
            return null;
      }
   }

   public String getDescription() {
      switch (this) {
         case FIRST_JOIN: return FIRST_JOIN_DESC.get();
         case NO_GROUP: return NO_GROUP_DESC.get();
         case ALWAYS_ASSIGN: return ALWAYS_DESC.get();
         default: throw new IllegalStateException();
      }
   }

   public String getExplanation() {
      switch (this) {
         case FIRST_JOIN: return FIRST_JOIN_EXPLAIN.get();
         case NO_GROUP: return NO_GROUP_EXPLAIN.get();
         case ALWAYS_ASSIGN: return ALWAYS_EXPLAIN.get();
         default: throw new IllegalStateException();
      }
   }
}
