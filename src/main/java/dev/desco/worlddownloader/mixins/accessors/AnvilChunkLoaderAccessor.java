package dev.desco.worlddownloader.mixins.accessors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AnvilChunkLoader.class)
public interface AnvilChunkLoaderAccessor {

    @Invoker void invokeWriteChunkToNBT(Chunk chunkIn, World worldIn, NBTTagCompound p_75820_3_);
}
