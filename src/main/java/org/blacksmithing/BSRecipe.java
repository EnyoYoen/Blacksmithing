package org.blacksmithing;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class BSRecipe {
    public Set<ItemStack> materials;
    public Material resultMaterial;
    public String name;
    public long duration;
    public long durationMarginOfError;

    public BSRecipe(String recipeStr) throws Exception {
        String[] splits = recipeStr.split(":",5);

        duration = Integer.parseInt(splits[1]);
        durationMarginOfError = Integer.parseInt(splits[2]);
        name = splits[3];
        resultMaterial = Material.getMaterial(splits[4]);
        if (resultMaterial == null) throw new Exception();

        materials = new HashSet<>();
        for (String split : splits[0].split(",")) {
            String[] items = split.split(" ");
            Material mat = switch (items[1]) {
                case "coal" -> Material.COAL;
                case "copper" -> Material.COPPER_INGOT;
                case "redstone" -> Material.REDSTONE;
                case "lapis" -> Material.LAPIS_LAZULI;
                case "iron" -> Material.IRON_INGOT;
                case "gold" -> Material.GOLD_INGOT;
                case "diamond" -> Material.DIAMOND;
                case "netherite" -> Material.NETHERITE_INGOT;
                default -> Material.ROTTEN_FLESH;
            };
            materials.add(new ItemStack(mat, Integer.parseInt(items[0])));
        }
    }
}
