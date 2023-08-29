package com.programmersbox.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.programmersbox.common.components.CustomNavigationDrawerItem
import com.programmersbox.common.components.IconsButton
import com.programmersbox.common.components.InfiniteListHandler
import com.programmersbox.common.components.TopicItem
import com.programmersbox.common.viewmodels.FavoritesViewModel
import com.programmersbox.common.viewmodels.RepoViewModel
import com.programmersbox.common.viewmodels.TopicViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moe.tlaster.precompose.navigation.*
import moe.tlaster.precompose.viewmodel.viewModel

@Composable
internal fun App(
    navigator: Navigator = rememberNavigator(),
    onCardClick: (GitHubTopic) -> Unit = {
        NetworkRepository.holder.add(it)
        navigator.navigate(Screen.RepoReadMe.route + "/${it.htmlUrl}")
    },
    onNewTabOpen: (GitHubTopic) -> Unit = {},
    onNewWindow: (GitHubTopic) -> Unit = {},
    onShareClick: (GitHubTopic) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    showLibrariesUsed: () -> Unit = {},
    showFavorites: () -> Unit = {},
) {
    val db = remember { Database() }
    val favoritesViewModel = viewModel { FavoritesViewModel(db) }
    CompositionLocalProvider(
        LocalAppActions provides remember(
            onCardClick,
            onNewTabOpen,
            onNewWindow,
            onShareClick,
            onSettingsClick,
            showLibrariesUsed,
            showFavorites
        ) {
            AppActions(
                onCardClick = onCardClick,
                onNewTabOpen = onNewTabOpen,
                onNewWindow = onNewWindow,
                onShareClick = onShareClick,
                onSettingsClick = onSettingsClick,
                showLibrariesUsed = showLibrariesUsed,
                showFavorites = showFavorites
            )
        },
        LocalMainScrollState provides rememberLazyListState(),
        LocalNavigator provides navigator
    ) {
        NavHost(
            navigator = navigator,
            initialRoute = Screen.App.route
        ) {
            scene(Screen.App.route) {
                TopicDrawerLocation(
                    viewModel { TopicViewModel(db.settingInformation) },
                    favoritesViewModel
                )
            }

            scene(
                Screen.RepoReadMe.route + "/{topic}",
                swipeProperties = SwipeProperties()
            ) { backStack ->
                GithubRepo(
                    vm = viewModel {
                        RepoViewModel(
                            backStack.path<String>("topic").orEmpty()
                        )
                    },
                    favoritesVM = favoritesViewModel,
                    backAction = { navigator.popBackStack() }
                )
            }

            scene(Screen.Favorites.route) {
                FavoritesUi(
                    favoritesVM = favoritesViewModel,
                    backAction = { navigator.popBackStack() }
                )
            }
        }
    }
}

val LocalAppActions = staticCompositionLocalOf<AppActions> { error("No Actions") }

internal val LocalMainScrollState = staticCompositionLocalOf<LazyListState> { error("No Actions") }
val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No Actions") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GithubTopicUI(
    vm: TopicViewModel,
    favoritesVM: FavoritesViewModel,
    navigationIcon: @Composable () -> Unit = {},
) {
    val appActions = LocalAppActions.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val state = LocalMainScrollState.current
    val showButton by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = navigationIcon,
                title = { Text(text = "Github Topics") },
                actions = {
                    Text("Page: ${vm.page}")
                    if (refreshIcon) {
                        IconsButton(
                            onClick = { scope.launch { vm.refresh() } },
                            icon = Icons.Default.Refresh
                        )
                    }
                    IconsButton(
                        onClick = appActions.onSettingsClick,
                        icon = Icons.Default.Settings
                    )
                    AnimatedVisibility(visible = showButton) {
                        IconsButton(
                            onClick = { scope.launch { state.animateScrollToItem(0) } },
                            icon = Icons.Default.ArrowUpward
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        TopicContent(
            modifier = Modifier,
            padding = padding,
            state = state,
            onCardClick = appActions.onCardClick,
            isLoading = vm.isLoading,
            onRefresh = vm::refresh,
            addTopic = vm::addTopic,
            savedTopics = vm.topicList,
            currentTopics = vm.currentTopics,
            onFavoriteClick = { b, topic ->
                if (b) favoritesVM.removeFavorite(topic)
                else favoritesVM.addFavorite(topic)
            },
            isFavorite = { favoritesVM.items.any { f -> f.htmlUrl == it.htmlUrl } },
            newPage = vm::newPage,
            items = vm.items
        )
    }
}

@Composable
internal fun TopicContent(
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    state: LazyListState,
    onCardClick: (GitHubTopic) -> Unit,
    isLoading: Boolean,
    onRefresh: suspend () -> Unit,
    items: List<GitHubTopic>,
    savedTopics: List<String>,
    currentTopics: List<String>,
    addTopic: (String) -> Unit,
    isFavorite: (GitHubTopic) -> Boolean,
    onFavoriteClick: (Boolean, GitHubTopic) -> Unit,
    newPage: suspend () -> Unit,
) {
    SwipeRefreshWrapper(
        paddingValues = padding,
        isRefreshing = isLoading,
        onRefresh = onRefresh
    ) {
        Box(
            modifier = modifier
                .padding(padding)
                .padding(vertical = 2.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
                state = state
            ) {
                items(items) {
                    TopicItemModification(item = it) {
                        TopicItem(
                            item = it,
                            savedTopics = savedTopics,
                            currentTopics = currentTopics,
                            onCardClick = onCardClick,
                            onTopicClick = addTopic,
                            isFavorite = isFavorite(it),
                            onFavoriteClick = { b -> onFavoriteClick(b, it) }
                        )
                    }
                }

                item {
                    val scope = rememberCoroutineScope()
                    ElevatedButton(
                        onClick = { scope.launch { newPage() } },
                        enabled = !isLoading,
                    ) { Text("Load More") }
                }
            }

            ReposScrollBar(state)

            LoadingIndicator(isLoading)

            if (useInfiniteLoader) {
                InfiniteListHandler(
                    listState = state,
                    onLoadMore = newPage,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun TopicDrawer(
    topicViewModel: TopicViewModel,
) {
    TopicDrawer(
        addTopic = topicViewModel::addTopic,
        removeTopic = topicViewModel::removeTopic,
        setTopic = topicViewModel::setTopic,
        toggleSingleTopic = topicViewModel::toggleSingleTopic,
        singleTopic = topicViewModel.singleTopic,
        topicList = topicViewModel.topicList,
        currentTopics = topicViewModel.currentTopics
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun TopicDrawer(
    addTopic: (String) -> Unit,
    removeTopic: (String) -> Unit,
    setTopic: (String) -> Unit,
    toggleSingleTopic: () -> Unit,
    singleTopic: Boolean,
    topicList: List<String>,
    currentTopics: List<String>,
) {
    var topicText by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            val actions = LocalAppActions.current
            TopAppBar(
                title = { Text("Topics") },
                actions = { IconsButton(onClick = actions.showFavorites, icon = Icons.Default.Favorite) }
            )
        },
        bottomBar = {
            BottomAppBar {
                OutlinedTextField(
                    value = topicText,
                    onValueChange = { topicText = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyUp) {
                                if (it.key == Key.Enter) {
                                    addTopic(topicText)
                                    topicText = ""
                                    true
                                } else false
                            } else false
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            addTopic(topicText)
                            topicText = ""
                        }
                    ),
                    label = { Text("Enter Topic") },
                    trailingIcon = {
                        IconsButton(
                            onClick = {
                                addTopic(topicText)
                                topicText = ""
                            },
                            icon = Icons.Default.Add
                        )
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 2.dp),
        ) {
            stickyHeader {
                val scope = rememberCoroutineScope()
                ElevatedCard(
                    onClick = { scope.launch { toggleSingleTopic() } }
                ) {
                    ListItem(
                        headlineContent = { Text("Use ${if (singleTopic) "Single" else "Multiple"} Topic(s)") },
                        trailingContent = {
                            Switch(
                                checked = singleTopic,
                                onCheckedChange = { scope.launch { toggleSingleTopic() } }
                            )
                        }
                    )
                }
            }
            items(topicList) {
                CustomNavigationDrawerItem(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    label = { Text(it) },
                    selected = it in currentTopics,
                    onClick = { setTopic(it) },
                    badge = { IconsButton(onClick = { removeTopic(it) }, icon = Icons.Default.Close) }
                )
            }
        }
    }
}
