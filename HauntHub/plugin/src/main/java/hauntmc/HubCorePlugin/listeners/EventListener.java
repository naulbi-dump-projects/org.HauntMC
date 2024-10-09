package hauntmc.HubCorePlugin.listeners;

import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import org.bukkit.event.block.*;
import org.bukkit.event.world.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.enchantment.*;
import hauntmc.HubCorePlugin.utils.config.*;

public class EventListener implements Listener {

    private DataConfig dataConfig;

    public EventListener(DataConfig dataConfig) {
        this.dataConfig = dataConfig;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(PlayerChatEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent e) {
        e.setDropItems(false);
        e.setExpToDrop(0);
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvil(PrepareAnvilEvent e) { e.setResult(null); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerBed(PlayerBedEnterEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDispense(BlockDispenseEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFade(BlockFadeEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPiston(BlockPistonExtendEvent e) {  e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPiston(BlockPistonRetractEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreakBlock(BlockBreakEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucket(PlayerBucketEmptyEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucket(PlayerBucketFillEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBurn(BlockBurnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCraftItem(CraftItemEvent e) {
        e.setResult(null);
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(PlayerItemDamageEvent e) {
        e.setCancelled(true);
        e.setDamage(0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByBlockEvent e) {
        if(e.getEntity() instanceof Player) {
            e.setDamage(0);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Player) {
            e.setCancelled(true);
            e.setDamage(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player) {
            e.setCancelled(true);
            e.setDamage(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPortal(EntityPortalEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatePortal(PortalCreateEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent e) {
        e.getBlock().setType(Material.AIR);
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDrop(PlayerDropItemEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEggThrow(PlayerEggThrowEvent e) {
        e.setHatching(false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnchantItem(EnchantItemEvent e) {
        if(e.getEnchanter() != null && e.getItem() != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnchantItem(PrepareItemEnchantEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onExpChange(PlayerExpChangeEvent e) {
        e.setAmount(0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onExplode(BlockExplodeEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFailTeleport(PlayerMoveEvent e) { // todo: replaced event (now optimization none)
        if(e.getTo() == null) return;
        if(e.getFrom() == null) return;
        if(e.getPlayer() == null) return;

        Location to = e.getTo();
        Location from = e.getFrom();
        if(to.getY() <= -1 || from.getY() <= -1 || to.getY() > 255 || from.getY() > 255) {
            e.getPlayer().teleport(dataConfig.spawn);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFish(PlayerFishEvent e) {
        e.setExpToDrop(0);
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodChange(FoodLevelChangeEvent e) {
        if(e.getEntity() instanceof Player) {
            e.setFoodLevel(20);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFromTo(BlockFromToEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFurnace(FurnaceBurnEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFurnace(FurnaceExtractEvent e) { e.setExpToDrop(0); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFurnace(FurnaceSmeltEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGrow(BlockGrowEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        e.setJoinMessage(null);

        p.teleport(dataConfig.spawn);
        dataConfig.joinEffects.forEach(effects -> {
            String[] effect = effects.split(":");
            PotionEffectType effectType = PotionEffectType.getByName(effect[0].toUpperCase());
            PotionEffect potionEffect = new PotionEffect(
                    effectType,
                    (effect[2] == null ? 9999999 : Integer.parseInt(effect[2])),
                    (effect[1] == null ? 1 : Integer.parseInt(effect[1]))
            );
            p.addPotionEffect(potionEffect, false);
        });


        if(p.hasPlayedBefore()) {
            p.sendMessage(dataConfig.joinMessagePlayedBefore);
            p.sendTitle(dataConfig.joinTitlePlayedBefore, dataConfig.joinSubtitlePlayedBefore);
            return;
        }
        p.sendMessage(dataConfig.joinMessageFirstPlay);
        p.sendTitle(dataConfig.joinTitleFirstPlay, dataConfig.joinSubtitleFirstPlay);
        p.setLevel(2021);
        p.setMaxHealth(0.5D);
        p.setBedSpawnLocation(dataConfig.spawn);
        if(!p.getGameMode().equals(GameMode.ADVENTURE)) p.setGameMode(GameMode.ADVENTURE);

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKick(PlayerKickEvent e) {
        e.setLeaveMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeavesDecay(LeavesDecayEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLevelChange(PlayerLevelChangeEvent e) { e.getPlayer().setLevel(e.getOldLevel()); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlaceBlock(BlockPlaceEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPortalCreate(PortalCreateEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onThunder(ThunderChangeEvent e) {
        if(!e.toThunderState()) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketFill(PlayerInteractEntityEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLightningStrike(LightningStrikeEvent e) { e.setCancelled(true); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWeatherChange(WeatherChangeEvent e) {
        if(!e.toWeatherState()) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent e) { // HideAdvancements
        e.getWorld().setGameRuleValue("announceAdvancements", "false");
    }

}