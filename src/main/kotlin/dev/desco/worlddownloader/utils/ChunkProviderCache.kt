package dev.desco.worlddownloader.utils

import net.minecraft.world.chunk.Chunk

interface ChunkProviderCache {

    var isSaving: Boolean

    fun getChunks(): Collection<Chunk>
    fun getCachedChunks(): Collection<Chunk>
}