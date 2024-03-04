package dev.desco.worlddownloader.mixins;

import com.google.common.collect.Lists;
import dev.desco.worlddownloader.utils.ChunkProviderCache;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mixin(ChunkProviderClient.class)
public class ChunkProviderClientMixin implements ChunkProviderCache {

    @Unique private boolean saving = false;
    @Shadow private Chunk blankChunk;
    @Shadow private List<Chunk> chunkListing;
    @Unique private final HashMap<Long, Chunk> chunkCache = new HashMap<>();

    @Inject(method = "unloadChunk", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onUnloadChunk(int x, int z, CallbackInfo ci, Chunk chunk) {
        if (this.saving) {
            this.chunkCache.put(ChunkCoordIntPair.chunkXZ2Int(x, z), chunk);
        }
    }

    @Inject(method = "loadChunk", at = @At("RETURN"))
    private void loadChunk(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> ci) {
        if (this.saving) {
            this.chunkCache.remove(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
        }
    }

    @Inject(method = "provideChunk(Lnet/minecraft/util/BlockPos;)Lnet/minecraft/world/chunk/Chunk;", at = @At("RETURN"), cancellable = true)
    private void onProvideChunk(BlockPos pos, CallbackInfoReturnable<Chunk> cir) {
        if (this.saving) {
            if (cir.getReturnValue().isEmpty()) {
                final Chunk chunk = this.chunkCache.get(ChunkCoordIntPair.chunkXZ2Int(pos.getX() >> 4, pos.getZ() >> 4));
                cir.setReturnValue(chunk == null ? this.blankChunk : chunk);
            }
        }
    }

    @Override
    public boolean isSaving() {
        return saving;
    }

    @Override
    public void setSaving(boolean b) {
        this.saving = b;
        if (!this.saving) {
            this.chunkCache.clear();
        }
    }

    @NotNull
    @Override
    public Collection<Chunk> getChunks() {
        return this.chunkListing;
    }

    @NotNull
    @Override
    public Collection<Chunk> getCachedChunks() {
        return this.chunkCache.values();
    }
}
