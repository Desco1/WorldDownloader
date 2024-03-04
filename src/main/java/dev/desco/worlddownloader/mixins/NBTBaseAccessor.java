package dev.desco.worlddownloader.mixins;

import net.minecraft.nbt.NBTBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.DataOutput;

@Mixin(NBTBase.class)
public interface NBTBaseAccessor {

    @Invoker abstract void invokeWrite(DataOutput output);
}
