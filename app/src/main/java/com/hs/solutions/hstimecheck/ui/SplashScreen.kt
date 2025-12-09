package com.hs.solutions.hstimecheck.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hs.solutions.hstimecheck.R
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*

@Composable
fun SplashScreen() {

    var startAnim by remember { mutableStateOf(false) }

    // Delay antes de começar a animação
    LaunchedEffect(Unit) {
        delay(9000)           // ⬅ espera 300ms antes de animar
        startAnim = true
    }

    // ANIMAÇÃO DE SCALE (zoom)
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.7f,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = ""
    )

    // ANIMAÇÃO DE ALPHA (fade)
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = LinearEasing
        ),
        label = ""
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .scale(scale)  // animação de zoom
                .alpha(alpha)  // animação de fade
        )
    }
}
