package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.FlameStreak
import com.example.ui.theme.GoldStar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Hardware-cached vector mascot avatar for 60 FPS rendering.
 * Only animates via GPU graphicsLayer when [isAnimated] is true.
 */
@Composable
fun OwlMascotAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    isAnimated: Boolean = false
) {
    val bounceOffset = if (isAnimated) {
        val infiniteTransition = rememberInfiniteTransition(label = "owl_anim")
        val value by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -4f,
            animationSpec = infiniteRepeatable(
                animation = tween(420, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "owl_bounce"
        )
        value
    } else {
        0f
    }

    Image(
        painter = painterResource(id = R.drawable.ic_buddy_owl),
        contentDescription = "Buddy öğretmen maskotu",
        modifier = modifier
            .size(size)
            .graphicsLayer {
                if (bounceOffset != 0f) {
                    translationY = bounceOffset
                }
            }
    )
}

private data class ConfettiParticle(
    val angleRad: Float,
    val speedFactor: Float,
    val sizeDp: Float,
    val color: Color,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun ConfettiOverlay(
    triggerCount: Int,
    modifier: Modifier = Modifier
) {
    if (triggerCount <= 0) return

    val progress = remember { Animatable(1f) }
    val particles = remember(triggerCount) {
        val palette = listOf(
            Color(0xFF6C47FF),
            Color(0xFFFF7A2F),
            Color(0xFF10B981),
            Color(0xFF0EA5E9),
            Color(0xFFF59E0B),
            Color(0xFFF43F5E)
        )
        List(36) { index ->
            val rnd = Random(triggerCount * 100 + index)
            ConfettiParticle(
                angleRad = (rnd.nextFloat() * 140f - 160f) * (PI.toFloat() / 180f),
                speedFactor = 0.45f + rnd.nextFloat() * 0.75f,
                sizeDp = 7f + rnd.nextFloat() * 6f,
                color = palette[index % palette.size],
                rotationSpeed = (rnd.nextFloat() - 0.5f) * 720f,
                isCircle = index % 3 == 0
            )
        }
    }

    LaunchedEffect(triggerCount) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val t = progress.value
            val originX = size.width * 0.5f
            val originY = size.height * 0.38f
            val maxRadius = size.minDimension * 0.65f
            val gravity = size.height * 0.42f
            val alpha = (1f - t).coerceIn(0f, 1f)

            particles.forEach { p ->
                val dist = maxRadius * p.speedFactor * kotlin.math.sqrt(t)
                val x = originX + cos(p.angleRad) * dist
                val y = originY + sin(p.angleRad) * dist + gravity * t * t
                val partSize = p.sizeDp.dp.toPx()

                if (p.isCircle) {
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = partSize * 0.5f,
                        center = Offset(x, y)
                    )
                } else {
                    rotate(degrees = p.rotationSpeed * t, pivot = Offset(x, y)) {
                        drawRoundRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(x - partSize / 2f, y - partSize * 0.3f),
                            size = Size(partSize, partSize * 0.6f),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreAndStreakPills(
    score: Int,
    streak: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = GoldStar.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⭐", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = FlameStreak.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, FlameStreak.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
