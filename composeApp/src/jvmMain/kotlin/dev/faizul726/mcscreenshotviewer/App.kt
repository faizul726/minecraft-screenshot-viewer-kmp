package dev.faizul726.mcscreenshotviewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.awt.Desktop
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
private data class MinecraftScreenshot(val captureTime: Long)

private fun getFolderPath(): File? {
    return when (System.getProperty("os.name").lowercase()) {
        "windows" -> File(System.getenv("APPDATA"), "Minecraft Bedrock\\Users")
        "linux" -> File(System.getenv("HOME"), ".local/share/mcpelauncher/games/com.mojang/Screenshots")
        else -> null
    }
}

private fun getSubFolders(root: File): List<File> {
    val list = mutableListOf<File>()

    for (entry in root.listFiles()) {
        if (entry.isDirectory) {
            val subfolder = File(entry, "games\\com.mojang\\Screenshots")
            if (subfolder.exists() && subfolder.isDirectory) list += subfolder
        }
    }
    
    return list
}

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

private val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)

private fun Long.humanReadableDate(): String = if (this > 0L) sdf.format(this * 1000) else "Unknown date"
private fun File.toBitmap(): ImageBitmap = this.inputStream().readAllBytes().decodeToImageBitmap()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val fileList = remember { mutableStateMapOf<String, Long>() }
        val screenshotList = remember { mutableStateListOf<Triple<File, ImageBitmap, Long>>() }
        var log by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        var isLoading by remember { mutableStateOf(false) }
        val targetFolder = getFolderPath()
        val targetFolderList = remember {
            if (System.getProperty("os.name").lowercase() == "windows") {
                getSubFolders(targetFolder!!)
            } else {
                listOf(targetFolder!!)
            }
        }
        var columnCount by remember { mutableStateOf(3) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                fileList.clear()
                isLoading = true
                scope.launch(Dispatchers.IO) {
                    fileList.clear()
                    screenshotList.clear()

                    try {
                        for (folder in targetFolderList) {
                            folder.listFiles().forEach {
                                if (it.isDirectory) {
                                    for (file in it.listFiles()) {
                                        if (file.name.endsWith(".jpeg", true)) {
                                            val metadata = File(file.absolutePath.replace(".jpeg", ".json", true))
                                            val captureTime = if (metadata.exists()) {
                                                Json.decodeFromString<MinecraftScreenshot>(metadata.readText()).captureTime
                                            } else {
                                                0L
                                            }
                                            screenshotList += (Triple(file, file.toBitmap(), captureTime))
                                            screenshotList.sortBy { f -> f.third }
                                            screenshotList.reverse()
                                        }
                                    }
                                }
                            }
                        }
                        //screenshotList.addAll(tempList)
                        log = "Found ${screenshotList.size} images"
                        isLoading = false
                    } catch (e: Exception) {
                        log += "\n${e.message ?: "Unknown error"}"
                    }
                }
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) LoadingIndicator(Modifier.size(32.dp).padding(end = 8.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Text(if (isLoading) "Loading..." else "Get file list")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Column count: $columnCount")
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = { if (columnCount > 1) columnCount-- },
                    enabled = columnCount != 1
                ) {
                    Text("-", fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = { if (columnCount < 10) columnCount++ },
                    enabled = columnCount != 10
                ) {
                    Text("+", fontWeight = FontWeight.Bold)
                }
            }
            if (log.isNotBlank()) Text(log, fontFamily = FontFamily.Monospace)

            if (!screenshotList.isEmpty()) {
                val state = rememberLazyGridState()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    state = state
                ) {
                    items(screenshotList) { screenshot ->
                        Box(
                            modifier = Modifier.clickable {
                                Desktop.getDesktop().open(screenshot.first)
                            }
                        ) {
                            Image(
                                bitmap = screenshot.second,
                                contentDescription = null,
                                contentScale = ContentScale.Fit
                            )
                            Text(screenshot.third.humanReadableDate(), color = Color.White, modifier = Modifier.background(Color.Black).align(Alignment.BottomStart), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}