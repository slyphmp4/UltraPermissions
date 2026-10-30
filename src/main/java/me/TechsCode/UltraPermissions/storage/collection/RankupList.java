package me.TechsCode.UltraPermissions.storage.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.UserRankup;

public class RankupList extends ArrayList<UserRankup> {
   public RankupList(int var1) {
      super(var1);
   }

   public RankupList() {
      super();
   }

   public RankupList(Collection<? extends UserRankup> var1) {
      super(var1);
   }

   public RankupList bestToWorst() {
      this.sort(Comparator.comparing(var0 -> var0.getGroup().get().map(Group::getPriority).orElse(0)));
      return this;
   }

   public RankupList worstToBest() {
      this.sort(Comparator.comparing(var0 -> var0.getGroup().get().map(Group::getPriority).orElse(0), Comparator.reverseOrder()));
      return this;
   }
}
