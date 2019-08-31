package me.randomhashtags.merchants;

import me.randomhashtags.merchants.utils.MFeature;
import me.randomhashtags.merchants.utils.supported.FactionsAPI;
import me.randomhashtags.merchants.utils.universal.UInventory;
import me.randomhashtags.merchants.utils.universal.UMaterial;
import me.randomhashtags.merchants.utils.supported.economy.Vault;
import me.randomhashtags.merchants.utils.objects.CustomPotion;
import me.randomhashtags.merchants.utils.objects.Merchant;
import me.randomhashtags.merchants.utils.objects.MerchantItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

public class MerchantsAPI extends MFeature implements Listener, CommandExecutor {
    private boolean citizens = false, closeInvUponSuccessfulPurchase = true, closeInvUponSuccessfulSell = true;
    private static MerchantsAPI instance;
    public static MerchantsAPI getAPI() {
        if(instance == null) instance = new MerchantsAPI();
        return instance;
    }

    private static TreeMap<Integer, String> treemap;
    private HashMap<Player, String> previousShop;
    private HashMap<Player, MerchantItem> isPurchasing, isSelling;

    private final FactionsAPI fapi = FactionsAPI.getFactionsAPI();

    public File dataF;
    public YamlConfiguration data;

    public final YamlConfiguration shops = YamlConfiguration.loadConfiguration(new File(merchants.getDataFolder() + File.separator + "shops.yml"));
    private final FileConfiguration config = merchants.getConfig();
    private UInventory purchaseInv, sellInv;
    private ItemStack purchaseCancel, purchaseOne, purchaseStack, purchaseInventory, sellCancel, sellOne, sellStack, sellInventory;

    private HashMap<Integer, List<Integer>> bTypeSlots, sTypeSlots;
    private List<Integer> purchaseDisplayItem, sellDisplayItem;
    public HashMap<String, UInventory> customInventories;
    public HashMap<String, HashMap<Integer, String>> opens;
    public HashMap<String, HashMap<Integer, ItemStack>> purchases;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final String m = event.getMessage().substring(1);
        for(String s : config.getConfigurationSection("commands").getKeys(false)) {
            final Merchant merchant = Merchant.valueOf(s);
            if(merchant != null) {
                final String cmdp = merchant.getCommandPermission(), tl = m.toLowerCase();
                if(tl.equals(s)) {
                    if(!merchant.isAccessibleFromCMD()) return;
                    else if(cmdp == null || player.hasPermission(cmdp)) {
                        if(!config.getBoolean("commands." + s + ".cmd")) return;
                        event.setCancelled(true);
                        final String i = config.getString("commands." + s + ".opens");
                        viewInventory(player, i);
                    }
                    return;
                } else if(tl.startsWith(s + " spawn ") && (cmdp == null || player.hasPermission(config.getString("commands." + s + ".permission") + ".spawn"))) {
                    event.setCancelled(true);
                    if(citizens) {
                        final int y = (s + " spawn ").length();
                        final Merchant mer = Merchant.valueOf(s);
                        if(mer != null)
                            mer.spawn(player.getLocation(), ChatColor.translateAlternateColorCodes('&', m.substring(y)));
                        else
                            player.sendMessage("[Merchants] That is not a valid Merchant path name!");
                    } else {
                        player.sendMessage("[Merchants] You need the Citizens plugin installed to do this!");
                    }
                    return;
                }
            }
        }
    }

    private boolean hasPermission(CommandSender sender, String permission, boolean sendNoPerm) {
        if(!(sender instanceof Player) || sender.hasPermission(permission)) return true;
        if(sendNoPerm) sendStringListMessage(sender, config.getStringList("messages.no permission"), null);
        return false;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final Player player = sender instanceof Player ? (Player) sender : null;
        final String n = cmd.getName();
        final int l = args.length;
        if(n.equals("merchants")) {
            final String ve = merchants.getDescription().getVersion();
            if(l == 0) {
                sendStringListMessage(sender, Arrays.asList("&6[Merchants] &aVersion: &e" + ve), null);
            } else if(l == 1 && args[0].equals("reload")) {
                merchants.reload();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[Merchants] &aMerchants v" + ve + " reloaded."));
            }
        } else if(n.equals("sell") && player != null) {
            if(l == 0) {
                sendStringListMessage(player, config.getStringList("messages.sell usage"), null);
            } else {
                final String a = args[0];
                if(a.equals("chest") && hasPermission(player, "Merchants.sell.chest", true)) {
                    final String[] type = l == 1 ? null : args[2].split(":");
                    final Block tblock = player.getTargetBlock(null, 5);
                    if(fapi.getFactionAt(tblock.getLocation()).equals(fapi.getFaction(player))) {
                        final Material t = tblock.getType();
                        if(t.name().contains("CHEST")) {
                            final Chest chest = (Chest) tblock.getState();
                            final InventoryHolder i = chest.getInventory().getHolder();
                            final DoubleChest d = i instanceof DoubleChest ? (DoubleChest) i : null;
                            sellchest(player, d != null ? d.getInventory() : chest.getBlockInventory(), type);
                        } else {
                            sendStringListMessage(player, config.getStringList("messages.must be looking at chest"), null);
                        }
                    }
                } else if(a.equals("hand") && hasPermission(player, "Merchants.sell.hand", true) || a.startsWith("inv") && hasPermission(player, "Merchants.sell.inventory", true)) {
                    sellItems(player, player.getItemInHand(), a.startsWith("inv"));
                }
            }
        }
        return true;
    }

    private void sellchest(Player player, Inventory inv, String[] type) {
        final Economy eco = Vault.economy;
        final HashMap<UMaterial, Integer> amounts = new HashMap<>();
        for(int i = 0; i < inv.getSize(); i++) {
            final ItemStack is = inv.getItem(i);
            if(is != null && !is.getType().equals(Material.AIR)) {
                final MerchantItem m = MerchantItem.valueOf(is);
                final double sp = m != null ? m.sellPrice : 0.00;
                final byte d = is.getData().getData();
                final String mat = type != null ? type[0].toUpperCase() : null, ism = is.getType().name();
                final UMaterial um = UMaterial.match(is);
                final String umn = um != null ? um.name() : null;
                if(m != null && sp != 0.00 && (type == null || type.length == 1 && (mat.equals(ism) || mat.equals(umn)) || (mat.equals(ism) || mat.equals(umn)) && d == Byte.parseByte(type[1]))) {
                    final int amount = is.getAmount();
                    if(!amounts.keySet().contains(um)) amounts.put(um, amount);
                    else amounts.put(um, amounts.get(um)+amount);
                    inv.setItem(i, new ItemStack(Material.AIR));
                }
            }
        }
        for(UMaterial m : amounts.keySet()) {
            final int amount = amounts.get(m);
            final MerchantItem mi = MerchantItem.valueOf(m.getItemStack());
            final double sp = mi.sellPrice;
            final double cost = round(amount*sp, 2);
            final HashMap<String, String> replacements = new HashMap<>();
            replacements.put("{SELL}", formatDouble(sp));
            replacements.put("{AMOUNT}", formatInt(amount));
            replacements.put("{COST}", formatDouble(cost));
            replacements.put("{ITEM}", m.name());
            eco.depositPlayer(player, cost);
            sendStringListMessage(player, config.getStringList("messages.sell success"), replacements);
        }
    }
    public void sellItems(Player player, ItemStack i, boolean inventory) {
        final FileConfiguration c = config;
        if(i == null || i.getType().equals(Material.AIR)) {
            sendStringListMessage(player, c.getStringList("messages.need to be holding item"), null);
        } else {
            final MerchantItem m = MerchantItem.valueOf(i);
            final List<String> msg = c.getStringList("messages.cannot sell item to server");
            if(m == null) {
                sendStringListMessage(player, msg, null);
            } else {
                final ItemStack is = m.getPurchase();
                final String it = is.hasItemMeta() && is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : UMaterial.match(is).name();
                final double s = m.sellPrice;
                if(i.isSimilar(is)) {
                    final HashMap<String, String> replacements = new HashMap<>();
                    replacements.put("{ITEM}", it);
                    final Economy e = Vault.economy;
                    final int amount;
                    if(inventory) {
                        amount = getAmount(player.getInventory(), is);
                        removeItem(player, is, amount);
                    } else {
                        amount = i.getAmount();
                        player.getInventory().setItemInHand(new ItemStack(Material.AIR));
                    }
                    final double total = amount*s;
                    replacements.put("{COST}", formatDouble(total));
                    replacements.put("{AMOUNT}", formatInt(amount));
                    e.depositPlayer(player, total);
                    sendStringListMessage(player, c.getStringList("messages.sell success"), replacements);
                    player.updateInventory();
                } else {
                    sendStringListMessage(player, msg, null);
                }
            }
        }
    }

    public void saveMerchants() {
        if(data == null) {
            dataF = new File(merchants.getDataFolder() + File.separator + "_data.yml");
            data = YamlConfiguration.loadConfiguration(dataF);
        }
        try {
            data.set("living", null);
            for(UUID u : Merchant.liveMerchants.keySet())
                data.set("living." + u.toString(), Merchant.liveMerchants.get(u).getPath());
            data.save(dataF);
            dataF = new File(merchants.getDataFolder() + File.separator + "_data.yml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadMerchants() {
        if(data == null) {
            dataF = new File(merchants.getDataFolder() + File.separator + "_data.yml");
            data = YamlConfiguration.loadConfiguration(dataF);
        }
        final ConfigurationSection cs = data.getConfigurationSection("living");
        final Set<String> set = cs != null ? cs.getKeys(false) : null;
        if(set != null) {
            for(String merchant : set) {
                final Merchant m = Merchant.valueOf(data.getString("living." + merchant));
                Merchant.liveMerchants.put(UUID.fromString(merchant), m);
            }
        }
    }

    public void reloadMerchants() {
        citizens = Bukkit.getPluginManager().isPluginEnabled("Citizens");
        closeInvUponSuccessfulPurchase = shops.getBoolean("close inventory upon.successful purchase");
        closeInvUponSuccessfulSell = shops.getBoolean("close inventory upon.successful sell");
        purchaseInv = new UInventory(null, shops.getInt("purchase.size"), ChatColor.translateAlternateColorCodes('&', shops.getString("purchase.title")));
        purchaseCancel = d(shops, "purchase.cancel");
        purchaseOne = d(shops, "purchase.one");
        purchaseStack = d(shops, "purchase.stack");
        purchaseInventory = d(shops, "purchase.inventory");

        sellInv = new UInventory(null, shops.getInt("sell.size"), ChatColor.translateAlternateColorCodes('&', shops.getString("sell.title")));
        sellCancel = d(shops, "sell.cancel");
        sellOne = d(shops, "sell.one");
        sellStack = d(shops, "sell.stack");
        sellInventory = d(shops, "sell.inventory");

        for(int i = 1; i <= 2; i++) {
            final String type = i == 1 ? "purchase" : "sell";
            final Inventory inventory = (i == 1 ? purchaseInv : sellInv).getInventory();
            final ItemStack cancel = i == 1 ? purchaseCancel : sellCancel, one = i == 1 ? purchaseOne : sellOne, stack = i == 1 ? purchaseStack : sellStack, inv = i == 1 ? purchaseInventory : sellInventory;
            final HashMap<Integer, List<Integer>> h = i == 1 ? bTypeSlots : sTypeSlots;
            final List<Integer> displayitems = i == 1 ? purchaseDisplayItem : sellDisplayItem;
            for(String s : shops.getConfigurationSection(type).getKeys(false)) {
                if(!s.equals("title") && !s.equals("size") && !s.equals("cancel") && !s.equals("one") && !s.equals("stack") && !s.equals("inventory")) {
                    final String ii = shops.getString(type + "." + s + ".item");
                    if(ii != null) {
                        final int slot = shops.getInt(type + "." + s + ".slot");
                        if(ii.equals("{ITEM}"))
                            displayitems.add(slot);
                        else if(ii.startsWith(type)) {
                            final int t = ii.contains("one") ? 1 : ii.contains("stack") ? 2 : 3;
                            if(!h.keySet().contains(t)) h.put(t, new ArrayList<>());
                            h.get(t).add(slot);
                            inventory.setItem(slot, t == 1 ? one.clone() : t == 2 ? stack.clone() : inv.clone());
                        } else
                            inventory.setItem(slot, ii.equals("cancel") ? cancel.clone() : d(shops,  type + "." + s));
                    }
                }
            }
        }
        final List<String> notbuyable = shops.getStringList("lores.not buyable"), notsellable = shops.getStringList("lores.not sellable");
        for(String cmd : config.getConfigurationSection("commands").getKeys(false)) {
            if(config.get("commands." + cmd + ".opens") != null) {
                final Merchant merchant = new Merchant(cmd, config.getBoolean("commands." + cmd + ".cmd"), config.getString("commands." + cmd + ".command permission"), config.getString("commands." + cmd + ".npc permission"), config.getString("commands." + cmd + ".opens"), config.getBoolean("commands." + cmd + ".npc"));
                final String o = config.getString("commands." + cmd + ".opens");
                if(!purchases.containsKey(o)) purchases.put(o, new HashMap<>());
                if(!opens.containsKey(o)) opens.put(o, new HashMap<>());
                final YamlConfiguration yml = YamlConfiguration.loadConfiguration(new File(merchants.getDataFolder() + File.separator + "shops" + File.separator, o + ".yml"));
                final UInventory ui = new UInventory(null, yml.getInt("size"), ChatColor.translateAlternateColorCodes('&', yml.getString("title")));
                final Inventory i = ui.getInventory();
                for(String s : yml.getConfigurationSection("items").getKeys(false)) {
                    if(!s.equals("title") && !s.equals("size")) {
                        final int slot = yml.getInt("items." + s + ".slot");
                        final String prices = yml.getString("items." + s + ".prices");
                        item = d(yml, "items." + s);
                        if(item == null && yml.get("items." + s + ".potion") != null) {
                            final boolean isSplash = yml.getBoolean("items." + s + ".splash");
                            lore.clear();
                            final List<PotionEffect> effects = new ArrayList<>();
                            for(String p : yml.getStringList("items." + s + ".potion")) {
                                final String P = p.toLowerCase();
                                if(P.startsWith("effects{")) {
                                    for(String e : P.split("\\{")[1].split("}")[0].split(";")) {
                                        final PotionEffectType t = PotionEffectType.getByName(e.split(":")[0].toUpperCase());
                                        final int lvl = Integer.parseInt(e.split(":")[1]), duration = Integer.parseInt(e.split(":")[2]);
                                        final PotionEffect pe = new PotionEffect(t, duration*20, lvl);
                                        int sec = duration, min = sec/60, hr = min/60;
                                        sec -= min*60;
                                        min -= hr*60;
                                        effects.add(pe);
                                        final String tn = t.getName().replace("INCREASE_DAMAGE", "STRENGTH").replace("FAST_DIGGING", "HASTE").replace("HEAL", "INSTANT_HEALTH").replace("HARM", "INSTANT_DAMAGE").replace("JUMP", "JUMP_BOOST").replace("SLOW", "SLOWNESS");
                                        String tnn = tn.contains("_") ? tn.replace("_", " ") : tn.substring(0, 1).toUpperCase() + tn.substring(1).toLowerCase();
                                        if(tnn.contains(" ")) {
                                            String f = tnn.split(" ")[0], l = tnn.split(" ")[1];
                                            f = f.substring(0, 1).toUpperCase() + f.substring(1).toLowerCase();
                                            l = l.substring(0, 1).toUpperCase() + l.substring(1).toLowerCase();
                                            tnn = f + " " + l;
                                        }
                                        final ChatColor c = tn.contains("HUNGER") || tn.contains("WEAKNESS") || tn.contains("POISON") || tn.contains("SLOW") || tn.contains("WITHER") || tn.contains("CONFUSION") ? ChatColor.RED : ChatColor.GRAY;
                                        final String time = !t.getName().equals("HEAL") && !t.getName().equals("HARM") ? " (" + (hr > 0 ? hr + ":" : "") + min + ":" + (sec < 10 ? "0" + sec : sec) + ")" : "";
                                        lore.add(c + tnn + " " + toRoman(lvl+1) + time);
                                    }
                                } else {
                                    lore.add(ChatColor.translateAlternateColorCodes('&', p));
                                }
                            }
                            String type = null;
                            for(PotionEffect b : effects) {
                                if(type == null || type.equals("AWKWARD")) {
                                    final PotionEffectType a = b.getType();
                                    if(a.equals(PotionEffectType.FIRE_RESISTANCE)) type = "FIRE_RESISTANCE";
                                    else if(a.equals(PotionEffectType.HARM)) type = "HARMING_1";
                                    else if(a.equals(PotionEffectType.HEAL)) type = "HEALING_1";
                                    else if(a.equals(PotionEffectType.INCREASE_DAMAGE)) type = "STRENGTH_1";
                                    else if(a.equals(PotionEffectType.INVISIBILITY)) type = "INVISIBILITY";
                                    else if(a.equals(PotionEffectType.JUMP)) type = "LEAPING_1";
                                    else if(a.equals(PotionEffectType.NIGHT_VISION)) type = "NIGHT_VISION";
                                    else if(a.equals(PotionEffectType.POISON)) type = "POISON_1";
                                    else if(a.equals(PotionEffectType.REGENERATION)) type = "REGENERATION_1";
                                    else if(a.equals(PotionEffectType.SLOW)) type = "SLOWNESS_1";
                                    else if(a.equals(PotionEffectType.SPEED)) type = "SWIFTNESS_1";
                                    else if(a.equals(PotionEffectType.WATER_BREATHING)) type = "WATER_BREATHING";
                                    else if(a.equals(PotionEffectType.WEAKNESS)) type = "WEAKNESS";
                                    else type = "AWKWARD";
                                    item = UMaterial.valueOf((isSplash ? "SPLASH_" : "") + "POTION_" + type).getItemStack();
                                }
                            }
                            itemMeta = item.getItemMeta();
                            itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                            final String n = yml.getString("items." + s + ".name");
                            if(n != null) itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', n));
                            itemMeta.setLore(lore); lore.clear();
                            item.setItemMeta(itemMeta);
                            final CustomPotion cp = new CustomPotion(item.clone(), effects);
                        }
                        final String op = yml.getString("items." + s + ".opens");
                        if(op == null || op.isEmpty())
                            purchases.get(o).put(slot, item.clone());
                        else
                            opens.get(o).put(slot, op);
                        itemMeta = item.getItemMeta(); lore.clear();
                        if(itemMeta.hasLore()) lore.addAll(itemMeta.getLore());
                        double bp = 0.00, sp = 0.00;
                        if(op == null && prices != null) {
                            bp = Double.parseDouble(prices.split(";")[0]);
                            sp = Double.parseDouble(prices.split(";")[1]);
                            for(String l : shops.getStringList("lores.shop lore")) {
                                if(l.contains("{BUY}")) {
                                    if(bp <= 0.00) {
                                        for(String q : notbuyable) lore.add(ChatColor.translateAlternateColorCodes('&', q));
                                    } else {
                                        l = l.replace("{BUY}", Double.toString(bp));
                                    }
                                }
                                if(l.contains("{SELL}")) {
                                    if(sp <= 0.00) {
                                        for(String q : notsellable) lore.add(ChatColor.translateAlternateColorCodes('&', q));
                                    } else {
                                        l = l.replace("{SELL}", Double.toString(sp));
                                    }
                                }
                                if(!l.contains("{BUY}") && !l.contains("{SELL}"))
                                    lore.add(ChatColor.translateAlternateColorCodes('&', l));
                            }
                        }
                        itemMeta.setLore(lore); lore.clear();
                        item.setItemMeta(itemMeta);
                        i.setItem(slot, item);
                        final ItemStack purchase = shops.get("items." + s + ".purchase") == null ? purchases.get(o).get(slot) : d(shops, "items." + s + "purchase");
                        new MerchantItem(o, s, slot, op, bp, sp, item, purchase, yml.getStringList("items." + s + ".commands"));
                    }
                }
                customInventories.put(o, ui);
            }
        }
    }

    public void load() {
        treemap = new TreeMap<>();
        treemap.put(1000, "M"); treemap.put(900, "CM"); treemap.put(500, "D"); treemap.put(400, "CD"); treemap.put(100, "C"); treemap.put(90, "XC");
        treemap.put(50, "L"); treemap.put(40, "XL"); treemap.put(10, "X"); treemap.put(9, "IX"); treemap.put(5, "V"); treemap.put(4, "IV"); treemap.put(1, "I");

        citizens = pluginmanager.isPluginEnabled("Citizens");
        previousShop = new HashMap<>();
        isPurchasing = new HashMap<>();
        isSelling = new HashMap<>();

        reloadMerchants();
        loadMerchants();

        customInventories = new HashMap<>();
        opens = new HashMap<>();
        purchases = new HashMap<>();

        bTypeSlots = new HashMap<>();
        sTypeSlots = new HashMap<>();
        purchaseDisplayItem = new ArrayList<>();
        sellDisplayItem = new ArrayList<>();
    }
    public void unload() {
        saveMerchants();
        final List<String> r = config.getStringList("messages.close due to reload");
        for(Player player : isPurchasing.keySet()) {
            sendStringListMessage(player, r, null);
            player.closeInventory();
        }
        for(Player player : isSelling.keySet()) {
            sendStringListMessage(player, r, null);
            player.closeInventory();
        }
    }
    @EventHandler
    private void inventoryCloseEvent(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        isPurchasing.remove(player);
        isSelling.remove(player);
        previousShop.remove(player);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void inventoryClickEvent(InventoryClickEvent event) {
        if(event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() == event.getWhoClicked()) {
            final Player player = (Player) event.getWhoClicked();
            final Inventory top = player.getOpenInventory().getTopInventory();
            final int r = event.getRawSlot();
            final ItemStack current = event.getCurrentItem();
            if(isPurchasing.containsKey(player) || isSelling.containsKey(player)) {
                final boolean isBuying = isPurchasing.containsKey(player);
                event.setCancelled(true);
                player.updateInventory();
                final String c = event.getClick().name();

                if(r < 0 || r >= top.getSize() || !c.contains("LEFT") && !c.contains("RIGHT") || current == null || current.getType().equals(Material.AIR) || isBuying && purchaseDisplayItem.contains(r) || !isBuying && sellDisplayItem.contains(r)) return;
                List<String> message = new ArrayList<>();
                final HashMap<String, String> replacements = new HashMap<>();

                final MerchantItem mi = (isBuying ? isPurchasing : isSelling).get(player);
                final String O = mi.opens;
                if(O != null) {
                    viewInventory(player, O.equals("PREVIOUS_SHOP") ? previousShop.getOrDefault(player, O) : O);
                    return;
                }
                ItemStack i = mi.getPurchase();
                final PlayerInventory PI = player.getInventory();
                int max = 0, inv = 0;
                if(i != null) {
                    max = i.getMaxStackSize();
                    inv = isBuying ? getAvailableAmount(PI, i) : getAmount(PI, i);
                }
                int amount = 1;
                if(isBuying) {
                    amount = bTypeSlots.get(1).contains(r) ? 1 : bTypeSlots.get(2).contains(r) ? max : bTypeSlots.get(3).contains(r) ? inv : 0;
                } else {
                    amount = sTypeSlots.get(1).contains(r) ? 1 : sTypeSlots.get(2).contains(r) ? max : sTypeSlots.get(3).contains(r) ? inv : 0;
                }
                final double p = isBuying ? mi.buyPrice : mi.sellPrice, cost = round(p*amount, 2);
                replacements.put("{BUY}", Double.toString(p));
                replacements.put("{SELL}", Double.toString(p));
                replacements.put("{AMOUNT}", Integer.toString(amount));
                replacements.put("{COST}", Double.toString(cost));
                if(i != null) replacements.put("{ITEM}", i.getType().name());
                boolean closeInv = true;
                if(isBuying) {
                    if(event.getCurrentItem().equals(purchaseCancel)) {
                        message = config.getStringList("messages.purchase cancelled");
                    } else if(Vault.economy.withdrawPlayer(player, cost).transactionSuccess()) {
                        closeInv = closeInvUponSuccessfulPurchase;
                        final List<String> cmds = mi.executedCommands;
                        final CommandSender cs = Bukkit.getConsoleSender();
                        final String n = player.getName();
                        if(cmds != null && !cmds.isEmpty()) {
                            message = config.getStringList("messages.purchase success commands");
                            for(int z = 1; z <= amount; z++)
                                for(String s : cmds)
                                    Bukkit.getServer().dispatchCommand(cs, s.replaceFirst("/", "").replace("{PLAYER}", n));
                        } else if(i != null) {
                            message = config.getStringList("messages.purchase success");
                            giveItem(player, i, amount);
                        }
                    } else {
                        message = config.getStringList("messages.purchase incomplete");
                    }
                    if(closeInv) isPurchasing.remove(player);
                } else {
                    if(current.equals(sellCancel)) {
                        message = config.getStringList("messages.sell cancelled");
                    } else if(amount > 0 && player.getInventory().containsAtLeast(i, amount)) {
                        message = config.getStringList("messages.sell success");
                        removeItem(player, i, amount);
                        Vault.economy.depositPlayer(player, cost);
                        closeInv = closeInvUponSuccessfulSell;
                    } else {
                        message = config.getStringList("messages.sell incomplete");
                    }
                    if(closeInv) isSelling.remove(player);
                }
                if(closeInv) player.closeInventory();
                sendStringListMessage(player, message, replacements);
            } else {
                final String t = event.getView().getTitle();
                for(String s : customInventories.keySet()) {
                    final String title = customInventories.get(s).getTitle();
                    if(t.equals(title)) {
                        event.setCancelled(true);
                        player.updateInventory();
                        final String c = event.getClick().name();
                        if(r < 0 || r >= top.getSize() || !c.contains("LEFT") && !c.contains("RIGHT") || current == null || current.getType().equals(Material.AIR)) return;
                        final String previous = previousShop.getOrDefault(player, null);
                        player.closeInventory();
                        final HashMap<Integer, String> o = opens.get(s);
                        if(o.containsKey(r)) {
                            final String target = o.get(r);
                            viewInventory(player, target.equals("PREVIOUS_SHOP") ? previous : target);
                        } else {
                            final MerchantItem mi = MerchantItem.valueOf(s, r);
                            if(c.contains("LEFT")) {
                                openPurchaseView(player, mi);
                            } else {
                                openSellView(player, mi);
                            }
                        }
                        previousShop.put(player, getMerchant(title));
                        return;
                    }
                }
            }
        }
    }
    private String getMerchant(String title) {
        for(String s : customInventories.keySet()) {
            if(customInventories.get(s).getTitle().equals(title)) {
                return s;
            }
        }
        return null;
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void potionSplashEvent(PotionSplashEvent event) {
        final CustomPotion cp = CustomPotion.valueOf(event.getEntity().getItem());
        if(cp != null) {
            final List<PotionEffect> pes = cp.getPotionEffects();
            for(LivingEntity e : event.getAffectedEntities()) {
                for(PotionEffect pe : pes) {
                    e.removePotionEffect(pe.getType());
                    e.addPotionEffect(pe);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void playerConsumeItemEvent(PlayerItemConsumeEvent event) {
        final ItemStack i = event.getItem();
        if(i.getType().name().contains("POTION") && i.hasItemMeta() && i.getItemMeta().hasLore() && i.getItemMeta().getItemFlags().contains(ItemFlag.HIDE_POTION_EFFECTS)) {
            final Player player = event.getPlayer();
            final CustomPotion cp = CustomPotion.valueOf(event.getItem());
            if(cp != null) {
                for(PotionEffect pe : cp.getPotionEffects()) {
                    player.removePotionEffect(pe.getType());
                    player.addPotionEffect(pe);
                }
                event.setCancelled(true);
                if(i.getAmount() == 1) {
                    player.setItemInHand(new ItemStack(Material.GLASS_BOTTLE, 1));
                } else {
                    item = event.getItem(); itemMeta = item.getItemMeta();
                    item.setAmount(item.getAmount() - 1);
                    player.setItemInHand(item);
                    player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE, 1));
                }
                player.updateInventory();
            }
        }
    }



    public void openPurchaseView(Player player, MerchantItem mi) {
        if(mi.buyPrice > 0.00) {
            open(player, mi, "RANDOMHASHTAGS");
            isPurchasing.put(player, mi);
        } else {
            sendStringListMessage(player, config.getStringList("messages.not buyable"), new HashMap<>());
        }
    }
    public void openSellView(Player player, MerchantItem mi) {
        if(mi.sellPrice > 0.00) {
            open(player, mi, "RANDONHASHTAGS");
            isSelling.put(player, mi);
        } else {
            sendStringListMessage(player, config.getStringList("messages.not sellable"), new HashMap<>());
        }
    }
    private void open(Player player, MerchantItem mi, String type) {
        final boolean isBuying = type.equals("RANDOMHASHTAGS");
        final UInventory inventory = isBuying ? purchaseInv : sellInv;
        final Inventory inve = inventory.getInventory();
        final HashMap<Integer, List<Integer>> typeslots = isBuying ? bTypeSlots : sTypeSlots;
        final List<Integer> displayitems = isBuying ? purchaseDisplayItem : sellDisplayItem;
        player.closeInventory();
        final ItemStack i = mi.getPurchase();
        player.openInventory(Bukkit.createInventory(player, inventory.getSize(), inventory.getTitle().replace("{ITEM}", i.getType().name())));
        player.getOpenInventory().getTopInventory().setContents(inve.getContents());

        for(int o : displayitems)
            player.getOpenInventory().getTopInventory().setItem(o, i);

        final double p = isBuying ? mi.buyPrice : mi.sellPrice;
        final PlayerInventory PI = player.getInventory();
        final int stack = i.getMaxStackSize(), inv = isBuying ? getAvailableAmount(PI, i) : getAmount(PI, i);
        final String cost1 = Double.toString(p), costStack = Double.toString(round(p*stack, 2)), costInv = Double.toString(round(p*inv, 2));
        for(int j : typeslots.keySet()) {
            for(int k : typeslots.get(j)) {
                item = player.getOpenInventory().getTopInventory().getItem(k);
                itemMeta = item.getItemMeta(); lore.clear();
                for(String s : itemMeta.getLore()) {
                    lore.add(s.replace("{QUANTITY}", Integer.toString(j == 1 ? 1 : j == 2 ? stack : inv)).replace("{COST}", j == 1 ? cost1 : j == 2 ? costStack : costInv));
                }
                itemMeta.setLore(lore); lore.clear();
                item.setItemMeta(itemMeta);
                player.getOpenInventory().setItem(k, item);
            }
        }
        player.updateInventory();
    }

    public void viewInventory(Player player, String key) {
        if(customInventories.containsKey(key)) {
            player.closeInventory();
            final UInventory i = customInventories.get(key);
            player.openInventory(Bukkit.createInventory(player, i.getSize(), i.getTitle()));
            player.getOpenInventory().getTopInventory().setContents(i.getInventory().getContents());
            player.updateInventory();
        }
    }

    public ItemStack d(FileConfiguration config, String path) {
        item = null;
        if(config == null && path != null || config != null && config.get(path + ".item") != null) {
            if(config != null && config.getString(path + ".item").toLowerCase().contains("spawner") && !config.getString(path + ".item").toLowerCase().startsWith("mob_spawner")) {
                item = UMaterial.SPAWNER.getItemStack(); itemMeta = item.getItemMeta();
                if(config.get(path + ".name") != null) {
                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(path + ".name")));
                    item.setItemMeta(itemMeta);
                }
                return item;
            }
            int amount = config != null && config.get(path + ".amount") != null ? config.getInt(path + ".amount") : 1;
            if(config == null && path.toLowerCase().contains(";amount=")) {
                amount = Integer.parseInt(path.toLowerCase().split(";amount=")[1]);
                path = path.split(";amount=")[0];
            }
            boolean enchanted = config != null && config.getBoolean(path + ".enchanted");
            lore.clear();
            String it = config != null ? config.getString(path + ".item").toUpperCase() : path, name = config != null ? config.getString(path + ".name") : null;
            final String material = it.toUpperCase();
            final UMaterial u;
            try {
                u = UMaterial.match(material);
                item = u.getItemStack();
            } catch (NullPointerException e) {
                Bukkit.broadcastMessage("Unrecognized material: " + material);
                return null;
            }
            final Material skullitem = UMaterial.PLAYER_HEAD_ITEM.getMaterial();
            item.setAmount(amount);
            itemMeta = item.getItemMeta();
            if(item.getType().equals(skullitem) && item.getData().getData() == 3) {
                ((SkullMeta) itemMeta).setOwner(it.split(":").length == 4 ? it.split(":")[3].split("}")[0] : "RandomHashTags");
            } else if(u.name().contains("SPAWN_EGG") && (v.contains("1.9") || v.contains("1.10") || v.contains("1.11") || v.contains("1.12"))) {
                ((org.bukkit.inventory.meta.SpawnEggMeta) itemMeta).setSpawnedType(EntityType.valueOf(u.name().split("_SPAWN_EGG")[0]));
            }
            itemMeta.setDisplayName(name != null ? ChatColor.translateAlternateColorCodes('&', name) : null);

            if(enchanted) itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            final HashMap<Enchantment, Integer> enchants = new HashMap<>();
            if(config != null && config.get(path + ".lore") != null) {
                lore.clear();
                for(String string : config.getStringList(path + ".lore")) {
                    if(string.toLowerCase().startsWith("e:"))
                        enchants.put(getEnchantment(string.split(":")[1]), getRemainingInt(string));
                    else if(string.toLowerCase().startsWith("venchants{")) {
                        for(String s : string.split("\\{")[1].split("}")[0].split(";")) {
                            enchants.put(getEnchantment(s), getRemainingInt(s));
                        }
                    } else
                        lore.add(ChatColor.translateAlternateColorCodes('&', string));
                }
            }
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            lore.clear();
            if(enchanted) item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
            for(Enchantment enchantment : enchants.keySet())
                if(enchantment != null)
                    item.addUnsafeEnchantment(enchantment, enchants.get(enchantment));
        }
        return item;
    }
    /*
     * This code is from "bhlangonijr" at
     * https://stackoverflow.com/questions/12967896
     */
    public final String toRoman(int number) {
        if(number <= 0) return "";
        int l = treemap.floorKey(number);
        if(number == l) return treemap.get(number);
        return treemap.get(l) + toRoman(number - l);
    }
}
