package org.blacksmithing;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.*;

public class BSListener implements Listener {

    List<BSRecipe> recipes = new ArrayList<>();
    Map<Coord, List<ItemStack>> tablesMaterials = new HashMap<>();
    Map<Coord, Long> tablesStartTimestamp = new HashMap<>();

    public void loadRecipes() {
        try {
            BufferedReader in = new BufferedReader(new FileReader("blacksmithing_recipes.txt"));
            String line;
            int i = 0;
            while (((line = in.readLine()) != null)) {
                try {
                    recipes.add(new BSRecipe(line));
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage("§2[Blacksmithing] §4§lWarning:§r Recipe at line " +
                            (i + 1) + " of file 'blacksmithing_recipes.txt' is incorrect");
                }
                i++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block center = e.getBlockPlaced();
        if (center.getType() == Material.CAULDRON) {
            Material heatSource = center.getRelative(0, -1, 0).getType();
            if (heatSource != Material.LAVA && heatSource != Material.FIRE)
                return;
            for (int x = -1 ; x < 2 ; x++) {
                for (int z = -1 ; z < 2 ; z++) {
                    if (x != 0 && z != 0 && center.getRelative(x, 0, z).getType() != Material.NETHER_BRICK) {
                        return;
                    }
                }
            }
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter("blacksmithing_tables.txt", true));
                out.write(center.getX() + ";" + center.getY() + ";" + center.getZ() + "\n");
                out.close();
                e.getPlayer().sendMessage("§aBlacksmith table created!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Block clickedBlock = e.getClickedBlock();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (clickedBlock == null) return;
            if (clickedBlock.getType() == Material.CAULDRON) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader("blacksmithing_tables.txt"));
                    String line;
                    while ((line = in.readLine()) != null) {
                        String[] splits = line.split(";", 3);
                        int x = clickedBlock.getX();
                        int y = clickedBlock.getY();
                        int z = clickedBlock.getZ();
                        if (x == Integer.parseInt(splits[0])
                                && y == Integer.parseInt(splits[1])
                                && z == Integer.parseInt(splits[2])) {
                            if (e.getItem() == null) return;
                            e.setCancelled(true);
                            Coord coords = new Coord(x,y,z);
                            Material mat = e.getItem().getType();
                            if (mat == Material.DIAMOND || mat == Material.IRON_INGOT || mat == Material.GOLD_INGOT) {
                                PlayerInventory inv = e.getPlayer().getInventory();
                                ItemStack itInHand = inv.getItemInMainHand();
                                itInHand.setAmount(itInHand.getAmount()-1);
                                inv.setItemInMainHand(itInHand);
                                if (tablesMaterials.containsKey(coords)) {
                                    List<ItemStack> mats = tablesMaterials.get(coords);
                                    boolean found = false;
                                    int i;
                                    for (i = 0 ; i < mats.size() ; i++) {
                                        if (mat == mats.get(i).getType()) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (found)
                                        mats.get(i).setAmount(mats.get(i).getAmount()+1);
                                    else
                                        mats.add(new ItemStack(mat));
                                } else {
                                    List<ItemStack> list = new ArrayList<>();
                                    list.add(new ItemStack(mat));
                                    tablesMaterials.put(coords, list);
                                    tablesStartTimestamp.put(coords, System.currentTimeMillis());
                                }
                            } else if (mat == Material.BUCKET && tablesMaterials.containsKey(coords)) {
                                PlayerInventory inv = e.getPlayer().getInventory();
                                ItemStack itInHand = inv.getItemInMainHand();
                                itInHand.setAmount(itInHand.getAmount()-1);
                                inv.setItemInMainHand(itInHand);

                                BSMaterial it = new BSMaterial(tablesMaterials.get(coords),
                                    System.currentTimeMillis()-tablesStartTimestamp.get(coords));
                                tablesMaterials.remove(coords);
                                tablesStartTimestamp.remove(coords);
                                e.getPlayer().getInventory().addItem(it);
                            }
                            return;
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else if (clickedBlock.getType() == Material.SMITHING_TABLE) {
                ItemStack it = e.getItem();
                if (it == null) return;
                ItemMeta meta = it.getItemMeta();
                if (meta == null) return;
                if (meta.getDisplayName().equals("Molten alloy")) {
                    PlayerInventory inv = e.getPlayer().getInventory();
                    ItemStack itInHand = inv.getItemInMainHand();
                    itInHand.setAmount(itInHand.getAmount()-1);
                    inv.setItemInMainHand(itInHand);

                    e.getPlayer().getInventory().addItem(BSMaterial.checkRecipes(recipes, e.getItem()));
                    e.setCancelled(true);
                }
            }
        }
    }
}
