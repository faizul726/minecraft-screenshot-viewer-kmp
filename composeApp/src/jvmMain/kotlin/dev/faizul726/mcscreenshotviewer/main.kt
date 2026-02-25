package dev.faizul726.mcscreenshotviewer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        Dialog(Frame(), e.message ?: "Error").apply {
            layout = FlowLayout()
            val label = Label(e.message)
            add(label)
            val button = Button("OK").apply {
                addActionListener { dispose() }
            }
            add(button)
            setSize(300,300)
            isVisible = true
        }
    }

    application {
        throw Exception("My custom error!")
        Window(
            onCloseRequest = ::exitApplication,
            title = "Minecraft Screenshot Viewer",
        ) {
            App()
        }
    }
}
