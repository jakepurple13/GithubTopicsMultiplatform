
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import com.programmersbox.common.*
import com.programmersbox.common.components.IconsButton
import com.programmersbox.common.viewmodels.FavoritesViewModel
import com.programmersbox.common.viewmodels.RepoViewModel
import com.programmersbox.common.viewmodels.TopicViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection

fun main() = mains()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun mains() {
    val db = Database()
    val closeOnExit = db.settingInformation
        .map { it.closeOnExit }
        .distinctUntilChanged()

    application {
        val vm = remember { AppViewModel() }
        val scope = rememberCoroutineScope()
        val favoritesVM = remember { FavoritesViewModel(db) }
        val topicViewModel = remember { TopicViewModel(db.settingInformation) }
        var showThemeSelector by remember { mutableStateOf(false) }
        val canCloseOnExit by closeOnExit.collectAsState(false)
        val snackbarHostState = remember { SnackbarHostState() }

        CompositionLocalProvider(
            LocalAppActions provides AppActions(
                onCardClick = vm::newTab,
                onNewTabOpen = vm::newTabAndOpen,
                onNewWindow = vm.repoWindows::add,
                onShareClick = {
                    val stringSelection = StringSelection(it.htmlUrl)
                    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(stringSelection, null)
                    Toolkit.getDefaultToolkit().beep()
                    scope.launch { snackbarHostState.showSnackbar("Copied") }
                },
                onSettingsClick = { showThemeSelector = true },
                showFavorites = { vm.selectTab(0) }
            )
        ) {
            Tray(
                icon = painterResource(
                    if (isSystemInDarkTheme()) "github_mark_logo_light.png"
                    else "github_mark_logo.png"
                ),
                onAction = { vm.showTopicWindow = true },
                menu = {
                    if (!canCloseOnExit) Item("Open Window", onClick = { vm.showTopicWindow = true })
                    Item("Quit App", onClick = ::exitApplication)
                }
            )

            WindowWithBar(
                visible = vm.showTopicWindow,
                windowTitle = "GitHub Topics",
                onCloseRequest = if (canCloseOnExit) ::exitApplication
                else {
                    { vm.showTopicWindow = false }
                },
                onPreviewKeyEvent = {
                    if (it.type == KeyEventType.KeyDown) {
                        when {
                            it.isMetaPressed && it.key == Key.W && (vm.selected != 0 && vm.selected != 1) -> {
                                vm.closeSelectedTab()
                                true
                            }

                            it.isCtrlPressed && it.isShiftPressed && it.key == Key.Tab -> {
                                vm.previousTab()
                                true
                            }

                            it.isCtrlPressed && it.key == Key.Tab -> {
                                vm.nextTab()
                                true
                            }

                            it.isMetaPressed && it.isShiftPressed && it.key == Key.T -> {
                                vm.reopenTabOrWindow()
                                true
                            }

                            else -> false
                        }
                    } else false
                },
                snackbarHostState = snackbarHostState,
                frameWindowScope = {
                    MenuOptions(
                        onShowSettings = { showThemeSelector = true },
                        refresh = { scope.launch { topicViewModel.refresh() } },
                        previousTab = vm::previousTab,
                        nextTab = vm::nextTab,
                        closeTabEnabled = vm.selected != 0 && vm.selected != 1,
                        closeTab = vm::closeSelectedTab,
                        canReopen = vm.canReopen,
                        reopen = vm::reopenTabOrWindow,
                        hasHistory = vm.canReopen,
                        openHistory = vm::openHistory
                    )
                }
            ) {
                Scaffold(
                    topBar = {
                        Column {
                            ScrollableTabRow(
                                selectedTabIndex = vm.selected.coerceIn(0, vm.browserTab.tabbed.size - 1),
                                edgePadding = 0.dp
                            ) {
                                vm.browserTab.tabsList.forEach { (index, tab) ->
                                    when (tab) {
                                        is Tabs.PinnedTab<TabType> -> {
                                            when (index) {
                                                0 -> {
                                                    LeadingIconTab(
                                                        selected = vm.selected == 0,
                                                        text = { Text("Favorites") },
                                                        onClick = { vm.selectTab(0) },
                                                        icon = { Icon(Icons.Default.Favorite, null) }
                                                    )
                                                }

                                                1 -> {
                                                    LeadingIconTab(
                                                        selected = vm.selected == 1,
                                                        text = { Text("Topics") },
                                                        onClick = { vm.selectTab(1) },
                                                        icon = { Icon(Icons.Default.Topic, null) }
                                                    )
                                                }
                                            }
                                        }

                                        is Tabs.Tab<TabType> -> {
                                            val topic = (tab.data as TabType.Normal).topic
                                            val hoverInteraction = remember { MutableInteractionSource() }
                                            val isHovering by hoverInteraction.collectIsHoveredAsState()
                                            LeadingIconTab(
                                                selected = vm.selected == index,
                                                text = { Text(topic.name) },
                                                onClick = { vm.selectTab(index) },
                                                modifier = Modifier.onPointerEvent(PointerEventType.Press) {
                                                    val isMiddleClick = it.button == PointerButton.Tertiary
                                                    if (isMiddleClick) vm.closeTab(tab)
                                                },
                                                icon = {
                                                    IconsButton(
                                                        onClick = { vm.closeTab(tab) },
                                                        icon = Icons.Default.Close,
                                                        modifier = Modifier.hoverable(hoverInteraction),
                                                        colors = IconButtonDefaults.iconButtonColors(
                                                            contentColor = if (isHovering) Color.Red
                                                            else LocalContentColor.current
                                                        )
                                                    )
                                                }
                                            )
                                        }

                                        is Tabs.EndTab<TabType> -> {
                                            AnimatedVisibility(vm.showHistory) {
                                                LeadingIconTab(
                                                    selected = vm.selected == vm.browserTab.tabbed.lastIndex,
                                                    text = { Text("History") },
                                                    onClick = {},
                                                    icon = { Icon(Icons.Default.History, null) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                ) { p ->
                    Box(modifier = Modifier.padding(p)) {
                        when (val tab = vm.browserTab.selectedTab()) {
                            is Tabs.PinnedTab<TabType> -> {
                                when ((tab.data as TabType.Pinned).index) {
                                    0 -> FavoritesUi(favoritesVM) { vm.selectTab(0) }
                                    1 -> TopicDrawerLocation(topicViewModel, favoritesVM)
                                }
                            }

                            is Tabs.Tab<TabType> -> {
                                key(vm.selected, vm.browserTab.refreshKey) {
                                    (tab.data as? TabType.Normal)?.topic?.let { topic ->
                                        val repo = remember {
                                            RepoViewModel(topic.htmlUrl)
                                        }
                                        GithubRepo(
                                            vm = repo,
                                            favoritesVM = favoritesVM,
                                            backAction = { vm.closeTab(tab) }
                                        )
                                        val browser = LocalBrowser.current
                                        DisposableEffect(Unit) {
                                            onDispose { browser.close(true) }
                                        }
                                    }
                                }
                            }

                            is Tabs.EndTab<TabType> -> {
                                HistoryUi(
                                    vm = vm,
                                    favoritesVM = favoritesVM,
                                    backAction = { vm.selectTab(1) }
                                )
                            }

                            else -> Unit
                        }
                    }
                }
            }

            vm.repoWindows.forEach { topic ->
                val topicSnackbarHostState = remember { SnackbarHostState() }

                CompositionLocalProvider(
                    LocalAppActions provides LocalAppActions.current.copy(
                        onShareClick = {
                            val stringSelection = StringSelection(it.htmlUrl)
                            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                            clipboard.setContents(stringSelection, null)
                            Toolkit.getDefaultToolkit().beep()
                            scope.launch { topicSnackbarHostState.showSnackbar("Copied") }
                        }
                    )
                ) {
                    WindowWithBar(
                        windowTitle = topic.name,
                        onCloseRequest = { vm.closeWindow(topic) },
                        snackbarHostState = topicSnackbarHostState,
                        frameWindowScope = {
                            MenuOptions(
                                onShowSettings = { showThemeSelector = true }
                            )
                        }
                    ) {
                        val repo = remember { RepoViewModel(topic.htmlUrl) }
                        GithubRepo(
                            vm = repo,
                            favoritesVM = favoritesVM,
                            backAction = { vm.closeWindow(topic) }
                        )

                        val browser = LocalBrowser.current

                        DisposableEffect(Unit) {
                            onDispose { browser.close(true) }
                        }
                    }
                }
            }

            WindowWithBar(
                windowTitle = "Settings",
                onCloseRequest = { showThemeSelector = false },
                visible = showThemeSelector
            ) {
                SettingsScreen {
                    NavigationDrawerItem(
                        label = { Text("Close Application on Exit? Or just hide window?") },
                        badge = {
                            Switch(
                                checked = canCloseOnExit,
                                onCheckedChange = { scope.launch { db.changeCloseOnExit(it) } }
                            )
                        },
                        onClick = { scope.launch { db.changeCloseOnExit(!canCloseOnExit) } },
                        selected = true,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Divider()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FrameWindowScope.MenuOptions(
    onShowSettings: () -> Unit,
    refresh: (() -> Unit)? = null,
    previousTab: () -> Unit = {},
    nextTab: () -> Unit = {},
    closeTabEnabled: Boolean = false,
    closeTab: () -> Unit = {},
    canReopen: Boolean = false,
    reopen: () -> Unit = {},
    hasHistory: Boolean = false,
    openHistory: () -> Unit = {}
) {
    MenuBar {
        Menu("Settings", mnemonic = 'T') {
            Item(
                "Settings",
                onClick = onShowSettings
            )

            Item(
                "History",
                enabled = hasHistory,
                onClick = openHistory
            )
        }

        refresh?.let { r ->
            Menu("Options") {
                Item(
                    "Refresh",
                    onClick = r,
                    shortcut = KeyShortcut(
                        meta = hostOs == OS.MacOS,
                        ctrl = hostOs == OS.Windows || hostOs == OS.Linux,
                        key = Key.R
                    )
                )

                Item("Libraries Used", onClick = LocalAppActions.current.showLibrariesUsed)
            }
        }
        Menu("Window") {
            Item(
                "Previous Tab",
                onClick = previousTab,
                shortcut = KeyShortcut(
                    ctrl = true,
                    shift = true,
                    key = Key.Tab
                )
            )
            Item(
                "Next Tab",
                onClick = nextTab,
                shortcut = KeyShortcut(
                    ctrl = true,
                    key = Key.Tab
                )
            )
            Item(
                "Close Tab",
                enabled = closeTabEnabled,
                onClick = closeTab,
                shortcut = KeyShortcut(
                    meta = hostOs == OS.MacOS,
                    ctrl = hostOs == OS.Windows || hostOs == OS.Linux,
                    key = Key.W
                )
            )

            Item(
                "Reopen Tab/Window",
                enabled = canReopen,
                onClick = reopen,
                shortcut = KeyShortcut(
                    meta = hostOs == OS.MacOS,
                    ctrl = hostOs == OS.Windows || hostOs == OS.Linux,
                    shift = true,
                    key = Key.T
                )
            )
        }
    }
}