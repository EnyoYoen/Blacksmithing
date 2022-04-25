package org.blacksmithing;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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
        result = null;
        if (!splits[3].trim().equals("")) {
            String[] attributesSplit = splits[3].split(",");
            if (attributesSplit[0].split("=")[0].equals("DURABILITY")) {
                result = BSDamageable.create(resultMaterial, Integer.parseInt(attributesSplit[0].split("=")[1]));
            }
        }
        if (result == null) {
            result = new ItemStack(resultMaterial);
        }

        ItemMeta resultm = result.getItemMeta();
        if (resultm == null) throw new Exception();
        resultm.setDisplayName(name);
        if (!splits[3].trim().equals("")) {
            for (String split : splits[3].split(",")) {
                String[] attributeSplit = split.split("=", 2);
                String attributeName = attributeSplit[0];
                if (attributeName.equals("DURABILITY")) continue;
                Attribute attribute = Attribute.valueOf(attributeName);
                resultm.addAttributeModifier(attribute, new AttributeModifier(attributeName,
                        Double.parseDouble(attributeSplit[1]), AttributeModifier.Operation.ADD_NUMBER));
            }
        }
        result.setItemMeta(resultm);

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
                    materials.add(null);
                    i++;
                    continue;
                }
            }
            materials.add(mat);
            i++;
        }
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
