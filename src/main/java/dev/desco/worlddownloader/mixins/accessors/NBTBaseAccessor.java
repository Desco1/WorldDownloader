package dev.desco.worlddownloader.mixins.accessors;

import net.minecraft.nbt.NBTBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.DataOutput;

@Mixin(NBTBase.class)
public interface NBTBaseAccessor {

    @Invoker void invokeWrite(DataOutput output);
}
