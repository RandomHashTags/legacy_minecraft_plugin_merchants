package me.randomhashtags.merchants;

import com.sun.istack.internal.NotNull;
import me.randomhashtags.merchants.addon.Merchant;
import me.randomhashtags.merchants.addon.MerchantItem;
import me.randomhashtags.merchants.addon.file.FileMerchant;
import me.randomhashtags.merchants.addon.obj.CustomPotion;
import me.randomhashtags.merchants.supported.FactionsAPI;
import me.randomhashtags.merchants.supported.economy.Vault;
import me.randomhashtags.merchants.universal.UInventory;
import me.randomhashtags.merchants.universal.UMaterial;
import me.randomhashtags.merchants.util.MFeature;
import me.randomhashtags.merchants.util.MerchantStorage;
import me.randomhashtags.merchants.util.OpenType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.util.*;

import static java.io.File.separator;

public class MerchantsAPI extends MFeature implements Listener, CommandExecutor {
    private static MerchantsAPI instance;
    public static MerchantsAPI getAPI() {
        if(instance == null) instance = new MerchantsAPI();
        return instance;
    }

    private boolean citizens = false, closeInvUponSuccessfulPurchase = true, closeInvUponSuccessfulSell = true;

    private HashMap<Player, Merchant> previousShop;
    private HashMap<Player, MerchantItem> isPurchasing, isSelling;

    private final FactionsAPI fapi = FactionsAPI.getFactionsAPI();

    private File dataF;
    public YamlConfiguration data, shops;
    private final FileConfiguration config = merchants.getConfig();
    private UInventory purchaseInv, sellInv;
    private ItemStack purchaseCancel, purchaseOne, purchaseStack, purchaseInventory, sellCancel, sellOne, sellStack, sellInventory;

    private List<Integer> purchaseDisplayItem, sellDisplayItem;

    private HashMap<Integer, List<Integer>> buyAmountSlots, sellAmountSlots;
    private HashMap<String, Merchant> merchantCommandIds;
    private HashMap<UUID, Merchant> livingMerchants;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final String m = event.getMessage().substring(1), tl = m.toLowerCase(), target = tl.split(" ")[0];
        final Merchant merchant = merchantCommandIds.getOrDefault(target, null);
        if(merchant != null) {
            event.setCancelled(true);
            final String cmdp = merchant.getCommandPermission();
            if(tl.equals(m)) {
                if(merchant.isAccessibleFromCommand() && (cmdp == null || player.hasPermission(cmdp))) {
                    viewInventory(player, merchant);
                } else {
                    player.sendMessage("[Merchants] This Merchant is not accessible from its command!");
                }
            } else if(tl.startsWith(m + " spawn ") && (cmdp == null || hasPermission(player, cmdp, false))) {
                if(citizens) {
                    final int y = (tl + " spawn ").length();
                    merchant.spawn(player.getLocation(), colorize(m.substring(y)));
                } else {
                    player.sendMessage("[Merchants] You need the Citizens plugin installed to do this!");
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
                sender.sendMessage(colorize("&6[Merchants] &aMerchants &ev" + ve + " &areloaded."));
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

    public void saveMerchants() {
        if(data == null) {
            dataF = new File(dataFolder + separator + "_data.yml");
            data = YamlConfiguration.loadConfiguration(dataF);
        }
        try {
            data.set("living", null);
            for(UUID u : livingMerchants.keySet()) {
                data.set("living." + u.toString(), livingMerchants.get(u).getIdentifier());
            }
            saveData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Merchant getLivingMerchant(UUID uuid) {
        return livingMerchants.getOrDefault(uuid, null);
    }
    public void despawn(UUID merchant) {
        livingMerchants.remove(merchant);
    }

    private void saveData() {
        try {
            data.save(dataF);
            data = YamlConfiguration.loadConfiguration(dataF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void load() {
        checkForUpdate();
        save(null, "_data.yml");
        dataF = new File(dataFolder + separator, "_data.yml");
        data = YamlConfiguration.loadConfiguration(dataF);
        if(data.getBoolean("save default shops")) {
            final String[] shops = new String[] {
                    "BASE", "BREWING", "COLOR", "DECOR",
                    "ELIXIR", "ELIXIRS", "ENCHANTER", "FARMING", "FISH",
                    "HOPPER", "MISC", "MOB", "ORE", "POTIONS",
                    "RAID"
            };
            for(String s : shops) {
                save("shops", s + ".yml");
            }
            data.set("save default shops", false);
            saveData();
        }

        save(null, "shops.yml");
        shops = YamlConfiguration.loadConfiguration(new File(dataFolder + separator, "shops.yml"));

        citizens = pluginmanager.isPluginEnabled("Citizens");
        previousShop = new HashMap<>();
        isPurchasing = new HashMap<>();
        isSelling = new HashMap<>();

        purchaseDisplayItem = new ArrayList<>();
        sellDisplayItem = new ArrayList<>();

        citizens = pluginmanager.isPluginEnabled("Citizens");
        closeInvUponSuccessfulPurchase = shops.getBoolean("close inventory upon.successful purchase");
        closeInvUponSuccessfulSell = shops.getBoolean("close inventory upon.successful sell");
        purchaseInv = new UInventory(null, shops.getInt("purchase.size"), colorize(shops.getString("purchase.title")));
        purchaseCancel = d(shops, "purchase.cancel");
        purchaseOne = d(shops, "purchase.one");
        purchaseStack = d(shops, "purchase.stack");
        purchaseInventory = d(shops, "purchase.inventory");

        sellInv = new UInventory(null, shops.getInt("sell.size"), colorize(shops.getString("sell.title")));
        sellCancel = d(shops, "sell.cancel");
        sellOne = d(shops, "sell.one");
        sellStack = d(shops, "sell.stack");
        sellInventory = d(shops, "sell.inventory");

        buyAmountSlots = new HashMap<>();
        sellAmountSlots = new HashMap<>();

        for(int i = 1; i <= 2; i++) {
            final boolean isPurchase = i == 1;
            final String type = isPurchase ? "purchase" : "sell";
            final Inventory inventory = (isPurchase ? purchaseInv : sellInv).getInventory();
            final ItemStack cancel = isPurchase ? purchaseCancel : sellCancel, one = isPurchase ? purchaseOne : sellOne, stack = isPurchase ? purchaseStack : sellStack, inv = isPurchase ? purchaseInventory : sellInventory;
            final HashMap<Integer, List<Integer>> h = isPurchase ? buyAmountSlots : sellAmountSlots;
            final List<Integer> displayitems = isPurchase ? purchaseDisplayItem : sellDisplayItem;
            for(String s : shops.getConfigurationSection(type).getKeys(false)) {
                if(!s.equals("title") && !s.equals("size") && !s.equals("cancel") && !s.equals("one") && !s.equals("stack") && !s.equals("inventory")) {
                    final String ii = shops.getString(type + "." + s + ".item");
                    if(ii != null) {
                        final int slot = shops.getInt(type + "." + s + ".slot");
                        if(ii.equals("{ITEM}")) {
                            displayitems.add(slot);
                        } else if(ii.startsWith(type)) {
                            final int t = ii.contains("one") ? 1 : ii.contains("stack") ? 2 : 3;
                            if(!h.containsKey(t)) {
                                h.put(t, new ArrayList<>());
                            }
                            h.get(t).add(slot);
                            inventory.setItem(slot, (t == 1 ? one : t == 2 ? stack : inv).clone());
                        } else {
                            inventory.setItem(slot, ii.equals("cancel") ? cancel.clone() : d(shops, type + "." + s));
                        }
                    }
                }
            }
        }
        final List<String> notbuyable = colorizeListString(shops.getStringList("lores.not buyable")), notsellable = colorizeListString(shops.getStringList("lores.not sellable")), shoplore = colorizeListString(shops.getStringList("lores.shop lore"));

        merchantCommandIds = new HashMap<>();
        livingMerchants = new HashMap<>();
        for(File f : new File(dataFolder + separator + "shops").listFiles()) {
            final Merchant m = new FileMerchant(f);
            merchantCommandIds.put(m.getCommand(), m);
        }

        final ConfigurationSection cs = data.getConfigurationSection("living");
        final Set<String> set = cs != null ? cs.getKeys(false) : null;
        if(set != null) {
            for(String merchant : set) {
                final Merchant m = MerchantStorage.getMerchant(data.getString("living." + merchant));
                livingMerchants.put(UUID.fromString(merchant), m);
            }
        }
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
        final Player player = (Player) event.getWhoClicked();
        if(player.getOpenInventory().getTopInventory().getHolder() == player) {
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
                final String opensMerchant = mi.getOpensMerchant();
                if(opensMerchant != null) {
                    final Merchant target = MerchantStorage.getMerchant(opensMerchant);
                    viewInventory(player, opensMerchant.equals("PREVIOUS_SHOP") ? previousShop.getOrDefault(player, target) : target);
                    return;
                }
                ItemStack i = mi.getPurchased();
                final PlayerInventory playerInv = player.getInventory();
                int max = 0, inv = 0;
                if(i != null) {
                    max = i.getMaxStackSize();
                    inv = isBuying ? getAvailableAmount(playerInv, i) : getAmount(playerInv, i);
                }
                final int amount = (isBuying ? buyAmountSlots : sellAmountSlots).get(1).contains(r) ? 1 : (isBuying ? buyAmountSlots : sellAmountSlots).get(2).contains(r) ? max : (isBuying ? buyAmountSlots : sellAmountSlots).get(3).contains(r) ? inv : 0;;
                final double p = (isBuying ? mi.getBuyPrice() : mi.getSellPrice()).doubleValue(), cost = round(p*amount, 2);
                replacements.put("{BUY}", Double.toString(p));
                replacements.put("{SELL}", Double.toString(p));
                replacements.put("{AMOUNT}", Integer.toString(amount));
                replacements.put("{COST}", Double.toString(cost));
                if(i != null) replacements.put("{ITEM}", i.getType().name());
                boolean closeInv = true;
                if(isBuying) {
                    if(current.equals(purchaseCancel)) {
                        message = config.getStringList("messages.purchase cancelled");
                    } else if(Vault.economy.withdrawPlayer(player, cost).transactionSuccess()) {
                        closeInv = closeInvUponSuccessfulPurchase;
                        final List<String> cmds = mi.getExecutedCommands();
                        final String n = player.getName();
                        if(cmds != null && !cmds.isEmpty()) {
                            message = config.getStringList("messages.purchase success commands");
                            for(int z = 1; z <= amount; z++) {
                                for(String s : cmds) {
                                    server.dispatchCommand(console, s.replaceFirst("/", "").replace("{PLAYER}", n));
                                }
                            }
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
                    } else if(amount > 0 && playerInv.containsAtLeast(i, amount)) {
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
                final Merchant merchant = getMerchant(t);
                if(merchant != null) {
                    event.setCancelled(true);
                    player.updateInventory();
                    final String c = event.getClick().name();
                    if(r < 0 || r >= top.getSize() || !c.contains("LEFT") && !c.contains("RIGHT") || current == null || current.getType().equals(Material.AIR)) return;
                    final Merchant previous = previousShop.getOrDefault(player, null);
                    player.closeInventory();
                    final HashMap<Integer, MerchantItem> merchantItems = merchant.getPages().get(1);
                    final MerchantItem mi = merchantItems.get(r);
                    final String target = mi.getOpensMerchant();
                    if(target != null) {
                        viewInventory(player, target.equals("PREVIOUS_SHOP") ? previous : MerchantStorage.getMerchant(target));
                    } else {
                        if(c.contains("LEFT")) {
                            openPurchaseView(player, mi);
                        } else {
                            openSellView(player, mi);
                        }
                    }
                    previousShop.put(player, merchant);
                }
            }
        }
    }
    private Merchant getMerchant(@NotNull String title) {
        for(Merchant m : MerchantStorage.MERCHANTS.values()) {
            if(m.getInventory().getTitle().equals(title)) {
                return m;
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
            final CustomPotion cp = CustomPotion.valueOf(i);
            if(cp != null) {
                for(PotionEffect pe : cp.getPotionEffects()) {
                    player.removePotionEffect(pe.getType());
                    player.addPotionEffect(pe);
                }
                event.setCancelled(true);
                if(i.getAmount() == 1) {
                    player.setItemInHand(new ItemStack(Material.GLASS_BOTTLE, 1));
                } else {
                    item = i; itemMeta = item.getItemMeta();
                    item.setAmount(item.getAmount() - 1);
                    player.setItemInHand(item);
                    player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE, 1));
                }
                player.updateInventory();
            }
        }
    }

    public void openPurchaseView(Player player, MerchantItem mi) {
        if(mi.getBuyPrice().doubleValue() > 0.00) {
            open(player, mi, OpenType.BUYING);
            isPurchasing.put(player, mi);
        } else {
            sendStringListMessage(player, config.getStringList("messages.not buyable"), new HashMap<>());
        }
    }
    public void openSellView(Player player, MerchantItem mi) {
        if(mi.getSellPrice().doubleValue() > 0.00) {
            open(player, mi, OpenType.SELLING);
            isSelling.put(player, mi);
        } else {
            sendStringListMessage(player, config.getStringList("messages.not sellable"), new HashMap<>());
        }
    }
    private void open(Player player, MerchantItem mi, OpenType type) {
        final boolean isBuying = type.equals(OpenType.BUYING);
        final UInventory inventory = isBuying ? purchaseInv : sellInv;
        final Inventory inve = inventory.getInventory();
        final HashMap<Integer, List<Integer>> typeslots = isBuying ? buyAmountSlots : sellAmountSlots;
        final List<Integer> displayitems = isBuying ? purchaseDisplayItem : sellDisplayItem;
        player.closeInventory();
        final ItemStack i = mi.getPurchased();
        player.openInventory(Bukkit.createInventory(player, inventory.getSize(), inventory.getTitle().replace("{ITEM}", i.getType().name())));
        final Inventory top = player.getOpenInventory().getTopInventory();
        top.setContents(inve.getContents());

        for(int o : displayitems) {
            top.setItem(o, i);
        }

        final double p = (isBuying ? mi.getBuyPrice() : mi.getSellPrice()).doubleValue();
        final PlayerInventory PI = player.getInventory();
        final int stack = i.getMaxStackSize(), inv = isBuying ? getAvailableAmount(PI, i) : getAmount(PI, i);
        final String stackString = Integer.toString(stack), invString = Integer.toString(inv);
        final String cost1 = Double.toString(p), costStack = Double.toString(round(p*stack, 2)), costInv = Double.toString(round(p*inv, 2));
        for(int j : typeslots.keySet()) {
            for(int k : typeslots.get(j)) {
                item = top.getItem(k);
                itemMeta = item.getItemMeta(); lore.clear();
                for(String s : itemMeta.getLore()) {
                    lore.add(s.replace("{QUANTITY}", j == 1 ? "1" : j == 2 ? stackString : invString).replace("{COST}", j == 1 ? cost1 : j == 2 ? costStack : costInv));
                }
                itemMeta.setLore(lore); lore.clear();
                item.setItemMeta(itemMeta);
                top.setItem(k, item);
            }
        }
        player.updateInventory();
    }

    public void viewInventory(@NotNull Player player, @NotNull Merchant merchant) {
        player.closeInventory();
        final UInventory i = merchant.getInventory();
        player.openInventory(Bukkit.createInventory(player, i.getSize(), i.getTitle()));
        player.getOpenInventory().getTopInventory().setContents(i.getInventory().getContents());
        player.updateInventory();
    }

    private void sellchest(Player player, Inventory inv, String[] type) {
        final HashMap<UMaterial, Integer> amounts = new HashMap<>();
        for(int i = 0; i < inv.getSize(); i++) {
            final ItemStack is = inv.getItem(i);
            if(is != null && !is.getType().equals(Material.AIR)) {
                final MerchantItem m = FileMerchant.valueOf(is);
                final double sp = m != null ? m.getSellPrice().doubleValue() : 0.00;
                final byte d = is.getData().getData();
                final String mat = type != null ? type[0].toUpperCase() : null, ism = is.getType().name();
                final UMaterial um = UMaterial.match(is);
                final String umn = um != null ? um.name() : null;
                if(m != null && sp != 0.00 && (type == null || type.length == 1 && (mat.equals(ism) || mat.equals(umn)) || (mat.equals(ism) || mat.equals(umn)) && d == Byte.parseByte(type[1]))) {
                    final int amount = is.getAmount();
                    if(!amounts.containsKey(um)) {
                        amounts.put(um, amount);
                    } else {
                        amounts.put(um, amounts.get(um)+amount);
                    }
                    inv.setItem(i, new ItemStack(Material.AIR));
                }
            }
        }
        for(UMaterial m : amounts.keySet()) {
            final int amount = amounts.get(m);
            final MerchantItem mi = FileMerchant.valueOf(m.getItemStack());
            final double sp = mi.getSellPrice().doubleValue();
            final double cost = round(amount*sp, 2);
            final HashMap<String, String> replacements = new HashMap<>();
            replacements.put("{SELL}", formatDouble(sp));
            replacements.put("{AMOUNT}", formatInt(amount));
            replacements.put("{COST}", formatDouble(cost));
            replacements.put("{ITEM}", m.name());
            Vault.economy.depositPlayer(player, cost);
            sendStringListMessage(player, config.getStringList("messages.sell success"), replacements);
        }
    }
    public void sellItems(Player player, ItemStack i, boolean inventory) {
        if(i == null || i.getType().equals(Material.AIR)) {
            sendStringListMessage(player, config.getStringList("messages.need to be holding item"), null);
        } else {
            final MerchantItem m = FileMerchant.valueOf(i);
            final List<String> msg = config.getStringList("messages.cannot sell item to server");
            if(m == null) {
                sendStringListMessage(player, msg, null);
            } else {
                final ItemStack is = m.getPurchased();
                final String it = is.hasItemMeta() && is.getItemMeta().hasDisplayName() ? is.getItemMeta().getDisplayName() : UMaterial.match(is).name();
                final double s = m.getSellPrice().doubleValue();
                if(i.isSimilar(is)) {
                    final HashMap<String, String> replacements = new HashMap<>();
                    replacements.put("{ITEM}", it);
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
                    Vault.economy.depositPlayer(player, total);
                    sendStringListMessage(player, config.getStringList("messages.sell success"), replacements);
                    player.updateInventory();
                } else {
                    sendStringListMessage(player, msg, null);
                }
            }
        }
    }
}
