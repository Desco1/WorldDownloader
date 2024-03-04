package dev.desco.worlddownloader.downloads

import net.minecraft.util.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import java.io.File

sealed interface Downloader {

    val dir: File
    fun saveChunks(chunks: Set<Chunk>)
}