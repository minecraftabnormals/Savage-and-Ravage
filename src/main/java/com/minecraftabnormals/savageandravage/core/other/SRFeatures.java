package com.minecraftabnormals.savageandravage.core.other;

import com.google.common.collect.ImmutableList;
import com.minecraftabnormals.abnormals_core.common.world.modification.BiomeModificationManager;
import com.minecraftabnormals.abnormals_core.common.world.modification.BiomeModificationPredicates;
import com.minecraftabnormals.abnormals_core.common.world.modification.BiomeSpawnsModifier;
import com.minecraftabnormals.abnormals_core.core.util.DataUtil;
import com.minecraftabnormals.savageandravage.common.world.gen.feature.EnclosureFeature;
import com.minecraftabnormals.savageandravage.core.SavageAndRavage;
import com.minecraftabnormals.savageandravage.core.registry.SREntities;
import com.mojang.datafixers.util.Pair;
import com.sun.org.apache.regexp.internal.RE;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPatternRegistry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SRFeatures {
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, SavageAndRavage.MOD_ID);

	public static final RegistryObject<Feature<NoFeatureConfig>> CREEPER_ENCLOSURE = FEATURES.register("creeper_enclosure", () -> new EnclosureFeature(NoFeatureConfig.field_236558_a_));

	public static void registerPools() {
		JigsawPatternRegistry.func_244094_a(new JigsawPattern(new ResourceLocation(SavageAndRavage.MOD_ID, "enclosure/enclosures"), new ResourceLocation("empty"), ImmutableList.of(Pair.of(JigsawPiece.func_242845_a(CREEPER_ENCLOSURE.get().withConfiguration(NoFeatureConfig.field_236559_b_)), 1)), JigsawPattern.PlacementBehaviour.RIGID));
		JigsawPatternRegistry.func_244094_a(new JigsawPattern(new ResourceLocation(SavageAndRavage.MOD_ID, "pillager_outpost/pillagers"), new ResourceLocation("empty"), ImmutableList.of(Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/pillager"), 1)),  JigsawPattern.PlacementBehaviour.RIGID));
		JigsawPatternRegistry.func_244094_a(new JigsawPattern(new ResourceLocation(SavageAndRavage.MOD_ID, "pillager_outpost/vindicators"), new ResourceLocation("empty"), ImmutableList.of(Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/vindicator"), 1)),  JigsawPattern.PlacementBehaviour.RIGID));
		JigsawPatternRegistry.func_244094_a(new JigsawPattern(new ResourceLocation(SavageAndRavage.MOD_ID, "pillager_outpost/note_blocks"), new ResourceLocation("empty"), ImmutableList.of(
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block0"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block1"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block2"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block3"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block4"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block6"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block7"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block8"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block9"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block10"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block11"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block12"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block13"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block14"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block15"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block16"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block17"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block18"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block19"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block20"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block21"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block22"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block23"), 1),
				Pair.of(JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/note_blocks/note_block24"), 1)), JigsawPattern.PlacementBehaviour.RIGID));
		for (String biome : new String[]{"plains", "snowy", "savanna", "desert", "taiga"})
			DataUtil.addToJigsawPattern(new ResourceLocation("village/" + biome + "/zombie/villagers"), JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":village/skeleton_villager").apply(JigsawPattern.PlacementBehaviour.RIGID), 10);
		DataUtil.addToJigsawPattern(new ResourceLocation("pillager_outpost/features"), JigsawPiece.func_242849_a(SavageAndRavage.MOD_ID + ":pillager_outpost/feature_targets_arrow").apply(JigsawPattern.PlacementBehaviour.RIGID), 2);
	}

	public static void registerBiomeModifications() {
		BiomeModificationManager manager = BiomeModificationManager.INSTANCE;
		manager.addModifier(BiomeSpawnsModifier.createSpawnAdder((key, biome) -> BiomeDictionary.hasType(key, BiomeDictionary.Type.OVERWORLD) && canHostilesSpawn(biome.getRegistryName()), EntityClassification.MONSTER, SREntities.SKELETON_VILLAGER::get, 5, 1, 1));
		manager.addModifier(BiomeSpawnsModifier.createSpawnAdder((key, biome) -> BiomeDictionary.hasType(key, BiomeDictionary.Type.OVERWORLD) && BiomeModificationPredicates.forCategory(Biome.Category.ICY, Biome.Category.EXTREME_HILLS).test(key, biome), EntityClassification.MONSTER, SREntities.ICEOLOGER::get, 8, 1, 1));
	}

	public static boolean canHostilesSpawn(ResourceLocation biomeName) {
		Biome biome = ForgeRegistries.BIOMES.getValue(biomeName);
		if (biome != null) {
			if (biome.getCategory() != Biome.Category.MUSHROOM && biome.getCategory() != Biome.Category.NONE) {
				return false;
			} else
				return biome == ForgeRegistries.BIOMES.getValue(new ResourceLocation("biomesoplenty", "rainbow_hills"));
		}
		return true;
	}
}