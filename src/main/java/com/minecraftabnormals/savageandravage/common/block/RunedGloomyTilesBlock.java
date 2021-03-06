package com.minecraftabnormals.savageandravage.common.block;

import com.minecraftabnormals.savageandravage.common.entity.IOwnableMob;
import com.minecraftabnormals.savageandravage.common.entity.RunePrisonEntity;
import com.minecraftabnormals.savageandravage.core.registry.SRSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.EvokerFangsEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class RunedGloomyTilesBlock extends ChiseledGloomyTilesBlock {

	public RunedGloomyTilesBlock(Properties blockProperties) {
		super(blockProperties);
	}

	@Override
	public void stepOn(World world, BlockPos pos, Entity entity) {
		super.stepOn(world, pos, entity);
		BlockState state = world.getBlockState(pos);
		if (!state.getValue(POWERED) && shouldTrigger(entity)) {
			world.setBlockAndUpdate(pos, state.setValue(POWERED, true));
			world.playSound(null, pos, SRSounds.BLOCK_RUNED_GLOOMY_TILES_ACTIVATE.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
			EvokerFangsEntity evokerFangs = EntityType.EVOKER_FANGS.create(world);
			if (evokerFangs != null) {
				evokerFangs.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);
				world.addFreshEntity(evokerFangs);
			}
			RunePrisonEntity runePrison = new RunePrisonEntity(world, pos, 25);
			runePrison.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);
			world.addFreshEntity(runePrison);
		}
	}

	public static boolean shouldTrigger(Entity entity) {
		if (entity instanceof LivingEntity) {
			if (!EntityTypeTags.RAIDERS.contains(entity.getType())) {
				if (entity instanceof PlayerEntity) {
					return !((PlayerEntity) entity).isCreative() && !entity.isSpectator();
				} else if (entity instanceof IOwnableMob) {
					LivingEntity owner = ((IOwnableMob) entity).getOwner();
					return owner == null || !EntityTypeTags.RAIDERS.contains(owner.getType());
				} else return !(entity instanceof ArmorStandEntity);
			}
		}
		return false;
	}
}
