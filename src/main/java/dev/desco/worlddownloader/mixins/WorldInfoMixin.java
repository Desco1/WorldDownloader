package dev.desco.worlddownloader.mixins;

import dev.desco.worlddownloader.utils.DownloadedWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldInfo.class)
public class WorldInfoMixin implements DownloadedWorld {

    private boolean isDownloaded = false;
    private boolean tickBlocks = true;
    private boolean tickEntities = true;
    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void onInit(NBTTagCompound tag, CallbackInfo ci) {
        this.isDownloaded = tag.getBoolean("DownloadedWorld");
        this.tickBlocks = !tag.hasKey("TickingBlocks") || tag.getBoolean("TickingBlocks");
        this.tickEntities = !tag.hasKey("TickingEntities") || tag.getBoolean("TickingEntities");
    }

    @Inject(method = "updateTagCompound", at = @At("RETURN"))
    private void onSaveInfo(NBTTagCompound nbt, NBTTagCompound playerNBT, CallbackInfo ci) {
        nbt.setBoolean("DownloadedWorld", this.isDownloaded);
        nbt.setBoolean("TickingBlocks", this.tickBlocks);
        nbt.setBoolean("TickingEntities", this.tickEntities);
    }

    @Override
    public boolean isDownloadedWorld() {
        return this.isDownloaded;
    }

    @Override
    public boolean isTickingBlocks() {
        return this.tickBlocks;
    }

    @Override
    public boolean isTickingEntities() {
        return this.tickEntities;
    }
}
