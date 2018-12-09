package org.mswsplex.testserver.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.mswsplex.testserver.utils.MSG;

public class CustomChunkGenerator extends ChunkGenerator {
	private Material[] layers;

	public CustomChunkGenerator(Material... layers) {
		List<Material> list = new ArrayList<>();
		for (int i = 0; i < layers.length; i++) {
			if (layers[i] == null)
				continue;
			list.add(layers[i]);
		}

		this.layers = new Material[list.size()];
		this.layers = list.toArray(this.layers);
		ArrayUtils.reverse(this.layers);

		MSG.log("Initialized new chunk generator, reading layers");
		MSG.log("--- START OF LAYERS ---");
		for (Material mat : this.layers)
			MSG.log(MSG.camelCase(mat + ""));
		MSG.log("--- END OF LAYERS ---");
	}

	@Override
	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
		ChunkData chunk = createChunkData(world);
		world.setBiome(chunkX, chunkZ, Biome.PLAINS);
		for (int x = 0; x < 16; x++)
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < this.layers.length; y++) {
					chunk.setBlock(x, y, z, layers[y]);
				}
			}
		return chunk;
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return Arrays.asList();
	}
}
