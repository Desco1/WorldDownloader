package dev.desco.worlddownloader.downloads

import dev.desco.worlddownloader.WorldDownloader
import dev.desco.worlddownloader.core.configs.impl.StructureSettings
import kotlinx.coroutines.launch
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagInt
import net.minecraft.nbt.NBTTagList
import net.minecraft.world.chunk.Chunk
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class StructureDownloader(val settings: StructureSettings): Downloader {
    override val dir = File("data")
        .resolve("descosworlddownloader")
        .resolve("structures").also { if (!it.exists()) it.mkdirs() }

    override fun saveChunks(chunks: Set<Chunk>) {
        WorldDownloader.launch {
            val nbt = NBTTagCompound()
            nbt.setInteger("DataVersion", 3700)
            val size = NBTTagList()
            repeat(3) {
                size.appendTag(NBTTagInt(1))
            }
            nbt.setTag("size", size)
            val blocks = NBTTagList()
            blocks.appendTag(NBTTagCompound().apply {
                setInteger("state", 0)
                setTag("pos", NBTTagList().apply { repeat(3) { this.appendTag(NBTTagInt(0))} })
            })
            nbt.setTag("blocks", blocks)

            nbt.setTag("entities", NBTTagList())
            nbt.setTag("palette", NBTTagList().apply {
                appendTag(NBTTagCompound().apply {
                    setString("Name", "minecraft:lit_furnace")
                })
            })

            dir.resolve("${format.format(Date())}.nbt").outputStream().use {
                CompressedStreamTools.writeCompressed(nbt, it)
            }
        }
    }

    companion object {
        private val format = SimpleDateFormat("H'h'mm'm'_MMMM_dd_yyyy")
    }
}