package org.blacksmithing;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class BSMaterial extends ItemStack {

    public BSMaterial(List<ItemStack> materials, long duration) {
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

    public static ItemStack checkRecipes(List<BSRecipe> recipes, ItemStack it) {
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            Set<ItemStack> its = new HashSet<>();
            Material[] mats = {Material.COAL, Material.COPPER_INGOT, Material.REDSTONE, Material.LAPIS_LAZULI,
                    Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.NETHERITE_INGOT};

            PersistentDataContainer container = meta.getPersistentDataContainer();
            for (Material mat : mats) {
                NamespacedKey key = new NamespacedKey(BlacksmithingPlugin.instance, mat.toString());
                if (container.has(key, PersistentDataType.INTEGER)) {
                    int amount = container.get(key, PersistentDataType.INTEGER);
                    its.add(new ItemStack(mat, amount));
                }
            }
            long duration = container.get(new NamespacedKey(BlacksmithingPlugin.instance, "duration")
                    , PersistentDataType.LONG);

            for (BSRecipe recipe : recipes) {
                if (recipe.materials.equals(its)
                        && recipe.duration - recipe.durationMarginOfError <= duration
                        && recipe.duration + recipe.durationMarginOfError >= duration) {
                    ItemStack resultItem = new ItemStack(recipe.resultMaterial);
                    ItemMeta resultMeta = resultItem.getItemMeta();
                    if (resultMeta == null) break;
                    resultMeta.setDisplayName(recipe.name);
                    resultItem.setItemMeta(resultMeta);
                    return resultItem;
                }
            }
        }
        ItemStack waste = new ItemStack(Material.IRON_NUGGET);
        ItemMeta wasteMeta = waste.getItemMeta();
        wasteMeta.setDisplayName("§8Metal waste");
        waste.setItemMeta(wasteMeta);
        return waste;
    }
}
