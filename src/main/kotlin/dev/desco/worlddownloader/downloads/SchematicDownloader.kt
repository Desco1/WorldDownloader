package dev.desco.worlddownloader.downloads

import dev.desco.worlddownloader.WorldDownloader
import dev.desco.worlddownloader.core.configs.impl.SchematicSettings
import dev.desco.worlddownloader.mixins.accessors.NBTBaseAccessor
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagDouble
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.world.chunk.Chunk
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.zip.GZIPOutputStream

class SchematicDownloader(private val settings: SchematicSettings): Downloader {
    override val dir = File("schematics").also { if (!it.exists()) it.mkdir() }

    override fun saveChunks(chunks: Set<Chunk>) {
        WorldDownloader.launch {
            runCatching {
                val minX = chunks.minOf { it.xPosition } * 16
                val maxX = chunks.maxOf { it.xPosition } * 16 + 16
                val minZ = chunks.minOf { it.zPosition } * 16
                val maxZ = chunks.maxOf { it.zPosition } * 16 + 16
                val minY = chunks.minOf { it.blockStorageArray.minOf { it?.yLocation ?: Int.MAX_VALUE } }
                val maxY = chunks.maxOf { it.blockStorageArray.maxOf { it?.yLocation ?: Int.MIN_VALUE } } + 16

                val nbt = NBTTagCompound()
                nbt.setShort("Width", (maxX - minX).toShort())
                nbt.setShort("Height", ((maxY - minY)).toShort())
                nbt.setShort("Length", (maxZ - minZ).toShort())
                nbt.setString("Materials", "Alpha")

                val byteArray = ByteArray((maxX - minX + 1) * (maxY - minY + 1) * (maxX - minX + 1)) { -1 }
                val blockMeta = ByteArray((maxX - minX + 1) * (maxY - minY + 1) * (maxX - minX + 1)) { -1 }
                val addBlocks = ByteArray((maxX - minX) * (maxY - minY) * (maxX - minX) / 2)

                // Block section
                for (y in minY until maxY) {
                    for (z in minZ until maxZ) {
                        for (x in minX until maxX) {
                            val chunk = chunks.find { it.xPosition == x shr 4 && it.zPosition == z shr 4  } ?: continue
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

                // Entity section
                val entityList = NBTTagList()
                if (settings.entities) {
                    for (chunk in chunks) {
                        for (list in chunk.entityLists) {
                            for (entity in list) {
                                val entityTag = NBTTagCompound()

                                if (entity.writeToNBTOptional(entityTag)) {
                                    entityList.appendTag(entityTag.apply {
                                        entityTag.getTagList("Pos", 6).apply {
                                            this.set(0, NBTTagDouble(this.getDoubleAt(0) - minX))
                                            this.set(1, NBTTagDouble(this.getDoubleAt(1) - minY))
                                            this.set(2, NBTTagDouble(this.getDoubleAt(2) - minZ))
                                        }

                                        if (settings.setVisible) {
                                            if (this.hasKey("Invisible")) { // For Armor Stands
                                                this.setBoolean("Invisible", false)
                                            }
                                            if (this.hasKey("ActiveEffects")) { // For everyone else
                                                val potions = this.getTagList("ActiveEffects", 10)
                                                for (i in 0 until potions.tagCount()) {
                                                    val effect = potions.getCompoundTagAt(i)
                                                    if (effect.getInteger("Id") == 14) {
                                                        potions.removeTag(i)
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                        if (settings.invulnerable) {
                                            this.setBoolean("Invulnerable", true)
                                        }
                                        if (settings.silent) {
                                            this.setBoolean("Silent", true)
                                        }
                                    })
                                }
                            }
                        }
                    }
                }
                nbt.setTag("Entities", entityList)

                nbt.setTag("TileEntities", NBTTagList())

                DataOutputStream(BufferedOutputStream(GZIPOutputStream(dir.resolve("${format.format(Date())}.schematic").outputStream()))).use {
                    it.writeByte(nbt.id.toInt())
                    it.writeUTF("Schematic")
                    // Yet another excellent Mojang gambit
                    (nbt as NBTBaseAccessor).invokeWrite(it)
                }
            }.onFailure {
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(
                    "${EnumChatFormatting.RED}Something went wrong trying to download chunks. Logs may include useful information."
                ))
                it.printStackTrace()
            }.onSuccess {
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(
                    "${EnumChatFormatting.GREEN}Chunks have been downloaded."
                ))
            }
        }
    }

    companion object {
        private val format = SimpleDateFormat("H'h'mm'm'_MMMM_dd_yyyy")
    }
}