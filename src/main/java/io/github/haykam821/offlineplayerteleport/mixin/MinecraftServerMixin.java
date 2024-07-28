package io.github.haykam821.offlineplayerteleport.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerMixin {
	@Accessor("saveHandler")
    public WorldSaveHandler getSaveHandler();
}
