package com.example.reproductormultimedia

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.reproductormultimedia.ui.theme.ReproductorMultimediaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReproductorMultimediaTheme {
                AppPrincipal()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPrincipal() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var pantallaActual by remember { mutableStateOf("Inicio") }
    var isVideoFullscreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler {
        if (isVideoFullscreen) {
            isVideoFullscreen = false
        } else if (pantallaActual != "Inicio") {
            pantallaActual = "Inicio"
        } else {
            (context as? ComponentActivity)?.finish()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isVideoFullscreen,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Mi Multimedia",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()

                val opciones = listOf(
                    "Inicio" to "🏠 Inicio",
                    "Música" to "🎵 Música",
                    "Fotos" to "📷 Fotos",
                    "Videos" to "🎬 Videos"
                )

                opciones.forEach { (id, label) ->
                    NavigationDrawerItem(
                        label = { Text(label) },
                        selected = pantallaActual == id,
                        onClick = {
                            pantallaActual = id
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isVideoFullscreen) {
                    TopAppBar(
                        title = { Text(pantallaActual) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Abrir Menú")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            val modifierBase = if (isVideoFullscreen) Modifier.fillMaxSize() else Modifier.padding(innerPadding)

            when (pantallaActual) {
                "Inicio" -> PantallaInicio(onNavigate = { pantallaActual = it }, modifier = modifierBase)
                "Música" -> ReproductorUI(modifier = modifierBase)
                "Fotos" -> GaleriaFotosUI(modifier = modifierBase)
                "Videos" -> ReproductorVideoUI(
                    modifier = modifierBase,
                    isFullscreen = isVideoFullscreen,
                    onFullscreenChange = { isVideoFullscreen = it }
                )
            }
        }
    }
}

@Composable
fun PantallaInicio(onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¿Qué deseas hacer hoy?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        TarjetaMenu(titulo = "🎵 Escuchar Música", onClick = { onNavigate("Música") })
        Spacer(modifier = Modifier.height(16.dp))
        TarjetaMenu(titulo = "📷 Ver Galería", onClick = { onNavigate("Fotos") })
        Spacer(modifier = Modifier.height(16.dp))
        TarjetaMenu(titulo = "🎬 Reproducir Videos", onClick = { onNavigate("Videos") })
    }
}

@Composable
fun TarjetaMenu(titulo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = titulo, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun GaleriaFotosUI(modifier: Modifier = Modifier) {
    val misFotos = listOf(
        R.drawable.nosotros,
        R.drawable.nosotros1,
        R.drawable.nosotros2,
        R.drawable.nosotros3,
        R.drawable.nosotros4,
        R.drawable.nosotros5,
        R.drawable.nosotros6,
    )

    var fotoSeleccionada by remember { mutableStateOf<Int?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(4.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(misFotos.size) { index ->
                val fotoRes = misFotos[index]
                Image(
                    painter = painterResource(id = fotoRes),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { fotoSeleccionada = fotoRes },
                    contentScale = ContentScale.Crop
                )
            }
        }

        fotoSeleccionada?.let { resId ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { fotoSeleccionada = null },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fotoSeleccionada = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

data class Video(val titulo: String, val videoResId: Int)

@Composable
fun ReproductorVideoUI(
    modifier: Modifier = Modifier,
    isFullscreen: Boolean,
    onFullscreenChange: (Boolean) -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val listaVideos = remember {
        listOf(
            Video("Nosotros 1", R.raw.nosotros),
            Video("Nosotros 2", R.raw.nosotros1)
        )
    }
    var indiceActual by remember { mutableStateOf(0) }
    val videoActual = listaVideos[indiceActual]
    var isPlaying by remember { mutableStateOf(true) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(indiceActual) {
        val uri = Uri.parse("android.resource://${context.packageName}/${videoActual.videoResId}")
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        isPlaying = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(if (!isFullscreen) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .background(if (isFullscreen) Color.Black else Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isFullscreen) LocalConfiguration.current.screenHeightDp.dp else 250.dp)
                .background(Color.Black)
                .clickable { showControls = !showControls }
        ) {
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (showControls) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(videoActual.titulo, color = Color.White, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onFullscreenChange(!isFullscreen) }) {
                            Icon(
                                if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                null, tint = Color.White
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        IconButton(onClick = { indiceActual = if (indiceActual > 0) indiceActual - 1 else listaVideos.size - 1 }) {
                            Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                        IconButton(onClick = {
                            isPlaying = !isPlaying
                            if (isPlaying) exoPlayer.play() else exoPlayer.pause()
                        }) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                        IconButton(onClick = { indiceActual = (indiceActual + 1) % listaVideos.size }) {
                            Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                    }

                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)) {
                        VideoProgressBar(exoPlayer)
                    }
                }
            }
        }

        if (!isFullscreen) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Siguientes videos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                listaVideos.forEachIndexed { index, video ->
                    VideoItemRow(
                        video = video,
                        selected = index == indiceActual,
                        onClick = { indiceActual = index }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

data class Cancion(val titulo: String, val artista: String, val audioResId: Int, val portadaResId: Int)

@Composable
fun ReproductorUI(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val listaCanciones = remember {
        listOf(
            Cancion("Breathin", "Ariana Grande", R.raw.ariana_grande_breathin, R.drawable.ariana_grande),
            Cancion("Dandelion", "Ariana Grande", R.raw.ariana_grande_dandelion, R.drawable.ariana_grande),
            Cancion("Daydreamin", "Ariana Grande", R.raw.ariana_grande_daydreamin, R.drawable.ariana_grande)
        )
    }
    var indiceActual by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var tiempoActual by remember { mutableStateOf(0f) }
    var duracionTotal by remember { mutableStateOf(0f) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val cancionActual = listaCanciones[indiceActual]

    LaunchedEffect(indiceActual) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, cancionActual.audioResId)
        duracionTotal = mediaPlayer?.duration?.toFloat() ?: 0f
        if (isPlaying) mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            if (indiceActual < listaCanciones.size - 1) indiceActual++ else {
                isPlaying = false
                mediaPlayer?.seekTo(0)
            }
        }
    }

    LaunchedEffect(isPlaying, mediaPlayer) {
        while (isPlaying && mediaPlayer != null) {
            tiempoActual = mediaPlayer!!.currentPosition.toFloat()
            delay(500)
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    fun formatearTiempo(ms: Int): String {
        val totalSecs = ms / 1000
        return String.format("%02d:%02d", totalSecs / 60, totalSecs % 60)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = cancionActual.portadaResId),
            contentDescription = null,
            modifier = Modifier.size(300.dp).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = cancionActual.titulo, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = cancionActual.artista, fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = formatearTiempo(tiempoActual.toInt()), fontSize = 12.sp)
            Slider(
                value = tiempoActual,
                onValueChange = { tiempoActual = it },
                onValueChangeFinished = { mediaPlayer?.seekTo(tiempoActual.toInt()) },
                valueRange = 0f..(if (duracionTotal > 0f) duracionTotal else 1f),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Text(text = formatearTiempo(duracionTotal.toInt()), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            BotonControl(icon = Icons.Default.SkipPrevious, size = 50) {
                isPlaying = true
                if (mediaPlayer != null && mediaPlayer!!.currentPosition > 3000) {
                    mediaPlayer?.seekTo(0)
                } else {
                    indiceActual = if (indiceActual > 0) indiceActual - 1 else listaCanciones.size - 1
                }
            }
            Button(
                onClick = {
                    isPlaying = !isPlaying
                    if (isPlaying) mediaPlayer?.start() else mediaPlayer?.pause()
                },
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(50)
            ) {
                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(40.dp))
            }
            BotonControl(icon = Icons.Default.SkipNext, size = 50) {
                isPlaying = true
                indiceActual = (indiceActual + 1) % listaCanciones.size
            }
        }
    }
}

@Composable
fun BotonControl(icon: ImageVector, size: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(size.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(size.dp))
    }
}

@Composable
fun VideoProgressBar(player: ExoPlayer) {
    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(1L) }
    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition
            duration = player.duration.coerceAtLeast(1)
            delay(500)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(formatTime(position), color = Color.White, fontSize = 12.sp)
        Slider(
            value = position.toFloat(),
            onValueChange = { player.seekTo(it.toLong()) },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )
        Text(formatTime(duration), color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun VideoItemRow(video: Video, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(video.videoResId) { obtenerMiniaturaVideo(context, video.videoResId) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(120.dp, 80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(video.titulo)
    }
}

fun obtenerMiniaturaVideo(context: Context, resId: Int): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        val uri = Uri.parse("android.resource://${context.packageName}/$resId")
        retriever.setDataSource(context, uri)
        retriever.getFrameAtTime(1000000)
    } catch (e: Exception) {
        null
    } finally {
        retriever.release()
    }
}

fun formatTime(ms: Long): String {
    val total = ms / 1000
    val min = total / 60
    val sec = total % 60
    return "%02d:%02d".format(min, sec)
}