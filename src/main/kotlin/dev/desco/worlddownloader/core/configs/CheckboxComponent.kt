package dev.desco.worlddownloader.core.configs

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.state.map
import gg.essential.elementa.state.state
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color
import kotlin.reflect.KProperty

class CheckboxComponent(name: String, default: Boolean): UIContainer() {

    private val scale = 1.25

    private var value by state(default)

    private var text = map(::value) {
        if (it) {
            "x"
        } else {
            ""
        }
    }

    private val container = UIBlock(Color(0, 0, 0)).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.width = (9 * scale).pixels
        this.height = (9 * scale).pixels
    } effect OutlineEffect(Color.WHITE, 1f, drawInsideChildren = true) childOf this

    private val checked = UIText("", shadow = false).constrain {
        this.x = CenterConstraint() + (0.5 * scale).pixels
        this.y = CenterConstraint() - (0.5 * scale).pixels
        this.textScale = scale.pixels
    }.apply {
        this.bindText(text.state)
    } childOf container

    private val name = UIText(name, shadow = false).constrain {
        this.x = SiblingConstraint(3f)
        this.y = (1.5 * scale).pixels
        this.textScale = scale.pixels
    } childOf this

    operator fun getValue(thisRef: Configuration, prop: KProperty<*>) = value
    operator fun setValue(thisRef: Configuration, prop: KProperty<*>, value: Boolean) {
        this.value = value
    }

    init {
        onLeftClick {
            value = !value
        }
    }
}