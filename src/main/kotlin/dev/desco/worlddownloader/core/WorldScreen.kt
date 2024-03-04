package dev.desco.worlddownloader.core

import dev.desco.worlddownloader.utils.ChunkProviderCache
import gg.essential.api.EssentialAPI
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.state.*
import gg.essential.elementa.utils.roundToRealPixels
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UMouse
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.EmptyChunk
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import kotlin.math.max
import kotlin.math.min

class WorldScreen: WindowScreen(ElementaVersion.V1, newGuiScale = 2) {

    private val provider = Minecraft.getMinecraft().theWorld.chunkProvider
    val chunks by state(hashSetOf<Chunk>())

    private val toolbar = UIBlock(Color.BLACK).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.width = 100.percent
        this.height = 5.percent
    } childOf window

    private var cursor by state(CursorType.AREA).apply {
        this.state.onSetValue {
            val i = it.ordinal
            background.animate {
                setXAnimation(Animations.OUT_SIN, 0.1f, (10 + i * (toolbar.getHeight() * 0.80).roundToRealPixels() + i * 5 - (toolbar.getHeight() * 0.07).roundToRealPixels()).pixels)
            }
        }
    }

    private val background = UIRoundedRectangle(5f).constrain {
        this.x = (10 - (toolbar.getHeight() * 0.07).roundToRealPixels()).pixels
        this.y = CenterConstraint()
        this.width = AspectConstraint()
        this.height = 95.percent
        this.color = Color(170, 170, 170, 170).toConstraint()
    } childOf toolbar

    private val select = UIImage.ofResource("/select-icon.png").constrain {
        this.x = 10.pixels
        this.y = CenterConstraint()
        this.width = AspectConstraint()
        this.height = 80.percent
    }.apply {
        this childOf toolbar
        onLeftClick {
            cursor = CursorType.SELECT
        }
        onMouseEnter {
            Window.enqueueRenderOperation {
                tooltip.setFloating(true)
                tooltip.setX(this.getLeft().pixels)
                tooltipText.setText("Select")
                tooltip.unhide(true)
            }
        }
        onMouseLeave {
            Window.enqueueRenderOperation {
                tooltip.setFloating(false)
                tooltip.hide(true)
            }
        }
    }

    private val move = UIImage.ofResource("/move-icon.png").constrain {
        this.x = SiblingConstraint(5f).apply { this.constrainTo = select }
        this.y = CenterConstraint()
        this.width = AspectConstraint()
        this.height = 80.percent
    }.apply {
        this childOf toolbar
        onLeftClick {
            cursor = CursorType.MOVE
        }
        onMouseEnter {
            Window.enqueueRenderOperation {
                tooltip.setFloating(true)
                tooltip.setX(this.getLeft().pixels)
                tooltipText.setText("Move")
                tooltip.unhide(true)
            }
        }
        onMouseLeave {
            Window.enqueueRenderOperation {
                tooltip.setFloating(false)
                tooltip.hide(true)
            }
        }
    }

    private val area = UIImage.ofResource("/area-icon.png").constrain {
        this.x = SiblingConstraint(5f).apply { this.constrainTo = move }
        this.y = CenterConstraint()
        this.width = AspectConstraint()
        this.height = 80.percent
    }.apply {
        this childOf toolbar
        onLeftClick {
            cursor = CursorType.AREA
        }
        onMouseEnter {
            Window.enqueueRenderOperation {
                tooltip.setFloating(true)
                tooltip.setX(this.getLeft().pixels)
                tooltipText.setText("Select area")
                tooltip.unhide(true)
            }
        }
        onMouseLeave {
            Window.enqueueRenderOperation {
                tooltip.setFloating(false)
                tooltip.hide(true)
            }
        }
    }

    private val divider = UIBlock(Color(85, 85, 85)).constrain {
        this.x = SiblingConstraint(5f).apply { this.constrainTo = area }
        this.y = CenterConstraint()
        this.width = 1.pixels
        this.height = 70.percent
    } childOf toolbar

    private val toggleCache = UIImage.ofResource("/cache-icon.png").constrain {
        this.x = SiblingConstraint(5f).apply { this.constrainTo = divider }
        this.y = CenterConstraint()
        this.width = AspectConstraint()
        this.height = 80.percent
        this.color = if ((provider as ChunkProviderCache).isSaving) {
            Color.WHITE
        } else {
            Color(85, 85, 85)
        }.toConstraint()
    }.apply {
        this childOf toolbar
        onMouseEnter {
            if (!(provider as ChunkProviderCache).isSaving) {
                animate {
                    setColorAnimation(Animations.LINEAR, 0.1f, Color(170, 170, 170).toConstraint())
                }
            }
            Window.enqueueRenderOperation {
                tooltip.setFloating(true)
                tooltip.setX(this.getLeft().pixels)
                tooltipText.setText("Toggle chunk cache")
                tooltip.unhide(true)
            }
        }
        onMouseLeave {
            if (!(provider as ChunkProviderCache).isSaving) {
                animate {
                    setColorAnimation(Animations.LINEAR, 0.1f, Color(85, 85, 85).toConstraint())
                }
            }
            Window.enqueueRenderOperation {
                tooltip.setFloating(false)
                tooltip.hide(true)
            }
        }
        onLeftClick {
            (provider as ChunkProviderCache).isSaving = !(provider as ChunkProviderCache).isSaving
            if ((provider as ChunkProviderCache).isSaving) {
                this.setColor(Color.WHITE.toConstraint())
            } else {
                this.setColor(Color(170, 170, 170).toConstraint())
            }
        }
    }

    private val download = UIImage.ofResource("/download-icon.png").constrain {
        this.x = SiblingConstraint(5f).apply { this.constrainTo = toggleCache }
        this.y = CenterConstraint()
        this.width = AspectConstraint()
        this.height = 80.percent
        this.color = Color.DARK_GRAY.toConstraint()
    }.apply {
        this childOf toolbar
        onLeftClick {
            EssentialAPI.getGuiUtil().openScreen(SettingScreen(this@WorldScreen))
        }
        onMouseEnter {
            Window.enqueueRenderOperation {
                tooltip.setFloating(true)
                tooltip.setX(this.getLeft().pixels)
                tooltipText.setText("Download chunks")
                tooltip.unhide(true)
            }
        }
        onMouseLeave {
            Window.enqueueRenderOperation {
                tooltip.setFloating(false)
                tooltip.hide(true)
            }
        }
    }

    private val tooltip = UIBlock(Color.BLACK).constrain {
        this.y = 5.percent + 5.pixels
        this.width = ChildBasedSizeConstraint() + 5.pixels
        this.height = ChildBasedSizeConstraint() + 5.pixels
    }.apply {
        this childOf window
        this.hide(true)
    }

    private val tooltipText = UIText().constrain {
        this.x = CenterConstraint()
        this.y = CenterConstraint()
    } childOf tooltip

    override fun afterInitialization() {
        cursor = CursorType.SELECT
    }

    private var dragging = false
    private var dragStart = 0f to 0f
    private var areaType = false

    private val mapWindow: UIContainer = UIContainer().constrain {
        this.x = 0.pixels
        this.y = 5.percent
        this.width = 100.percent
        this.height = FillConstraint(false)
    }.apply {
        this childOf window
        this effect ScissorEffect()
        this.onMouseClick {
            dragging = true
            dragStart = it.absoluteX to it.absoluteY
            if (cursor == CursorType.AREA) {
                val state = (this.hitTest(it.absoluteX, it.absoluteY) as? ChunkComponent)?.selected
                state?.let {
                    areaType = state == false
                } ?: run {
                    dragging = false
                    dragStart = 0f to 0f
                }
            }
        }.onMouseRelease {
            if (dragging && cursor == CursorType.AREA) {
                val xRange = min(dragStart.first, UMouse.Scaled.x.toFloat()) .. max(dragStart.first, UMouse.Scaled.x.toFloat())
                val yRange = min(dragStart.second, UMouse.Scaled.y.toFloat()) .. max(dragStart.second, UMouse.Scaled.y.toFloat())

                for (chunkComponent in chunkHolder.childrenOfType<ChunkComponent>()) {
                    if ((chunkComponent.getLeft() in xRange || chunkComponent.getRight() in xRange)
                        && (chunkComponent.getTop() in yRange || chunkComponent.getBottom() in yRange)) {
                        chunkComponent.selected = areaType
                    }
                }
            }

            dragging = false
            dragStart = 0f to 0f
        }.onMouseDrag { mouseX, mouseY, mouseButton ->
            if (!dragging) return@onMouseDrag
            if (cursor != CursorType.MOVE) return@onMouseDrag

            val absX = mouseX + this.getLeft()
            val absY = mouseY + this.getTop()
            val deltaX = absX - dragStart.first
            val deltaY = absY - dragStart.second
            dragStart = absX to absY


            if (deltaX != 0f || deltaY != -1f) { // Do not ask. Elementa is playing dolls with the parameter types
                val newX = (chunkHolder.getLeft() - this.getLeft() + deltaX)
                    .coerceIn(-chunkHolder.getWidth() + 10f, this.getRight() - 10f)
                val newY = (chunkHolder.getTop() - this.getTop() + deltaY)
                    .coerceIn(-chunkHolder.getHeight() + this.getTop() + 10f, this.getBottom() - 10f)
                chunkHolder.setX(newX.pixels)
                chunkHolder.setY(newY.pixels)
            }
        }
    }

    private val chunkHolder = UIContainer().constrain {
        this.width = ChildBasedRangeConstraint()
        this.height = ChildBasedRangeConstraint()
    } childOf mapWindow

    init {
        provider as ChunkProviderCache

        val minX = provider.getChunks().minOf { it.xPosition }
        val minZ = provider.getChunks().minOf { it.zPosition }
        val maxX = provider.getChunks().maxOf { it.xPosition } + 1
        val maxZ = provider.getChunks().maxOf { it.zPosition } + 1
        for (chunk in provider.getChunks()) {
            ChunkComponent(chunk).constrain {
                this.x = ((chunk.xPosition - minX) * 16).pixels
                this.y = ((chunk.zPosition - minZ) * 16).pixels
                this.width = 16.pixels
                this.height = 16.pixels
            } childOf chunkHolder
        }
        for (chunk in provider.getCachedChunks()) {
            ChunkComponent(chunk, true).constrain {
                this.x = ((chunk.xPosition - minX) * 16).pixels
                this.y = ((chunk.zPosition - minZ) * 16).pixels
                this.width = 16.pixels
                this.height = 16.pixels
            } childOf chunkHolder
        }

        chunkHolder.constrain {
            this.x = 50.percent - ((maxX - minX) * 8).pixels
            this.y = 50.percent - ((maxZ - minZ) * 8).pixels
        }
    }

    inner class ChunkComponent(val chunk: Chunk, val cache: Boolean = false): UIContainer() {

        private val color = Color(1 / 3f, 1f, 1f, 0f)

        var selected by state(false).apply {
            this.state.onSetValue {
                if (it) {
                    chunks.add(chunk)
                } else {
                    chunks.remove(chunk)
                }
            }
        }
        private val actualColor = map(::selected) {
            if (it) {
                color.withAlpha(0.4f)
            } else {
                color.withAlpha(0.0f)
            }
        }

        val image = UIImage(CompletableFuture.supplyAsync { imageFromChunk(chunk, cache) }).constrain {
            this.width = 100.percent
            this.height = 100.percent
        } childOf this

        val overlay = UIBlock().constrain {
            this.width = 100.percent
            this.height = 100.percent
            this.color = actualColor.state.toConstraint()
        } childOf this

        private var hasClicked = false

        init {
            this.onLeftClick {
                hasClicked = true
            }
            this.onMouseRelease {
                if (cursor == CursorType.SELECT && hasClicked) {
                    selected = !selected
                }
                hasClicked = false
            }
        }

        override fun isPointInside(x: Float, y: Float): Boolean {
            return x >= getLeft()
                    && x <= getRight()
                    && y >= getTop()
                    && y <= getBottom()
        }

        private fun imageFromChunk(chunk: Chunk, shaded: Boolean = false): BufferedImage {
            val pos = BlockPos.MutableBlockPos()
            val img = BufferedImage(16, 16, 1)
            if (chunk is EmptyChunk) return img
            for (x in 0 .. 15) {
                for (z in 0 .. 15) {
                    pos.set(chunk.xPosition * 16 + x, chunk.getHeightValue(x, z) - 1, chunk.zPosition * 16 + z)
                    val color = chunk.getBlockState(pos)?.let { it.block.getMapColor(it) } ?: continue
                    val northHeight = pos.north().let {
                        provider.provideChunk(it).getHeight(it) - 1
                    }
                    val diff = pos.y.compareTo(northHeight) + 1
                    img.setRGB(x, z, if (shaded) Color(color.getMapColor(diff)).darker().rgb else color.getMapColor(diff))
                }
            }
            return img
        }

        override fun hitTest(x: Float, y: Float) = this
    }

    enum class CursorType {
        SELECT,
        MOVE,
        AREA
    }
}