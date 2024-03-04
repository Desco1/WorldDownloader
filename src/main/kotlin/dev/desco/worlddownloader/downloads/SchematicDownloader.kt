package dev.desco.worlddownloader.downloads

import dev.desco.worlddownloader.WorldDownloader
import dev.desco.worlddownloader.core.configs.impl.SchematicSettings
import dev.desco.worlddownloader.mixins.NBTBaseAccessor
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.MathHelper
import net.minecraft.world.chunk.Chunk
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.zip.GZIPOutputStream
import kotlin.math.floor

class SchematicDownloader(private val settings: SchematicSettings): Downloader {
    override val dir = File("schematics").also { if (!it.exists()) it.mkdir() }

    override fun saveChunks(chunks: Set<Chunk>) {
        WorldDownloader.launch {
            val minX = chunks.minOf { it.xPosition } * 16
            val maxX = chunks.maxOf { it.xPosition } * 16 + 16
            val minZ = chunks.minOf { it.zPosition } * 16
            val maxZ = chunks.maxOf { it.zPosition } * 16 + 16
            val minY = chunks.minOf { it.blockStorageArray.minOf { it?.yLocation ?: Int.MAX_VALUE } * 16 }
            val maxY = chunks.maxOf { it.blockStorageArray.maxOf { it?.yLocation ?: Int.MIN_VALUE } } + 16

            val nbt = NBTTagCompound()
            nbt.setShort("Width", (maxX - minX).toShort())
            nbt.setShort("Height", ((maxY - minY)).toShort())
            nbt.setShort("Length", (maxZ - minZ).toShort())
            nbt.setString("Materials", "Alpha")

            val byteArray = ByteArray((maxX - minX + 1) * (maxY - minY + 1) * (maxX - minX + 1)) { -1 }
            val blockMeta = ByteArray((maxX - minX + 1) * (maxY - minY + 1) * (maxX - minX + 1)) { -1 }
            val addBlocks = ByteArray((maxX - minX) * (maxY - minY) * (maxX - minX) / 2)

            chunks.first().let { println("${it.xPosition}, ${it.zPosition}") }

            for (y in minY until maxY) {
                for (z in minZ until maxZ) {
                    for (x in minX until maxX) {
                        val chunk = chunks.find { it.xPosition == x shr 4 && it.zPosition == z shr 4  } ?: run {
                            println("$x, $z")
                            println("${x shr 4}, ${z shr 4}")
                            return@launch
                        }
                        chunk.blockStorageArray?.get(y shr 4)?.let {
                            val i = x.mod(16)
                            val j = y.mod(16)
                            val k = z.mod(16)

                            val state = it.get(i, j, k)
                            val id = Block.getIdFromBlock(state.block)
                            val meta = state.block.getMetaFromState(state)

                            byteArray[((y - minY) * (maxZ - minZ) + (z - minZ)) * (maxX - minX) + x - minX] = (id and 0xff).toByte()
                            blockMeta[((y - minY) * (maxZ - minZ) + (z - minZ)) * (maxX - minX) + x - minX] = (meta and 0xff).toByte()
                        }
                    }
                }
            }

            nbt.setByteArray("Blocks", byteArray)
            nbt.setByteArray("Data", blockMeta)

/*            if (addBlocks.isNotEmpty()) {
                nbt.setByteArray("AddBlocks", addBlocks)
            }*/

            nbt.setTag("TileEntities", NBTTagList())

            DataOutputStream(BufferedOutputStream(GZIPOutputStream(dir.resolve("${format.format(Date())}.schematic").outputStream()))).use {
                it.writeByte(nbt.id.toInt())
                it.writeUTF("Schematic")
                // Yet another excellent Mojang gambit
                (nbt as NBTBaseAccessor).invokeWrite(it)
            }
        }
    }

    companion object {
        private val format = SimpleDateFormat("H'h'mm'm'_MMMM_dd_yyyy")
    }
}