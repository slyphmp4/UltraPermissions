package me.TechsCode.UltraPermissions.base.item;

import java.io.IOException;
import org.bukkit.inventory.ItemStack;
import me.TechsCode.UltraPermissions.base.security.SafeItems;

public class ItemSerializer {
   public ItemSerializer() {}
   public static String itemTo64(ItemStack item) { return SafeItems.encode(item); }
   public static ItemStack itemFrom64(String data) throws IOException { return SafeItems.decode(data); }
}
