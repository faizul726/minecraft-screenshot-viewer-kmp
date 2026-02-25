import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.3.10"
}

kotlin {
    jvm()

    /*linuxX64 {
        binaries {
            executable()
        }
    }

    mingwX64 {
        binaries {
            executable()
        }
    }*/
    
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "dev.faizul726.mcscreenshotviewer.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Rpm, TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.faizul726.mcscreenshotviewer"
            packageVersion = "1.0.0"
        }
    }
}
