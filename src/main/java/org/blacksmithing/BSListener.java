package org.blacksmithing;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.*;

public class BSListener implements Listener {

    List<Material> allowedMaterials = new ArrayList<>();
    List<BSRecipe> recipes = new ArrayList<>();
    List<BSCraft> crafts = new ArrayList<>();
    Map<Coord, List<ItemStack>> tablesMaterials = new HashMap<>();
    Map<Coord, Long> tablesStartTimestamp = new HashMap<>();

    public void loadRecipes() {
        try {
            BufferedReader in = new BufferedReader(new FileReader("blacksmithing_materials.txt"));
            String line;
            int i = 0;
            while (((line = in.readLine()) != null)) {
                Material mat = Material.getMaterial(line);
                if (mat == null)
                    Bukkit.getConsoleSender().sendMessage("§2[Blacksmithing] §4§lWarning:§r Material at line " +
                            (i + 1) + " of file 'blacksmithing_materials.txt' is incorrect");
                else
                    allowedMaterials.add(mat);
                i++;
            }
        } catch (IOException ex) {
            Bukkit.getConsoleSender().sendMessage("§2[Blacksmithing] §4§lError:§r Could not open file " +
                    "'blacksmithing_materials.txt'");
        }

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
            Bukkit.getConsoleSender().sendMessage("§2[Blacksmithing] §4§lError:§r Could not open file " +
                    "'blacksmithing_recipes.txt'");
        }

        try {
            BufferedReader in = new BufferedReader(new FileReader("blacksmithing_crafts.txt"));
            String line;
            int i = 0;
            while (((line = in.readLine()) != null)) {
                try {
                    crafts.add(new BSCraft(line, recipes));
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage("§2[Blacksmithing] §4§lWarning:§r Craft at line " +
                            (i + 1) + " of file 'blacksmithing_crafts.txt' is incorrect");
                }
                i++;
            }
        } catch (IOException ex) {
            Bukkit.getConsoleSender().sendMessage("§2[Blacksmithing] §4§lError:§r Could not open file " +
                    "'blacksmithing_crafts.txt'");
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        ItemStack[] items = e.getInventory().getMatrix();
        ItemStack result;
        for (BSCraft craft : crafts) {
            if ((result = craft.equals(items)) != null) {
                e.getInventory().setResult(result);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlockPlaced();
        Material mat = block.getType();

        int x = block.getX();
        int z = block.getZ();
        int y1, y2, y3;
        if (mat == Material.CAULDRON) {
            y1 = -1;
            y2 = 0;
            y3 = 1;
        } else if (mat == Material.MAGMA_BLOCK) {
            y1 = 0;
            y2 = 1;
            y3 = 2;
        } else if (mat == Material.IRON_TRAPDOOR || mat == Material.CHAIN) {
            y1 = -2;
            y2 = -1;
            y3 = 0;
        } else {
            return;
        }

        if (block.getRelative(0, y1, 0).getType() != Material.MAGMA_BLOCK ||
                block.getRelative(0, y2, 0).getType() != Material.CAULDRON ||
                (block.getRelative(0, y3, 0).getType() != Material.CHAIN &&
                 block.getRelative(0, y3, 0).getType() != Material.IRON_TRAPDOOR))
            return;

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("blacksmithing_tables.txt", true));
            out.write(x + ";" + (block.getY() + y2) + ";" + z + "\n");
            out.close();
            e.getPlayer().sendMessage("§aBlacksmith table created!");
        } catch (IOException ex) {
            Bukkit.getConsoleSender().sendMessage("§2[Blacksmithing] §4§lError:§r Could not open file " +
                    "'blacksmithing_tables.txt'");
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockBreakEvent e) {
        Block block = e.getBlock();
        Material mat = block.getType();

        int y;
        if (mat == Material.CAULDRON || mat == Material.LAVA_CAULDRON) {
            y = 0;
        } else if (mat == Material.CHAIN || mat == Material.IRON_TRAPDOOR) {
            y = 1;
        } else if (mat == Material.MAGMA_BLOCK) {
            y = -1;
        } else {
            return;
        }

        try {
            File inFile = new File("blacksmithing_tables.txt");
            File tempFile = new File("blacksmithing_tables_tmp.txt");
            BufferedReader in = new BufferedReader(new FileReader(inFile));
            BufferedWriter out = new BufferedWriter(new FileWriter(tempFile, true));
            String line;
            while (((line = in.readLine()) != null)) {
                String[] splits = line.split(";");
                if (Integer.parseInt(splits[0]) == block.getX() &&
                    Integer.parseInt(splits[1])+y == block.getY() &&
                    Integer.parseInt(splits[2]) == block.getZ()) {
                    e.getPlayer().sendMessage("§bBlacksmith table removed!");
                    continue;
                }
                out.write(line + "\n");
            }
            out.close();
            tempFile.renameTo(inFile);
        } catch (IOException ex) {
            Bukkit.getConsoleSender().sendMessage("§2[Blacksmithing] §4§lError:§r Could not open file " +
                    "'blacksmithing_tables.txt'");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Block clickedBlock = e.getClickedBlock();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (clickedBlock == null) return;
            if (clickedBlock.getType() == Material.CAULDRON || clickedBlock.getType() == Material.LAVA_CAULDRON) {
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
                            Coord coords = new Coord(x,y,z);
                            Material mat = e.getItem().getType();
                            if (mat == Material.BUCKET && tablesMaterials.containsKey(coords)) {
                                clickedBlock.getWorld().setType(clickedBlock.getLocation(), Material.CAULDRON);

                                PlayerInventory inv = e.getPlayer().getInventory();
                                ItemStack itInHand = inv.getItemInMainHand();
                                itInHand.setAmount(itInHand.getAmount()-1);
                                inv.setItemInMainHand(itInHand);

                                BSMaterial it = new BSMaterial(tablesMaterials.get(coords),
                                        System.currentTimeMillis()-tablesStartTimestamp.get(coords));
                                tablesMaterials.remove(coords);
                                tablesStartTimestamp.remove(coords);
                                e.getPlayer().getInventory().addItem(it);
                            } else if (mat == Material.CLOCK && tablesMaterials.containsKey(coords)) {
                                String formattedDuration = "";
                                long duration = (System.currentTimeMillis() - tablesStartTimestamp.get(coords))/1000;
                                long hours = duration / 3600;
                                long minutes = (duration % 3600) / 60;
                                long seconds = duration % 60;
                                boolean comma = false;

                                if (hours != 0) {
                                    formattedDuration += hours + " hour" + (hours > 1 ? "s" : "");
                                    comma = true;
                                }
                                if (minutes != 0) {
                                    formattedDuration += (comma ? ", " : "") + minutes + " minute"
                                            + (minutes > 1 ? "s" : "");
                                    comma = true;
                                } else {
                                    comma = false;
                                }
                                if (!(comma && seconds == 0))
                                    formattedDuration += (comma ? " and " : "") + seconds + " second"
                                            + (seconds > 1 ? "s" : "");

                                e.getPlayer().sendMessage("§2[Blacksmithing] §r§6Started " + formattedDuration
                                        + " ago");
                            } else if (allowedMaterials.contains(mat)) {
                                clickedBlock.getWorld().setType(clickedBlock.getLocation(), Material.LAVA_CAULDRON);

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

                    e.getPlayer().getInventory().addItem(BSMaterial.checkRecipes(recipes, allowedMaterials,
                            e.getItem()));
                    e.setCancelled(true);
                }
            }
        }
    }
}
