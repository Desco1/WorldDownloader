package dev.desco.worlddownloader.mixins;

import net.minecraft.profiler.Profiler;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World implements IThreadListener {

    protected WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Redirect(method = "updateBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;canDoRainSnowIce(Lnet/minecraft/world/chunk/Chunk;)Z"))
    private boolean overrideSnowAndIce(WorldProvider provider, Chunk chunk) {
        // Another excellent gambit by Mojang.
        if (this.getGameRules().getInt("randomTickSpeed") == 0) {
            return false;
        }
        return provider.canDoRainSnowIce(chunk);
    }
}
