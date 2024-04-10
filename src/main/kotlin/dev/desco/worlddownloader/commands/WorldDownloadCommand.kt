package dev.desco.worlddownloader.commands

import dev.desco.worlddownloader.core.WorldScreen
import gg.essential.api.EssentialAPI
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler

object WorldDownloadCommand: Command("wdl") {

    @DefaultHandler
    fun handle() {
        EssentialAPI.getGuiUtil().openScreen(WorldScreen())
    }
}