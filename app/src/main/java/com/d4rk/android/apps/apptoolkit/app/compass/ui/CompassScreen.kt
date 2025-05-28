package com.d4rk.android.apps.apptoolkit.app.compass.ui

import android.graphics.Typeface
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Stable
interface CompassSensorState {
    val currentBearing : Float
    val isAvailable : Boolean
}

class PreviewCompassSensorState(initialBearing : Float = 0f) : CompassSensorState {
    private var _bearing by mutableFloatStateOf(initialBearing)
    override val currentBearing get() = _bearing
    override val isAvailable = true
}

@Composable
fun rememberCompassSensorState() : CompassSensorState = remember { PreviewCompassSensorState(320f) }

@Composable
fun CompassScreen(
    paddingValues : PaddingValues , sensorState : CompassSensorState
) {
    Column(
        modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
    ) {
        Box(
            modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth() , contentAlignment = Alignment.Center
        ) {
            if (! sensorState.isAvailable) {
                Text(
                    "Compass sensor not available" , color = MaterialTheme.colorScheme.onSurfaceVariant , style = MaterialTheme.typography.titleMedium
                )
            }
            else {

                Box(
                    contentAlignment = Alignment.Center , modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .aspectRatio(1f)
                ) {
                    CompassDisplay(
                        bearing = sensorState.currentBearing , modifier = Modifier.fillMaxSize()
                    )
                    CentralBearingText(
                        bearing = sensorState.currentBearing

                    )
                }
            }
        }

        CompassBottomBar(
            modifier = Modifier.padding(horizontal = 16.dp , vertical = 24.dp)
        )
    }
}

@Composable
fun CompassDisplay(
    bearing: Float, modifier: Modifier = Modifier
) {
    val animatedBearing by animateFloatAsState(
        targetValue = bearing, animationSpec = spring(stiffness = Spring.StiffnessLow), label = "bearingAnimation"
    )

    val density = LocalDensity.current
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val textPaint = remember(density, onSurfaceColor) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = density.run { 12.sp.toPx() }
            color = onSurfaceColor.toArgb()
        }
    }
    val cardinalPaint = remember(density, onSurfaceColor) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = density.run { 20.sp.toPx() }
            typeface = Typeface.DEFAULT_BOLD
            color = onSurfaceColor.toArgb()
        }
    }

    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
                .onSizeChanged { newSize ->
                    componentSize = newSize
                }
                .clipToBounds(), contentAlignment = Alignment.TopCenter
    ) {

        if (componentSize != IntSize.Zero) {
            val canvasWidth = componentSize.width.toFloat()
            val canvasHeight = componentSize.height.toFloat()

            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

            val compassRoseRadius = minOf(canvasWidth, canvasHeight) / 2f * 0.92f
            val tickCircleRadius = compassRoseRadius * 0.9f
            val numberTextRadius = compassRoseRadius
            val cardinalTextRadius = tickCircleRadius * 0.70f

            Canvas(
                modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { rotationZ = -animatedBearing }
            ) {

                for (deg in 0 until 360 step 2) {
                    val isMajor = deg % 30 == 0
                    val isMedium = deg % 10 == 0 && !isMajor
                    val tickStartFactor: Float
                    val strokeWidthPx: Float
                    val tickColor = onSurfaceColor

                    when {
                        isMajor -> {
                            tickStartFactor = 0.80f
                            strokeWidthPx = with(density) { 2.dp.toPx() }
                        }

                        isMedium -> {
                            tickStartFactor = 0.88f
                            strokeWidthPx = with(density) { 1.5.dp.toPx() }
                        }

                        else -> {
                            tickStartFactor = 0.92f
                            strokeWidthPx = with(density) { 1.dp.toPx() }
                        }
                    }

                    val angleRad = Math.toRadians(deg.toDouble() - 90.0).toFloat()
                    val startX = center.x + tickCircleRadius * tickStartFactor * cos(angleRad)
                    val startY = center.y + tickCircleRadius * tickStartFactor * sin(angleRad)
                    val endX = center.x + tickCircleRadius * cos(angleRad)
                    val endY = center.y + tickCircleRadius * sin(angleRad)

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeWidthPx
                    )
                }

                for (deg in 0 until 360 step 30) {
                    val angleRad = Math.toRadians(deg.toDouble() - 90.0).toFloat()

                    val textX = center.x + numberTextRadius * cos(angleRad)
                    val textY = center.y + numberTextRadius * sin(angleRad) + textPaint.textSize / 3f
                    drawIntoCanvas { nativeCanvas ->
                        nativeCanvas.nativeCanvas.save()
                        nativeCanvas.nativeCanvas.translate(textX, textY)
                        nativeCanvas.nativeCanvas.rotate(animatedBearing + deg.toFloat())
                        nativeCanvas.nativeCanvas.drawText(deg.toString(), 0f, 0f, textPaint)
                        nativeCanvas.nativeCanvas.restore()
                    }

                    val cardinalLetter = when (deg) {
                        0 -> "N"; 90 -> "E"; 180 -> "S"; 270 -> "W"; else -> null
                    }
                    cardinalLetter?.let {
                        val cardinalX = center.x + cardinalTextRadius * cos(angleRad)
                        val cardinalY = center.y + cardinalTextRadius * sin(angleRad) + cardinalPaint.textSize / 3f
                        drawIntoCanvas { nativeCanvas ->
                            nativeCanvas.nativeCanvas.save()
                            nativeCanvas.nativeCanvas.translate(cardinalX, cardinalY)
                            nativeCanvas.nativeCanvas.rotate(animatedBearing + deg.toFloat())
                            nativeCanvas.nativeCanvas.drawText(it, 0f, 0f, cardinalPaint)
                            nativeCanvas.nativeCanvas.restore()
                        }
                    }
                }
            }

            val indicatorWidth = 6.dp
            val indicatorHeight = 30.dp
            val indicatorHeightPx = with(density) { indicatorHeight.toPx() }

            val indicatorOffsetY = with(density) {

                ((canvasHeight / 2f) - tickCircleRadius - (indicatorHeightPx * 0.1f)).toDp()
            }

            Box(
                modifier = Modifier
                        .offset(y = indicatorOffsetY)
                        .width(indicatorWidth)
                        .height(indicatorHeight)

            )
        }
    }
}

@Composable
fun CentralBearingText(
    bearing : Float , modifier : Modifier = Modifier
) {
    val roundedBearing = (bearing.toInt() % 360 + 360) % 360

    val direction = remember(roundedBearing) {
        when {
            roundedBearing >= 338 || roundedBearing < 23 -> "North"
            roundedBearing < 68 -> "Northeast"
            roundedBearing < 113 -> "East"
            roundedBearing < 158 -> "Southeast"
            roundedBearing < 203 -> "South"
            roundedBearing < 248 -> "Southwest"
            roundedBearing < 293 -> "West"
            else -> "Northwest"
        }
    }

    Column(
        modifier = modifier , horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$roundedBearing°" , fontSize = 72.sp , fontWeight = FontWeight.Light
        )
        Text(
            text = direction , fontSize = 20.sp , fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun CompassBottomBar(modifier : Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth() , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Explore , contentDescription = "North Type" , modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Magnetic North" , fontSize = 16.sp
        )
    }
}