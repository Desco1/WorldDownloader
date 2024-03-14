package dev.desco.worlddownloader.core

import dev.desco.worlddownloader.core.configs.impl.WorldSettings
import dev.desco.worlddownloader.core.configs.impl.SchematicSettings
import dev.desco.worlddownloader.downloads.SchematicDownloader
import dev.desco.worlddownloader.downloads.WorldDownloader
import gg.essential.api.EssentialAPI
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.state.map
import gg.essential.elementa.state.state
import gg.essential.elementa.state.toConstraint
import gg.essential.elementa.state.zip
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color

class SettingScreen(private val worldScreen: WorldScreen): WindowScreen(ElementaVersion.V1, newGuiScale = 2) {

    private val worldSettings = WorldSettings()
    private val schematicSettings = SchematicSettings()

    private val toolbar = UIContainer().constrain {
        this.x = 0.percent
        this.y = 0.percent
        this.width = 100.percent
        this.height = 5.percent
    } effect OutlineEffect(Color.WHITE, 1f, sides = setOf(OutlineEffect.Side.Bottom)) childOf window

    private val goBack = UIText("Back").constrain {
        this.x = 1.5.percent
        this.y = CenterConstraint()
    }.apply {
        onMouseEnter {
            this@apply.setText("§nBack")
        }
        onMouseLeave {
            this@apply.setText("Back")
        }
        onLeftClick {
            EssentialAPI.getGuiUtil().openScreen(worldScreen)
        }
    }  childOf toolbar

    private val divider = UIBlock(Color.WHITE).constrain {
        this.x = SiblingConstraint() + 1.5.percent
        this.y = 0.pixels
        this.width = 1.pixels
        this.height = 100.percent
    } childOf toolbar

    private val saveButton = UIText("Save as...").constrain {
        this.x = SiblingConstraint() + 1.5.percent
        this.y = CenterConstraint()
    }.apply {
        onMouseEnter {
            this@apply.setText("§nSave as...")
        }
        onMouseLeave {
            this@apply.setText("Save as...")
        }
        onLeftClick {
            when (downloader) {
                DownloaderType.WORLD -> WorldDownloader(worldSettings)
                DownloaderType.SCHEMATIC -> SchematicDownloader(schematicSettings)
            }.saveChunks(worldScreen.chunks)
            // todo this could use better UI but eh
        }
    } childOf toolbar

    var downloader by state(DownloaderType.SCHEMATIC).apply {
        state.onSetValue {
            container.clearChildren()
            when (it) {
                DownloaderType.WORLD -> worldSettings
                DownloaderType.SCHEMATIC -> schematicSettings
            } childOf container
        }
    }

    init {
        for (type in DownloaderType.entries) {
            TypeComponent(type).constrain {
                this.x = SiblingConstraint(10f)
                this.y = CenterConstraint()
            } childOf toolbar
        }
    }

    inner class TypeComponent(val type: DownloaderType): UIText() {
        private var hovered: Boolean by state(false)
        private val selected: Boolean by map(::downloader) { it == type }

        private val state by zip(::hovered, ::selected)

        init {
            onLeftClick {
                downloader = type
            }
            onMouseEnter {
                hovered = true
            }
            onMouseLeave {
                hovered = false
            }

            bindText(map(::state) { (hovered, selected) ->
                if (hovered || selected) {
                    type.name.uppercase()
                } else {
                    type.name.lowercase()
                }
            }.state)

            setColor(map<Pair<Boolean, Boolean>, Color>(::state) { (hovered, selected) ->
                if (hovered || selected) {
                    Color.WHITE
                } else {
                    Color.GRAY
                }
            }.state.toConstraint())
        }
    }

    private val container = UIContainer().constrain {
        this.x = 0.percent
        this.y = 5.percent
        this.width = 100.percent
        this.height = 95.percent
    } childOf window

    init {
        downloader = DownloaderType.WORLD
    }

    enum class DownloaderType {
        WORLD,
        SCHEMATIC,;
    }
}