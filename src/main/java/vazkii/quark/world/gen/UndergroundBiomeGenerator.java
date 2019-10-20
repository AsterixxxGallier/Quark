package vazkii.quark.world.gen;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.world.generator.MultiChunkFeatureGenerator;
import vazkii.quark.world.config.UndergroundBiomeConfig;

import java.util.*;

public class UndergroundBiomeGenerator extends MultiChunkFeatureGenerator {

	public final UndergroundBiomeConfig info;

	public UndergroundBiomeGenerator(UndergroundBiomeConfig info, Module module, String name) {
		super(info.dimensions, () -> module.enabled, name.hashCode());
		this.info = info;
	}

	@Override
	public int getFeatureRadius() {
		return info.horizontalSize + info.horizontalVariation;
	}

	@Override
	public void generateChunkPart(BlockPos src, ChunkGenerator<? extends GenerationSettings> generator, Random random, BlockPos chunkCorner, IWorld world) {
		int radiusX = info.horizontalSize + random.nextInt(info.horizontalVariation);
		int radiusY = info.verticalSize + random.nextInt(info.verticalVariation);
		int radiusZ = info.horizontalSize + random.nextInt(info.horizontalVariation);
		
		UndergroundBiomeGenerationContext context = new UndergroundBiomeGenerationContext(world, src, generator, random,
				info, radiusX, radiusY, radiusZ);
		apply(context, chunkCorner);
	}

	@Override
	public BlockPos[] getSourcesInChunk(Random random, ChunkGenerator<? extends GenerationSettings> generator, BlockPos chunkCorner) {
		if(info.rarity > 0 && random.nextInt(info.rarity) == 0) {
			return new BlockPos[] {
					chunkCorner.add(random.nextInt(16), info.minYLevel + random.nextInt(info.maxYLevel - info.minYLevel), random.nextInt(16))
			};
		}

		return new BlockPos[0];
	}

	@Override
	public boolean isSourceValid(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, BlockPos pos) {
		Biome biome = getBiome(generator, pos);
		return info.biomes.canSpawn(biome);
	}

	public void apply(UndergroundBiomeGenerationContext context, BlockPos chunkCorner) {
		int centerX = context.source.getX();
		int centerY = context.source.getY();
		int centerZ = context.source.getZ();

		double radiusX2 = context.radiusX * context.radiusX;
		double radiusY2 = context.radiusY * context.radiusY;
		double radiusZ2 = context.radiusZ * context.radiusZ;

		forEachChunkBlock(chunkCorner, centerY - context.radiusY, centerY + context.radiusY, (pos) -> {
			int x = pos.getX() - centerX;
			int y = pos.getY() - centerY;
			int z = pos.getZ() - centerZ;

			double distX = x * x;
			double distY = y * y;
			double distZ = z * z;
			double dist = distX / radiusX2 + distY / radiusY2 + distZ / radiusZ2;
			boolean inside = dist <= 1;

			if(inside)
				info.biomeObj.fill(context, pos);
		});

		context.floorList.forEach(pos -> info.biomeObj.finalFloorPass(context, pos));
		context.ceilingList.forEach(pos -> info.biomeObj.finalCeilingPass(context, pos));
		context.wallMap.keySet().forEach(pos -> info.biomeObj.finalWallPass(context, pos));
		context.insideList.forEach(pos -> info.biomeObj.finalInsidePass(context, pos));

		//		if(info.biome.hasDungeon() && world instanceof ServerWorld && random.nextDouble() < info.biome.dungeonChance) {
		//			List<BlockPos> candidates = new ArrayList<>(context.wallMap.keySet());
		//			candidates.removeIf(pos -> {
		//				BlockPos down = pos.down();
		//				BlockState state = world.getBlockState(down);
		//				return info.biome.isWall(world, down, state) || state.getBlock().isAir(state, world, down);
		//			});
		//
		//			if(!candidates.isEmpty()) {
		//				BlockPos pos = candidates.get(world.rand.nextInt(candidates.size()));
		//
		//				Direction border = context.wallMap.get(pos);
		//				if(border != null)
		//					info.biome.spawnDungeon((ServerWorld) world, pos, border);
		//			}
		//		}
	}
	
	@Override
	public String toString() {
		return "UndergroundBiomeGenerator[" + info.biomeObj + "]";
	}

	public static class UndergroundBiomeGenerationContext {

		public final IWorld world;
		public final BlockPos source;
		public final ChunkGenerator<? extends GenerationSettings> generator;
		public final Random random;
		public final UndergroundBiomeConfig info;
		public final int radiusX;
		public final int radiusY;
		public final int radiusZ;

		public final List<BlockPos> floorList = new LinkedList<>();
		public final List<BlockPos> ceilingList = new LinkedList<>();
		public final List<BlockPos> insideList = new LinkedList<>();

		public final Map<BlockPos, Direction> wallMap = new HashMap<>();
		
		public UndergroundBiomeGenerationContext(IWorld world, BlockPos source, ChunkGenerator<? extends GenerationSettings> generator,
												 Random random, UndergroundBiomeConfig info, int radiusX, int radiusY, int radiusZ) {
			this.world = world;
			this.source = source;
			this.generator = generator;
			this.random = random;
			this.info = info;
			this.radiusX = radiusX;
			this.radiusY = radiusY;
			this.radiusZ = radiusZ;
		}
		
	}
}