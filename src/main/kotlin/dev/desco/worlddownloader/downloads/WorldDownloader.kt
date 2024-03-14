package dev.desco.worlddownloader.downloads

import dev.desco.worlddownloader.WorldDownloader
import dev.desco.worlddownloader.core.configs.impl.WorldSettings
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.scoreboard.ScoreboardSaveData
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.NibbleArray
import net.minecraft.world.chunk.storage.RegionFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class WorldDownloader(private val settings: WorldSettings): Downloader {

    private val world = Minecraft.getMinecraft().theWorld
    private val player = Minecraft.getMinecraft().thePlayer

    override val dir = File("saves").also { if (!it.exists()) it.mkdir() }

    override fun saveChunks(chunks: Set<Chunk>) {
        WorldDownloader.launch {
            val name = format.format(Date())
            val save = dir.resolve(name).also { if (!it.exists()) it.mkdir() }
            val regions = hashMapOf<Pair<Int, Int>, RegionFile>()
            chunks.forEach {
                val region = regions.computeIfAbsent(it.xPosition shr 5 to (it.zPosition shr 5)) { (x, z) ->
                    RegionFile(save.resolve("region").also { if (!it.exists()) it.mkdir() }.resolve("r.$x.$z.mca"))
                }

                val chunkIndex = (it.xPosition and 31) + (it.zPosition and 31) * 32
                val stream = region.getChunkDataOutputStream(chunkIndex % 32, chunkIndex / 32)
                val root = NBTTagCompound()
                val level = NBTTagCompound()
                root.setTag("Level", level)

                // Copied and pasted from AnvilChunkLoader, refactored a bit for readability
                level.setByte("V", 1.toByte())
                level.setInteger("xPos", it.xPosition)
                level.setInteger("zPos", it.zPosition)
                level.setLong("LastUpdate", world.totalWorldTime)
                level.setIntArray("HeightMap", it.heightMap)
                level.setBoolean("TerrainPopulated", it.isTerrainPopulated)
                level.setBoolean("LightPopulated", it.isLightPopulated)
                level.setLong("InhabitedTime", it.inhabitedTime)

                // Block section
                val blocks = it.blockStorageArray
                val blockList = NBTTagList()
                val flag = !world.provider.hasNoSky
                for (blockStorage in blocks) {
                    if (blockStorage != null) {
                        val nbt = NBTTagCompound()
                        nbt.setByte("Y", (blockStorage.yLocation shr 4 and 255).toByte())
                        val byteArray = ByteArray(blockStorage.data.size)
                        val blockData = NibbleArray()
                        val blockMeta = NibbleArray()

                        for (i in blockStorage.data.indices) {
                            val c0 = blockStorage.data[i]
                            val j = i and 15
                            val k = i shr 8 and 15
                            val l = i shr 4 and 15

                            if (c0.code shr 12 != 0) {
                                blockMeta[j, k, l] = c0.code shr 12
                            }

                            byteArray[i] = (c0.code shr 4 and 255).toByte()
                            blockData[j, k, l] = c0.code and 15
                        }

                        nbt.setByteArray("Blocks", byteArray)
                        nbt.setByteArray("Data", blockData.data)

                        if (blockMeta.data.isNotEmpty()) {
                            nbt.setByteArray("Add", blockMeta.data)
                        }

                        nbt.setByteArray("BlockLight", blockStorage.blocklightArray.data)

                        if (flag) {
                            nbt.setByteArray("SkyLight", blockStorage.skylightArray.data)
                        } else {
                            nbt.setByteArray(
                                "SkyLight",
                                ByteArray(blockStorage.blocklightArray.data.size)
                            )
                        }

                        blockList.appendTag(nbt)
                    }
                }
                level.setTag("Sections", blockList)

                level.setByteArray("Biomes", it.biomeArray)

                // Entity section
                it.setHasEntities(false)
                val entityList = NBTTagList()
                if (settings.entities) {
                    for (index in it.entityLists.indices) {
                        for (entity in it.entityLists[index]) {
                            val entityTag = NBTTagCompound()

                            if (entity.writeToNBTOptional(entityTag)) {
                                it.setHasEntities(true)
                                entityList.appendTag(entityTag.apply {
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
                level.setTag("Entities", entityList)

                // Tile entity section
                val tileEntityList = NBTTagList()
                for (tileEntity in it.tileEntityMap.values) {
                    val tileEntityTag = NBTTagCompound()
                    tileEntity.writeToNBT(tileEntityTag)
                    tileEntityList.appendTag(tileEntityTag)
                }
                level.setTag("TileEntities", tileEntityList)

                CompressedStreamTools.write(root, stream)
                stream.close()
            }
            regions.values.forEach { it.close() }

            // Level.dat
            save.resolve("level.dat").outputStream().use {
                CompressedStreamTools.writeCompressed(newLevelDat(name), it)
            }

            // Scoreboard.dat
            if (settings.scoreboard && world.scoreboard != null) {
                save.resolve("data").also { it.mkdir() }.resolve("scoreboard.dat").outputStream().use {
                    val scoreboard = NBTTagCompound()
                    ScoreboardSaveData().apply { setScoreboard(world.scoreboard) }.writeToNBT(scoreboard)
                    val nbt = NBTTagCompound()
                    nbt.setTag("data", scoreboard)
                    CompressedStreamTools.writeCompressed(nbt, it)
                }
            }

            Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(
                "${EnumChatFormatting.GREEN}Chunks have been downloaded."
            ))
        }
    }

    private fun newLevelDat(name: String) = NBTTagCompound().apply {
        this.setTag("Data", NBTTagCompound().apply {
            this.setBoolean("DownloadedWorld", true)
            this.setBoolean("TickingBlocks", !settings.blockUpdates)
            this.setBoolean("TickingEntities", settings.tickEntities)

            this.setLong("RandomSeed", 0L)
            this.setString("generatorName", "flat")
            this.setString("generatorOptions", "3;minecraft:air;1")
            this.setInteger("GameType", 3)
            this.setBoolean("MapFeatures", false)
            this.setInteger("SpawnX", player.posX.toInt())
            this.setInteger("SpawnY", player.posY.toInt())
            this.setInteger("SpawnZ", player.posZ.toInt())
            this.setLong("Time", if (settings.dayTime) world.worldTime else 6000L)
            this.setLong("LastPlayed", System.currentTimeMillis())
            this.setLong("SizeOnDisk", 0L)
            this.setString("LevelName", name)
            this.setInteger("version", 19133)
            this.setInteger("clearWeatherTime", if (settings.weather) world.worldInfo.cleanWeatherTime else 0)
            this.setInteger("rainTime", if (settings.weather) world.worldInfo.rainTime else 0)
            this.setBoolean("raining", if (settings.weather) world.worldInfo.isRaining else false)
            this.setInteger("thunderTime", if (settings.weather) world.worldInfo.rainTime else 0)
            this.setBoolean("thundering", if (settings.weather) world.worldInfo.isThundering else false)
            this.setBoolean("hardcore", false)
            this.setBoolean("initialized", true)
            this.setBoolean("allowCommands", true)

            val playerNBT = NBTTagCompound()
            player.writeToNBT(playerNBT)
            player.writeEntityToNBT(playerNBT)
            this.setTag("Player", playerNBT.apply {
                setInteger("playerGameType", if (settings.spectator) 3 else Minecraft.getMinecraft().playerController.currentGameType.ordinal)
                if (!settings.attributes) {
                    removeTag("Attributes")
                }
                if (!settings.inventory) {
                    removeTag("Inventory")
                }
                if (!settings.potions) {
                    removeTag("ActiveEffects")
                }
            })
            this.setTag("GameRules", NBTTagCompound().apply {
                setString("commandBlockOutput", "true")
                setString("doDaylightCycle", "false")
                setString("doEntityDrops", "false")
                setString("doFireTick", "false")
                setString("doMobLoot", "false")
                setString("doMobSpawning", "false")
                setString("doTileDrops", "true")
                setString("keepInventory", "true")
                setString("logAdminCommands", "true")
                setString("mobGriefing", "false")
                setString("naturalRegeneration", "true")
                setString("randomTickSpeed", if (settings.pauseRandomTicks) "0" else "3")
                setString("reducedDebugInfo", "false")
                setString("sendCommandFeedback", "true")
                setString("showDeathMessages", "true")
            })
        })
    }

    companion object {
        private val format = SimpleDateFormat("H'h'mm'm'_MMMM_dd_yyyy")
    }
}