package org.blacksmithing;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BSCraft {

    ItemStack result;
    List<Material> materials = new ArrayList<>();
    HashMap<Integer, String> specials = new HashMap<>();

    public BSCraft(String craftStr, List<BSRecipe> recipes) throws Exception {
        String[] splits = craftStr.split(":",4);

        String name = splits[0];
        Material resultMaterial = Material.getMaterial(splits[1]);
        if (resultMaterial == null) throw new Exception();
        result = new ItemStack(resultMaterial);
        ItemMeta resultm = result.getItemMeta();
        if (resultm == null) throw new Exception();
        resultm.setDisplayName(name);
        result.setItemMeta(resultm);

        HashMap<Material, Character> materials = new HashMap<>();
        String[] shape = {"", "", ""};
        Character[] characters = {'A','B','C','D','E','F','G','H','I'};
        int charCounter = 0;
        int i = 0;
        for (String split : splits[2].split(",", 9)) {
            Material mat = Material.getMaterial(split);
            if (mat == null) {
                boolean found = false;
                for (BSRecipe recipe : recipes) {
                    if (recipe.name.equals(split)) {
                        mat = recipe.resultMaterial;
                        specials.put(i, recipe.name.substring(recipe.name.lastIndexOf("§")+2));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    this.materials.add(null);
                    shape[i / 3] += " ";
                    i++;
                    continue;
                }
            }
            this.materials.add(mat);
            if (materials.containsKey(mat)) {
                shape[i / 3] += materials.get(mat);
            } else {
                shape[i/3] += characters[charCounter];
                materials.put(mat, characters[charCounter]);
                charCounter++;
            }                
            i++;
        }
        if (materials.size() == 0) throw new Exception();
    }

    public ItemStack equals(ItemStack[] items) {
        for (int i = 0 ; i < 9 ; i++) {
            if (items[i] == null) {
                 if (materials.get(i) == null)
                     continue;
                 else
                     return null;
            }
            if (!items[i].getType().equals(materials.get(i)))
                return null;
            if (specials.containsKey(i)) {
                String displayName = items[i].getItemMeta().getDisplayName();
                if (displayName.isEmpty() || !displayName.substring(displayName.lastIndexOf("§")+2).equals(specials.get(i)))
                    return null;
            }
        }
        return result;
    }
}
