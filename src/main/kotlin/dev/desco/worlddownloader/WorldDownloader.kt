package dev.desco.worlddownloader

import dev.desco.worlddownloader.commands.WorldDownloadCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

@Mod(
    modid = WorldDownloader.MODID,
    version = WorldDownloader.VERSION,
    name = WorldDownloader.NAME,
    clientSideOnly = true,
    acceptedMinecraftVersions = "[1.8.9]",
    modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter"
)
object WorldDownloader: CoroutineScope {
    const val NAME = "WorldDownloader"
    const val MODID = "descoisnotcreativesorry"
    const val VERSION = "1.0"

    val MODDIR = File("worlddownloader").also { if (!it.exists()) it.mkdir() }
    override val coroutineContext: CoroutineContext = Executors.newFixedThreadPool(10).asCoroutineDispatcher() + SupervisorJob()

    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent) {
        WorldDownloadCommand.register()

    }
}