package dev.desco.worlddownloader.core.configs.impl

import dev.desco.worlddownloader.core.configs.Configuration

class SchematicSettings: Configuration() {

    private val worldCategory = category("Entities")
    val entities by setting("Save entities", true)
    val setVisible by setting("Make entities visible", false)
    val invulnerable by setting("Make entities invulnerable", false)
    val silent by setting("Make entities silent", false)
}