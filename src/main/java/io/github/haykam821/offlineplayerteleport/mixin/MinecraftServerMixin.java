package io.github.haykam821.offlineplayerteleport.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PlayerSaveHandler;

@Mixin(MinecraftServer.class)
public interface MinecraftServerMixin {
	@Accessor("saveHandler")
	public PlayerSaveHandler getSaveHandler();
}
