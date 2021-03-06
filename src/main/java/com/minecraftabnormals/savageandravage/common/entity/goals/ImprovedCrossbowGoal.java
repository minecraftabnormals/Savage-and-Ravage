package com.minecraftabnormals.savageandravage.common.entity.goals;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

public class ImprovedCrossbowGoal<T extends CreatureEntity & IRangedAttackMob & ICrossbowUser> extends Goal {

	private final T entity;
	private ImprovedCrossbowGoal.CrossbowState crossbowState = ImprovedCrossbowGoal.CrossbowState.UNCHARGED;
	private final double speedChanger;
	private final float radiusSq;
	private int seeTime;
	private int wait;
	private final double blocksUntilBackupSq;

	public ImprovedCrossbowGoal(T entity, double speedChanger, float radius, double blocksUntilBackup) {
		this.entity = entity;
		this.speedChanger = speedChanger;
		this.radiusSq = radius * radius;
		this.blocksUntilBackupSq = blocksUntilBackup;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	private boolean hasCrossbowOnMainHand() {
		return this.entity.getMainHandItem().getItem() instanceof CrossbowItem;
	}

	private boolean hasAttackTarget() {
		return this.entity.getTarget() != null && this.entity.getTarget().isAlive();
	}

	@Override
	public boolean canUse() {
		return this.hasAttackTarget() && this.hasCrossbowOnMainHand();
	}

	@Override
	public boolean canContinueToUse() {
		return this.hasAttackTarget() && (this.canUse() || !this.entity.getNavigation().isDone()) && this.hasCrossbowOnMainHand();
	}

	@Override
	public void stop() {
		super.stop();
		this.entity.setAggressive(false);
		this.entity.setTarget(null);
		this.seeTime = 0;
		if (this.entity.isUsingItem()) {
			this.entity.stopUsingItem();
			this.entity.setChargingCrossbow(false);
		}
	}

	private boolean isWalkable() {
		PathNavigator pathnavigator = this.entity.getNavigation();
		NodeProcessor nodeprocessor = pathnavigator.getNodeEvaluator();
		return nodeprocessor.getBlockPathType(this.entity.level, MathHelper.floor(this.entity.getX() + 1.0D), MathHelper.floor(this.entity.getY()), MathHelper.floor(this.entity.getZ() + 1.0D)) == PathNodeType.WALKABLE;
	}

	@Override
	public void tick() {
		LivingEntity target = this.entity.getTarget();
		this.entity.setAggressive(true); // Minecraft doesn't think shooting an arrow at another entity is aggression.
		if (target == null)
			return;

		boolean canSeeEnemy = this.entity.getSensing().canSee(target);
		if (canSeeEnemy) {
			++this.seeTime;
		} else {
			this.seeTime = 0;
		}

		double distanceSq = target.distanceToSqr(entity);
		double distance = target.distanceTo(entity);
		if (distance <= blocksUntilBackupSq && !(target instanceof AbstractVillagerEntity)) {
			this.entity.lookAt(target, 30.0F, 30.0F);
			if (this.isWalkable())
				this.entity.getMoveControl().strafe(entity.isUsingItem() ? -0.5F : -3.0F, 0); // note: when an entity is "charging" their crossbow they set an active hand
		}
		ItemStack activeStack = this.entity.getUseItem();
		boolean shouldMoveTowardsEnemy = (distanceSq > (double) this.radiusSq || this.seeTime < 5) && this.wait == 0;
		if (shouldMoveTowardsEnemy) {
			this.entity.getNavigation().moveTo(target, this.isCrossbowUncharged() ? this.speedChanger : this.speedChanger * 0.5D);
		} else {
			this.entity.getNavigation().stop();
		}

		this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
		if (this.crossbowState == ImprovedCrossbowGoal.CrossbowState.UNCHARGED && !CrossbowItem.isCharged(activeStack)) {
			if (canSeeEnemy) {
				this.entity.startUsingItem(ProjectileHelper.getWeaponHoldingHand(this.entity, Items.CROSSBOW));
				this.crossbowState = ImprovedCrossbowGoal.CrossbowState.CHARGING;
				this.entity.setChargingCrossbow(true);
			}
		} else if (this.crossbowState == ImprovedCrossbowGoal.CrossbowState.CHARGING) {
			if (!this.entity.isUsingItem()) {
				this.crossbowState = ImprovedCrossbowGoal.CrossbowState.UNCHARGED;
			}

			int i = this.entity.getTicksUsingItem();
			if (i >= CrossbowItem.getChargeDuration(activeStack) || CrossbowItem.isCharged(activeStack)) {
				this.entity.releaseUsingItem();
				this.crossbowState = ImprovedCrossbowGoal.CrossbowState.CHARGED;
				this.wait = 20 + this.entity.getRandom().nextInt(20);
				if (entity.getOffhandItem().getItem() instanceof FireworkRocketItem) {
					entity.startUsingItem(Hand.OFF_HAND);
				}
				this.entity.setChargingCrossbow(false);
			}
		} else if (this.crossbowState == ImprovedCrossbowGoal.CrossbowState.CHARGED) {
			--this.wait;
			if (this.wait == 0) {
				this.crossbowState = ImprovedCrossbowGoal.CrossbowState.READY_TO_ATTACK;
			}
		} else if (this.crossbowState == ImprovedCrossbowGoal.CrossbowState.READY_TO_ATTACK && canSeeEnemy) {
			this.entity.performRangedAttack(target, 1.0F);
			CrossbowItem.setCharged(this.entity.getItemInHand(ProjectileHelper.getWeaponHoldingHand(this.entity, Items.CROSSBOW)), false);
			this.crossbowState = ImprovedCrossbowGoal.CrossbowState.UNCHARGED;
		}
	}

	private boolean isCrossbowUncharged() {
		return this.crossbowState == ImprovedCrossbowGoal.CrossbowState.UNCHARGED;
	}

	enum CrossbowState {
		UNCHARGED, CHARGING, CHARGED, READY_TO_ATTACK
	}
}
