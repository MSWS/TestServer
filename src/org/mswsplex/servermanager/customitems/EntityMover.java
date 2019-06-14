package org.mswsplex.servermanager.customitems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mswsplex.servermanager.utils.MSG;
import org.mswsplex.servermanager.utils.ParticleEffect;

public class EntityMover extends CustomItem {

	private Map<UUID, List<Entity>> controls;
	private Map<UUID, Double> dist;

	public EntityMover(JavaPlugin plugin) {
		super(plugin);

		controls = new HashMap<UUID, List<Entity>>();
		dist = new HashMap<UUID, Double>();

		new BukkitRunnable() {

			@Override
			public void run() {
				for (UUID uuid : controls.keySet()) {
					Player player = Bukkit.getPlayer(uuid);
					if (player == null || !player.isOnline())
						continue;
					for (Entity ent : controls.get(uuid)) {
						Vector offset = player.getLocation().getDirection().normalize()
								.multiply(dist.get(ent.getUniqueId()));
						ent.teleport(player.getEyeLocation().add(offset));
						ent.setVelocity(new Vector(0, 0, 0));
						ent.setFallDistance(0);

						for (double i = 0; i <= Math.PI; i += Math.PI / 5) {
							double radius = Math.sin(i);
							double y = Math.cos(i);
							for (double a = 0; a < Math.PI * 2; a += Math.PI / 5) {
								double x = Math.cos(a) * radius;
								double z = Math.sin(a) * radius;
								Location loc = ent.getLocation().clone();
								loc.add(x, y, z);

								ParticleEffect.FLAME.display(new Vector(), 0, loc, 500);
							}
						}

					}
				}
				controls.keySet()
						.removeIf(uuid -> Bukkit.getPlayer(uuid) == null || !Bukkit.getPlayer(uuid).isOnline());

			}
		}.runTaskTimer(plugin, 0, 1);
	}

	public double getAmp(double dist) {
		return Math.sqrt(dist);
	}

	@Override
	public String getName() {
		return "&d&lEntity Mover";
	}

	@Override
	public ItemStack getStack() {
		ItemStack stack = new ItemStack(Material.STICK);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(MSG.color(getName()));
		List<String> lore = new ArrayList<String>();
		lore.add(MSG.color("&eLeft-Click&7 - Grab entities"));
		lore.add(MSG.color("&eRight-Click&7 - Let go of entities"));
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!player.getItemInHand().isSimilar(getStack()))
			return;

		List<Entity> entities = player.getNearbyEntities(100, 100, 100).stream()
				.filter(ent -> isLookingAt(player, ent.getLocation())).collect(Collectors.toList());

		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			controls.remove(player.getUniqueId());
		} else {
			controls.put(player.getUniqueId(), entities);
			for (Entity ent : entities) {
				dist.put(ent.getUniqueId(), ent.getLocation().distance(player.getLocation()));
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onSlotHeldItemEvent(PlayerItemHeldEvent event) {
		if (Math.abs(event.getPreviousSlot() - event.getNewSlot()) > 1)
			return;

		boolean up = ((event.getPreviousSlot() > event.getNewSlot())
				|| (event.getPreviousSlot() == 0 && event.getNewSlot() == 8))
				&& !(event.getPreviousSlot() == 8 && event.getNewSlot() == 0);
		Player player = event.getPlayer();

		if (controls.containsKey(player.getUniqueId())) {
			for (Entity ent : controls.get(player.getUniqueId())) {
				double d = ent.getLocation().distance(player.getLocation()), change = getAmp(d);
				dist.put(ent.getUniqueId(), dist.get(ent.getUniqueId()) + (up ? change : -change));
			}
		}
	}

	private boolean isLookingAt(Player player, Location target) {
		Location eye = player.getEyeLocation();
		Vector toEntity = target.toVector().subtract(eye.toVector());
		double dot = toEntity.normalize().dot(eye.getDirection());

		return dot > 0.99D;
	}

	@Override
	public String getId() {
		return "entitymover";
	}

}
