package com.minecraftabnormals.savageandravage.core.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.LootContext;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Accessor("lastHurtByPlayerTime")
	int getRecentlyHit();

	@Invoker("createLootContext")
	LootContext.Builder invokeGetLootContextBuilder(boolean recentlyHit, DamageSource damageSource);
}