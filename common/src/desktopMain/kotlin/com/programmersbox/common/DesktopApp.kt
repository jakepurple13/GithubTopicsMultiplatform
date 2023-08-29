package com.programmersbox.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import kotlinx.coroutines.flow.flow
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler
import org.cef.CefApp
import org.cef.CefClient
import org.cef.browser.CefBrowser
import java.awt.Component
import java.io.File
import javax.swing.BoxLayout
import javax.swing.JPanel

@Composable
public fun WebView(
    component: Component,
    modifier: Modifier = Modifier,
) {
    SwingPanel(
        background = MaterialTheme.colorScheme.background,
        modifier = modifier,
        factory = {
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(component)
            }
        }
    )
}

public val LocalBrowserHandler: ProvidableCompositionLocal<BrowserHandler> =
    staticCompositionLocalOf { BrowserHandler() }

public class BrowserHandler {
    public val app: CefApp by lazy {
        val builder = CefAppBuilder()
        //Configure the builder instance
        builder.setInstallDir(File("jcef-bundle")) //Default
        builder.setProgressHandler(ConsoleProgressHandler()) //Default
        builder.cefSettings.windowless_rendering_enabled = true //Default - select OSR mode
        builder.addJcefArgs("--force-dark-mode")

        //Set an app handler. Do not use CefApp.addAppHandler(...), it will break your code on MacOSX!
        builder.setAppHandler(object : MavenCefAppHandlerAdapter() {})
        //Build a CefApp instance using the configuration above
        builder.build()
    }

    public val client: CefClient by lazy { app.createClient() }
    public fun createBrowser(url: String): CefBrowser = client.createBrowser(url, false, false)
}