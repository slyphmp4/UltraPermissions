package me.TechsCode.UltraPermissions.base.security;

import org.bukkit.inventory.Inventory;

/** InventoryView changed from class to interface; use a fixed reflective accessor across server APIs. */
public final class ViewAccess {
    private ViewAccess() {}
    public static Inventory top(Object view) {
        if (view == null) return null;
        try {
            java.lang.reflect.Method method = view.getClass().getMethod("getTopInventory");
            method.setAccessible(true);
            Object result = method.invoke(view);
            return result instanceof Inventory ? (Inventory) result : null;
        } catch (ReflectiveOperationException | RuntimeException invalid) { return null; }
    }
}
