package com.example.reproductormultimedia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reproductormultimedia.ui.theme.ReproductorMultimediaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReproductorMultimediaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ReproductorUI(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ReproductorUI(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Portada del Disco (Simulada con un cuadro gris por ahora)
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Portada", color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Título y Artista
        Text(
            text = "Canción de Prueba",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Edwin",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Controles (Anterior, Play, Siguiente)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonControl(icon = Icons.Default.SkipPrevious, size = 50)

            // Botón de Play más grande
            Button(
                onClick = { /* Acción de play */ },
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(50), // Círculo
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(40.dp)
                )
            }

            BotonControl(icon = Icons.Default.SkipNext, size = 50)
        }
    }
}

@Composable
fun BotonControl(icon: ImageVector, size: Int) {
    IconButton(onClick = { /* Acción */ }, modifier = Modifier.size(size.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReproductorPreview() {
    ReproductorMultimediaTheme {
        ReproductorUI()
    }
}