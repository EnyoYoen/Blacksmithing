package org.blacksmithing;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BSDamageable {

    static public ItemStack create(Material mat, int durability) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer cont = meta.getPersistentDataContainer();
        cont.set(new NamespacedKey(BlacksmithingPlugin.instance, "blacksmithing_durability"),
                PersistentDataType.INTEGER, durability);
        cont.set(new NamespacedKey(BlacksmithingPlugin.instance, "blacksmithing_ratio"),
                PersistentDataType.FLOAT, (float) mat.getMaxDurability() / (float) durability);
        item.setItemMeta(meta);
        return item;
    }

    static public boolean checkDamageable(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey key = new NamespacedKey(BlacksmithingPlugin.instance, "blacksmithing_durability");
        return item.getType().getMaxDurability() > 0 &&
                meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER) != null;
    }

    static public void takeDamage(ItemStack damageable) {
        ItemMeta meta = damageable.getItemMeta();
        assert meta != null;
        PersistentDataContainer cont = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(BlacksmithingPlugin.instance, "blacksmithing_durability");
        int virtualDurability = cont.get(key, PersistentDataType.INTEGER)-1;
        float ratio  = cont.get(new NamespacedKey(BlacksmithingPlugin.instance, "blacksmithing_ratio"),
                PersistentDataType.FLOAT);
        cont.set(key, PersistentDataType.INTEGER, virtualDurability);
        ((Damageable) meta).setDamage(damageable.getType().getMaxDurability()-(int)(ratio*virtualDurability)-1);
        damageable.setItemMeta(meta);
    }
}
