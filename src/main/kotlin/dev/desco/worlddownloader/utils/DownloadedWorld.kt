package dev.desco.worlddownloader.utils

interface DownloadedWorld {

    val isDownloadedWorld: Boolean
    val isTickingBlocks: Boolean
    val isTickingEntities: Boolean
}