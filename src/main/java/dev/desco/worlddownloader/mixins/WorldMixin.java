package dev.desco.worlddownloader.mixins;

import dev.desco.worlddownloader.utils.DownloadedWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public abstract class WorldMixin {

    @Shadow abstract WorldInfo getWorldInfo();

    @Redirect(method = "updateEntityWithOptionalForce",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateRidden()V"))
    private void onUpdateRidden(Entity in) {
        if (shouldPreventEntityTicks(in)) {
            if (in instanceof EntityLivingBase) {
                ((EntityLivingBase) in).prevRotationYawHead = ((EntityLivingBase) in).rotationYawHead;
                ((EntityLivingBase) in).prevRenderYawOffset = ((EntityLivingBase) in).renderYawOffset;
            }
            return;
        }
        in.updateRidden();
    }

    @Redirect(method = {"updateEntityWithOptionalForce", "updateEntities"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    private void onUpdate(Entity in) {
        if (shouldPreventEntityTicks(in)) {
            if (in instanceof EntityLivingBase) {
                ((EntityLivingBase) in).prevRotationYawHead = ((EntityLivingBase) in).rotationYawHead;
                ((EntityLivingBase) in).prevRenderYawOffset = ((EntityLivingBase) in).renderYawOffset;
            }
            return;
        }
        in.onUpdate();
    }

    @ModifyVariable(method = "setBlockState(Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", at = @At("HEAD"), argsOnly = true)
    private int removeBlockUpdates(int flag) {
        if ((flag & 1) == 1 && shouldPreventBlockUpdates()) {
            return flag - 1;
        }
        return flag;
    }

    private boolean shouldPreventEntityTicks(Entity in) {
        if ((World) (Object) this instanceof WorldClient && !Minecraft.getMinecraft().isIntegratedServerRunning()) {
            return false;
        }
        WorldInfo info;
        if ((World) (Object) this instanceof WorldClient) {
            info = Minecraft.getMinecraft().getIntegratedServer().getEntityWorld().getWorldInfo();
        } else {
            info = this.getWorldInfo();
        }

        if (info instanceof DownloadedWorld && ((DownloadedWorld) info).isDownloadedWorld()) {
            if (in instanceof EntityPlayer) {
                return false;
            }

            if (!(in instanceof EntityFX) && !((DownloadedWorld) info).isTickingEntities()) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldPreventBlockUpdates() {
        if ((World) (Object) this instanceof WorldClient && !Minecraft.getMinecraft().isIntegratedServerRunning()) {
            return false;
        }
        WorldInfo info;
        if ((World) (Object) this instanceof WorldClient) {
            info = Minecraft.getMinecraft().getIntegratedServer().getEntityWorld().getWorldInfo();
        } else {
            info = this.getWorldInfo();
        }

        if (info instanceof DownloadedWorld && ((DownloadedWorld) info).isDownloadedWorld()) {
            if (!((DownloadedWorld) info).isTickingBlocks()) {
                return true;
            }
        }

        return false;
    }
}
