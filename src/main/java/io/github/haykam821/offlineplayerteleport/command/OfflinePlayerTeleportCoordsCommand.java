package io.github.haykam821.offlineplayerteleport.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import io.github.haykam821.offlineplayerteleport.OfflinePlayerTeleport;
import io.github.haykam821.offlineplayerteleport.mixin.MinecraftServerMixin;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionType;

import java.util.Collection;

public final class OfflinePlayerTeleportCoordsCommand {
    private OfflinePlayerTeleportCoordsCommand() {
        return;
    }
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(OfflinePlayerTeleport.MOD_ID + "coords")
            .requires(Permissions.require("offlineplayerteleportcoods.command", 2))
            .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                .then(CommandManager.argument("position", BlockPosArgumentType.blockPos())
                    .executes(OfflinePlayerTeleportCoordsCommand::execute))));
    }
    
    @SuppressWarnings("deprecation")
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<GameProfile> targets = GameProfileArgumentType.getProfileArgument(context, "player");
        
        // Validate that a single player is selected by the argument
        if (targets.isEmpty()) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        } else if (targets.size() > 1) {
            throw EntityArgumentType.TOO_MANY_PLAYERS_EXCEPTION.create();
        }
        
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "position");
        
        ServerCommandSource source = context.getSource();
        
        MinecraftServer server = source.getServer();
        ServerWorld world = source.getWorld();
        
        GameProfile targetProfile = targets.iterator().next();
        ServerPlayerEntity target = new ServerPlayerEntity(server, world, targetProfile, null);
        
        WorldSaveHandler saveHandler = ((MinecraftServerMixin) server).getSaveHandler();
        NbtCompound nbt = saveHandler.loadPlayerData(target);
        
        RegistryKey<World> destinationKey = DimensionType.worldFromDimensionNbt(new Dynamic<>(NbtOps.INSTANCE, nbt.get("Dimension"))).result().orElse(World.OVERWORLD);
        ServerWorld destination = server.getWorld(destinationKey);
        
        if (destination == null) {
            destination = server.getOverworld();
        }
        
        target.setPos(pos.getX(), pos.getY(), pos.getZ());
        target.setWorld(destination);
        
        saveHandler.savePlayerData(target);
        
        context.getSource().sendFeedback(
            Text.literal(
                "%s position was changed to %d %d %d".formatted(
                    target.getName().getString(),
                    pos.getX(), pos.getY(), pos.getZ())
            ), true);
        
        return Command.SINGLE_SUCCESS;
    }
}
