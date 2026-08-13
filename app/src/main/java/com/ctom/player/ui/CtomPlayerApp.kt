package com.ctom.player.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ctom.player.data.MediaItemModel
import com.ctom.player.data.MediaKind
import com.ctom.player.playback.PlaybackBus
import com.ctom.player.playback.PlaybackService
import com.ctom.player.ui.components.LiquidBottomBar
import com.ctom.player.ui.components.LiquidGlassCard
import com.ctom.player.ui.components.LiquidIconButton
import com.ctom.player.ui.components.LiquidButton
import com.ctom.player.ui.theme.IceBlue
import com.ctom.player.ui.theme.OceanBlack
import com.ctom.player.ui.theme.OceanSurface
import com.ctom.player.ui.theme.OceanSurfaceRaised
import com.ctom.player.ui.theme.SoftCyan
import com.ctom.player.ui.theme.StrokeBlue
import com.ctom.player.ui.theme.TextMuted
import com.ctom.player.ui.theme.TextPrimary
import com.ctom.player.ui.theme.TextSecondary
import com.ctom.player.ui.theme.Violet
import com.ctom.player.ui.theme.WaterBlue

private enum class Screen { HOME, MUSIC, VIDEO, PLAYLISTS, MORE, SEARCH, NOW_PLAYING }

@Composable
fun CtomPlayerApp(
    hasMediaPermission: Boolean,
    initialDestination: String? = null,
    requestMediaPermission: () -> Unit,
    vm: MainViewModel = viewModel(),
) {
    val library by vm.library.collectAsState()
    val isScanning by vm.isScanning.collectAsState()
    val playback by PlaybackBus.snapshot.collectAsState()
    var screen by remember { mutableStateOf(screenFor(initialDestination)) }

    LaunchedEffect(hasMediaPermission) { vm.refresh(hasMediaPermission) }

    val openItem: (MediaItemModel) -> Unit = { item ->
        PlaybackService.play(LocalContext.current, item)
        screen = Screen.NOW_PLAYING
    }

    Scaffold(
        containerColor = OceanBlack,
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                AnimatedVisibility(visible = playback.uri != null && screen != Screen.NOW_PLAYING) {
                    MiniPlayer(
                        playback = playback,
                        onOpen = { screen = Screen.NOW_PLAYING },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
                LiquidBottomBar {
                    BottomNavItem(Screen.HOME, "HOME", Icons.Outlined.Home, screen) { screen = it }
                    BottomNavItem(Screen.MUSIC, "MUSIC", Icons.Outlined.LibraryMusic, screen) { screen = it }
                    BottomNavItem(Screen.VIDEO, "VIDEO", Icons.Outlined.Movie, screen) { screen = it }
                    BottomNavItem(Screen.PLAYLISTS, "PLAYLISTS", Icons.Outlined.PlaylistPlay, screen) { screen = it }
                    BottomNavItem(Screen.MORE, "MORE", Icons.Outlined.MoreHoriz, screen) { screen = it }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                Screen.HOME -> HomeScreen(
                    library = library,
                    playback = playback,
                    hasPermission = hasMediaPermission,
                    isScanning = isScanning,
                    requestPermission = requestMediaPermission,
                    onSearch = { screen = Screen.SEARCH },
                    onOpen = openItem,
                    onNavigate = { screen = it },
                )

                Screen.MUSIC -> LibraryScreen(
                    title = "Music library",
                    eyebrow = "LOCAL MUSIC",
                    items = library.songs,
                    kind = MediaKind.MUSIC,
                    isScanning = isScanning,
                    hasPermission = hasMediaPermission,
                    requestPermission = requestMediaPermission,
                    onBack = { screen = Screen.HOME },
                    onOpen = openItem,
                    onSearch = { screen = Screen.SEARCH },
                )

                Screen.VIDEO -> LibraryScreen(
                    title = "Video library",
                    eyebrow = "LOCAL VIDEO",
                    items = library.videos,
                    kind = MediaKind.VIDEO,
                    isScanning = isScanning,
                    hasPermission = hasMediaPermission,
                    requestPermission = requestMediaPermission,
                    onBack = { screen = Screen.HOME },
                    onOpen = openItem,
                    onSearch = { screen = Screen.SEARCH },
                )

                Screen.PLAYLISTS -> PlaceholderScreen(
                    title = "Playlists",
                    eyebrow = "YOUR COLLECTIONS",
                    icon = Icons.Outlined.PlaylistPlay,
                    message = "Create playlists on-device and keep your listening organized without sending your library anywhere.",
                    action = "CREATE PLAYLIST",
                    onBack = { screen = Screen.HOME },
                )

                Screen.MORE -> MoreScreen(onBack = { screen = Screen.HOME })

                Screen.SEARCH -> SearchScreen(
                    items = library.all,
                    onBack = { screen = Screen.HOME },
                    onOpen = openItem,
                )

                Screen.NOW_PLAYING -> NowPlayingScreen(
                    playback = playback,
                    onBack = { screen = Screen.HOME },
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    target: Screen,
    label: String,
    icon: ImageVector,
    current: Screen,
    onSelect: (Screen) -> Unit,
) {
    val selected = target == current
    Column(
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable { onSelect(target) }
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = if (selected) IceBlue else TextMuted, modifier = Modifier.size(20.dp))
        Text(label, color = if (selected) IceBlue else TextMuted, fontSize = 9.sp, letterSpacing = 0.8.sp)
    }
}

private fun screenFor(destination: String?): Screen = when (destination) {
    "MUSIC" -> Screen.MUSIC
    "VIDEO" -> Screen.VIDEO
    "PLAYLISTS" -> Screen.PLAYLISTS
    "SEARCH" -> Screen.SEARCH
    "FAVORITES" -> Screen.MORE
    else -> Screen.HOME
}

@Composable
private fun AppHeader(
    eyebrow: String,
    title: String,
    onBack: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            LiquidIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Back", onBack)
            Spacer(Modifier.width(4.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(eyebrow, color = IceBlue, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium)
            Text(title, color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
        }
        if (onSearch != null) LiquidIconButton(Icons.Outlined.Search, "Search your library", onSearch)
    }
}

@Composable
private fun HomeScreen(
    library: com.ctom.player.data.MediaLibrary,
    playback: com.ctom.player.playback.PlaybackSnapshot,
    hasPermission: Boolean,
    isScanning: Boolean,
    requestPermission: () -> Unit,
    onSearch: () -> Unit,
    onOpen: (MediaItemModel) -> Unit,
    onNavigate: (Screen) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            AppHeader(eyebrow = "PERSONAL MEDIA", title = "ctom~player", onSearch = onSearch)
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("A quieter way to listen.", color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Your music and video library, discovered from this device and kept entirely local.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LiquidButton("MUSIC") { onNavigate(Screen.MUSIC) }
                    LiquidButton("VIDEO") { onNavigate(Screen.VIDEO) }
                }
            }
        }
        if (!hasPermission) {
            item {
                PermissionCard(requestPermission)
            }
        } else {
            item {
                HomeSection(
                    title = "Recently played",
                    meta = if (isScanning) "INDEXING" else "${library.songs.size} SONGS  ·  ${library.videos.size} VIDEOS",
                    items = library.songs.take(8),
                    onOpen = onOpen,
                    emptyMessage = "Your listening history will appear here.",
                )
            }
            item {
                HomeSection(
                    title = "Continue watching",
                    meta = "LOCAL VIDEO",
                    items = library.videos.take(8),
                    onOpen = onOpen,
                    emptyMessage = "Videos with saved positions will appear here.",
                )
            }
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickTile("FAVORITES", Icons.Outlined.FavoriteBorder, Modifier.weight(1f))
                QuickTile("PLAYLISTS", Icons.Outlined.QueueMusic, Modifier.weight(1f)) { onNavigate(Screen.PLAYLISTS) }
                QuickTile("FOLDERS", Icons.Outlined.FolderOpen, Modifier.weight(1f))
            }
        }
        if (playback.uri != null) {
            item {
                Text(
                    "Now playing · ${playback.title}",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = TextMuted,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(requestPermission: () -> Unit) {
    LiquidGlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Open your local library", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                "Allow access to local audio and video so ctom~player can index and play it on-device.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            LiquidButton("GRANT MEDIA ACCESS", onClick = requestPermission)
        }
    }
}

@Composable
private fun HomeSection(
    title: String,
    meta: String,
    items: List<MediaItemModel>,
    onOpen: (MediaItemModel) -> Unit,
    emptyMessage: String,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text(meta, color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) {
            Text(emptyMessage, modifier = Modifier.padding(horizontal = 20.dp), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items, key = { "${it.kind}-${it.id}" }) { item ->
                    ArtworkCard(item, onClick = { onOpen(item) })
                }
            }
        }
    }
}

@Composable
private fun ArtworkCard(item: MediaItemModel, onClick: () -> Unit) {
    LiquidGlassCard(modifier = Modifier.width(164.dp), onClick = onClick) {
        Column {
            ArtworkPlaceholder(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                kind = item.kind,
                title = item.title,
            )
            Column(modifier = Modifier.padding(13.dp)) {
                Text(item.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ArtworkPlaceholder(modifier: Modifier, kind: MediaKind, title: String) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                if (kind == MediaKind.VIDEO) listOf(Color(0xFF101F37), Violet.copy(alpha = 0.7f), Color(0xFF082F42))
                else listOf(Color(0xFF0D3445), WaterBlue.copy(alpha = 0.65f), Color(0xFF122045)),
            ),
        ),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            if (kind == MediaKind.VIDEO) "VIDEO" else "♪",
            modifier = Modifier.padding(12.dp),
            color = SoftCyan.copy(alpha = 0.78f),
            fontSize = if (kind == MediaKind.VIDEO) 10.sp else 34.sp,
            letterSpacing = 2.sp,
        )
    }
}

@Composable
private fun QuickTile(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit = {}) {
    LiquidGlassCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = IceBlue, modifier = Modifier.size(21.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = TextSecondary, fontSize = 9.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun LibraryScreen(
    title: String,
    eyebrow: String,
    items: List<MediaItemModel>,
    kind: MediaKind,
    isScanning: Boolean,
    hasPermission: Boolean,
    requestPermission: () -> Unit,
    onBack: () -> Unit,
    onOpen: (MediaItemModel) -> Unit,
    onSearch: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(eyebrow, title, onBack, onSearch)
        if (!hasPermission) {
            PermissionCard(requestPermission)
        } else if (isScanning) {
            Text("Indexing your local media…", color = TextSecondary, modifier = Modifier.padding(20.dp))
        } else if (items.isEmpty()) {
            PlaceholderScreen(
                title = "Nothing here yet",
                eyebrow = if (kind == MediaKind.MUSIC) "MUSIC LIBRARY" else "VIDEO LIBRARY",
                icon = if (kind == MediaKind.MUSIC) Icons.Outlined.LibraryMusic else Icons.Outlined.Movie,
                message = "No compatible local files were found. Add media to this device and refresh.",
                action = "REFRESH LIBRARY",
                onBack = onBack,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items, key = { "${it.kind}-${it.id}" }) { item ->
                    LibraryRow(item, onClick = { onOpen(item) })
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(item: MediaItemModel, onClick: () -> Unit) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ArtworkPlaceholder(Modifier.size(58.dp).clip(RoundedCornerShape(15.dp)), item.kind, item.title)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 13.dp)) {
                Text(item.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(formatDuration(item.durationMs), color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SearchScreen(items: List<MediaItemModel>, onBack: () -> Unit, onOpen: (MediaItemModel) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = items.filter {
        query.isBlank() || listOf(it.title, it.subtitle, it.album).any { field -> field.contains(query, ignoreCase = true) }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader("FIND ANYTHING", "Search", onBack)
        androidx.compose.material3.OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = IceBlue) },
            placeholder = { Text("Songs, artists, albums, videos", color = TextMuted) },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WaterBlue,
                unfocusedBorderColor = StrokeBlue,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = WaterBlue,
            ),
        )
        Spacer(Modifier.height(18.dp))
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(results, key = { "${it.kind}-${it.id}" }) { item -> LibraryRow(item) { onOpen(item) } }
        }
    }
}

@Composable
private fun MiniPlayer(
    playback: com.ctom.player.playback.PlaybackSnapshot,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidGlassCard(modifier = modifier, onClick = onOpen) {
        Row(modifier = Modifier.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            ArtworkPlaceholder(Modifier.size(45.dp).clip(RoundedCornerShape(13.dp)), if (playback.kind == "VIDEO") MediaKind.VIDEO else MediaKind.MUSIC, playback.title)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 11.dp)) {
                Text(playback.title, color = TextPrimary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(playback.subtitle, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { PlaybackService.command(LocalContext.current, PlaybackService.ACTION_TOGGLE) }) {
                Icon(if (playback.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = if (playback.isPlaying) "Pause" else "Play", tint = IceBlue)
            }
            IconButton(onClick = { PlaybackService.command(LocalContext.current, PlaybackService.ACTION_NEXT) }) {
                Icon(Icons.Outlined.SkipNext, contentDescription = "Next", tint = IceBlue)
            }
        }
    }
}

@Composable
private fun NowPlayingScreen(playback: com.ctom.player.playback.PlaybackSnapshot, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 28.dp),
    ) {
        AppHeader(if (playback.kind == "VIDEO") "VIDEO PLAYER" else "NOW PLAYING", "ctom~player", onBack)
        Box(
            modifier = Modifier.padding(horizontal = 28.dp).fillMaxWidth().height(330.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0B3041), Color(0xFF102348), Color(0xFF10142D)))),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (playback.kind == "VIDEO") "VIDEO" else "♪", color = SoftCyan.copy(alpha = 0.86f), fontSize = 58.sp, letterSpacing = 3.sp)
        }
        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp)) {
            Text(playback.title, color = TextPrimary, style = MaterialTheme.typography.headlineMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(playback.subtitle, color = IceBlue, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = { if (playback.durationMs > 0) playback.positionMs.toFloat() / playback.durationMs else 0f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = WaterBlue,
                trackColor = OceanSurfaceRaised,
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(playback.positionMs), color = TextMuted, fontSize = 11.sp)
                Text(formatDuration(playback.durationMs), color = TextMuted, fontSize = 11.sp)
            }
            Spacer(Modifier.height(22.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                LiquidIconButton(Icons.Outlined.SkipPrevious, "Previous", { PlaybackService.command(LocalContext.current, PlaybackService.ACTION_PREVIOUS) }, tint = SoftCyan)
                IconButton(
                    onClick = { PlaybackService.command(LocalContext.current, PlaybackService.ACTION_TOGGLE) },
                    modifier = Modifier.size(68.dp).background(IceBlue, CircleShape),
                ) {
                    Icon(if (playback.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = "Play or pause", tint = OceanBlack, modifier = Modifier.size(32.dp))
                }
                LiquidIconButton(Icons.Outlined.SkipNext, "Next", { PlaybackService.command(LocalContext.current, PlaybackService.ACTION_NEXT) }, tint = SoftCyan)
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidButton("QUEUE") {}
                LiquidButton("SLEEP TIMER") {}
                LiquidButton("FAVORITE") {}
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    eyebrow: String,
    icon: ImageVector,
    message: String,
    action: String,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(eyebrow, title, onBack)
        LiquidGlassCard(modifier = Modifier.padding(20.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = IceBlue, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(16.dp))
                Text(message, color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(18.dp))
                LiquidButton(action) {}
            }
        }
    }
}

@Composable
private fun MoreScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader("SYSTEM", "More", onBack)
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { MoreRow("Widget appearance", "Deep Ocean · medium opacity", Icons.Outlined.Settings) }
            item { MoreRow("Sleep timer", "Stop playback after a set duration", Icons.Outlined.Timer) }
            item { MoreRow("Storage", "Local-only media indexing", Icons.Outlined.FolderOpen) }
            item { MoreRow("About ctom~player", "Version 1.0.0", Icons.Outlined.LibraryMusic) }
            item {
                Text(
                    "Your media never leaves this device. ctom~player does not upload files, collect listening history, or track you.",
                    modifier = Modifier.padding(top = 22.dp),
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun MoreRow(title: String, subtitle: String, icon: ImageVector) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = IceBlue)
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    if (milliseconds <= 0L) return "--:--"
    val totalSeconds = milliseconds / 1000
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}