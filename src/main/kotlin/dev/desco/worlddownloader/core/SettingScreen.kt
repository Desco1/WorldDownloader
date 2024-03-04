package dev.desco.worlddownloader.core

import dev.desco.worlddownloader.core.configs.impl.SaveSettings
import dev.desco.worlddownloader.core.configs.impl.SchematicSettings
import dev.desco.worlddownloader.core.configs.impl.StructureSettings
import dev.desco.worlddownloader.downloads.Downloader
import dev.desco.worlddownloader.downloads.SaveDownloader
import dev.desco.worlddownloader.downloads.SchematicDownloader
import dev.desco.worlddownloader.downloads.StructureDownloader
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.state.map
import gg.essential.elementa.state.state
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color
import kotlin.math.abs
import kotlin.reflect.KProperty

class SettingScreen(val worldScreen: WorldScreen): WindowScreen(ElementaVersion.V1, newGuiScale = 2) {

    private val saveSettings = SaveSettings()
    private val schematicSettings = SchematicSettings()
    private val structureSettings = StructureSettings()

    private val toolbar = UIContainer().constrain {
        this.x = 0.percent
        this.y = 0.percent
        this.width = 15.percent
        this.height = 100.percent
    } effect OutlineEffect(Color.WHITE, 1f, sides = setOf(OutlineEffect.Side.Right)) childOf window

    private val typeBlock = UIContainer().constrain {
        this.x = CenterConstraint()
        this.y = 5.pixels
        this.width = 95.percent
        this.height = ChildBasedSizeConstraint(2f)
    } childOf toolbar

    private val previous = UIText("<-").constrain {
        this.x = 5.pixels
        this.y = CenterConstraint()
    }.apply {
        onMouseEnter {
            effect(OutlineEffect(Color.WHITE, 1f))
        }
        onMouseLeave {
            removeEffect<OutlineEffect>()
        }
        onLeftClick {
            downloader = DownloaderType.entries[if (downloader.ordinal == 0) DownloaderType.entries.size - 1 else (downloader.ordinal - 1) % DownloaderType.entries.size]
        }
    } childOf typeBlock

    var downloader by state(DownloaderType.SCHEMATIC).apply {
        state.onSetValue {
            container.clearChildren()
            when (it) {
                DownloaderType.SAVE -> saveSettings
                DownloaderType.SCHEMATIC -> schematicSettings
                DownloaderType.STRUCTURE -> structureSettings
            } childOf container
            typeText.setText(it.name)
        }
    }

    private val typeText: UIText = UIText("").constrain {
        this.x = CenterConstraint()
        this.y = CenterConstraint()
    }.apply {
        onMouseEnter {
            effect(OutlineEffect(Color.WHITE, 1f))
        }
        onMouseLeave {
            removeEffect<OutlineEffect>()
        }
        onLeftClick {
            when (downloader) {
                DownloaderType.SAVE -> SaveDownloader(saveSettings)
                DownloaderType.SCHEMATIC -> SchematicDownloader(schematicSettings)
                DownloaderType.STRUCTURE -> StructureDownloader(structureSettings)
                else -> TODO()
            }.saveChunks(worldScreen.chunks)
        }
    } childOf typeBlock

    private val next = UIText("->").constrain {
        this.x = 5.pixels(true)
        this.y = CenterConstraint()
    }.apply {
        onMouseEnter {
            effect(OutlineEffect(Color.WHITE, 1f))
        }
        onMouseLeave {
            removeEffect<OutlineEffect>()
        }
        onLeftClick {
            downloader = DownloaderType.entries[(downloader.ordinal + 1).mod(DownloaderType.entries.size)]
        }
    } childOf typeBlock

    private val container = UIContainer().constrain {
        this.x = 15.percent
        this.y = 0.percent
        this.width = 85.percent
        this.height = 100.percent
    } childOf window

    init {
        downloader = DownloaderType.SAVE
    }

    enum class DownloaderType {
        SAVE,
        SCHEMATIC,
        STRUCTURE,;
    }
}