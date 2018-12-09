package org.mswsplex.testserver.utils;

import org.bukkit.Material;

public enum WorldType {
	SANDSTONE("Sandstone", Material.SANDSTONE, Material.SAND, Material.SAND, Material.SAND, Material.SAND,
			Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND,
			Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND,
			Material.SAND, Material.SAND, Material.SAND, Material.BEDROCK),
	ORES("Ores", Material.COAL_ORE, Material.IRON_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE, Material.QUARTZ_ORE, Material.EMERALD_ORE, Material.DIAMOND_ORE);
	private String name;
	private Material[] layers;

	WorldType(String name, Material... layers) {
		this.name = name;
		this.layers = layers;
	}

	public Material[] getLayers() {
		return layers;
	}

	@Override
	public String toString() {
		return name;
	}
}
