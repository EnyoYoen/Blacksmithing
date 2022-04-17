package org.blacksmithing;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BMaterial extends ItemStack {

    public BMaterial(List<ItemStack> materials, long duration) {
        super(Material.LAVA_BUCKET);
        ItemMeta meta = this.getItemMeta();
        assert meta != null;
        PersistentDataContainer cont = meta.getPersistentDataContainer();
        for (ItemStack it : materials) {
            cont.set(new NamespacedKey(BlacksmithingPlugin.instance, it.getType().toString()),
                    PersistentDataType.INTEGER, it.getAmount());
        }
        cont.set(new NamespacedKey(BlacksmithingPlugin.instance, "duration"), PersistentDataType.LONG, duration);
        meta.setDisplayName("Molten alloy");
        this.setItemMeta(meta);
    }

    public static String checkRecipes(Map<List<ItemStack>, String> recipes, ItemStack it) {
        List<ItemStack> its = new LinkedList<>();
        Material[] mats = {Material.COAL, Material.COPPER_INGOT, Material.REDSTONE, Material.LAPIS_LAZULI,
                Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.NETHERITE_INGOT};
        for (Material mat : mats) {
            NamespacedKey key = new NamespacedKey(BlacksmithingPlugin.instance, mat.toString());
            ItemMeta meta = it.getItemMeta();
            assert meta != null;
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(key , PersistentDataType.INTEGER)) {
                int amount = container.get(key, PersistentDataType.INTEGER);
                its.add(new ItemStack(mat, amount));
            }
        }
        return recipes.get(its);
    }
}
