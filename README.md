# WorldDownloader
A World Downloader for Minecraft 1.8.9, with features such as customizable regions, different formats and other utils.

Grab the mod from the [releases page](https://github.com/Desco1/WorldDownloader/releases) or build it yourself.

## Usage
Everything in the mod is after the /wdl command. There is nothing in the mod that doesn't start with that command.
### Selection and editing
![Toolbar](assets/images/toolbar.png)

In the top left corner of the /wdl menu you can find some tools that change how you interact with the chunks.
- **Select**, single click on a chunk to toggle if it's selected or not for download.
- **Move**, click and drag to move the map around, useful for larger downloads.
- **Area**, click and drag to select a square of chunks. If the first chunk you click is selected, the chunks in the area will not be selected, and vice versa. This is pretty much the tool you are most often going to use.
- **Toggle cache**, for larger downloads, this is necessary. Toggling the cache will keep unloaded chunks in memory, and they will show up in the map greyed out, available for download. This toggle is only applied to the world you are currently on, switching worlds will toggle it off.
- **Download**, I should not need to explain this one.

### Settings and formats
After making a selection and clicking the download button, you are moved into the setting screen. There, you are able to select download format and change a few settings about it.\
Currently, there are 2 available formats, World (a regular Vanilla Minecraft save) and Schematic. For the most part, all of their settings are intuitive and not worth to explain, but there are a few exceptions for the World format:
- `Remove block updates` does exactly that. Blocks you edit manually will not notify nearby blocks.
- `Pause random ticks, snow and ice` is a bit of functionality (pausing weather creating snow layers and ice being created/melted) on top of the randomTickSpeed being set to 0. This behaviour exists on all singleplayer worlds, even if they are not world downloads.
- `Tick entities` does exactly that. If toggled off, all saved entities will not tick. This is the equivalent of the NoAI tag on some entities, but none will tick either, such as projectiles. This is useful for servers with custom mob behaviours, where they would revert to vanilla otherwise.
These settings are only available as long as you keep the mod. If you make a download but then remove the mod, they will not have any effect. Except for pausing snow and ice, these are not toggleable after finishing the world download.

After download, World downloads will appear in the /saves/ folder, directly usable for singleplayer; Schematic downloads will appear in the /schematics/ folder.
## Other
You may use any code found in this mod for any purpose unless you are making a public or paid world downloader.\
Thanks to Lucy for helping me choose an icon for the `Toggle Chunk Cache` tool.

### Will I get banned for using this mod on Hypixel?
Maybe. Use at your own risk.
### I got banned for using this mod on Hypixel!!!!
That's rough buddy.
### Does the schematic format work for the Create Mod?
No, but soon™.
### Does the schematic format work for WorldEdit?
Yes. The schematic will be placed as if your position when placing was the point with lowest x, y and z coordinates. Also, WorldEdit doesn't place entities when pasting schematics.
### Does the download include fake player entities?
No, but MAYBE soon™.
### Structure format?
I looked into .structure format for a while, but in the end I decided to drop it due to Structure Blocks not allowing too many blocks. If there is a change in the format that allows for bigger regions OR I discover a way to chain multiple structures together, I may bother to add it.
