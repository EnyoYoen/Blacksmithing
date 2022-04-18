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
            Material mat = Material.getMaterial(items[1]);
            if (mat == null) throw new Exception();
            materials.add(new ItemStack(mat, Integer.parseInt(items[0])));
        }
    }
}
