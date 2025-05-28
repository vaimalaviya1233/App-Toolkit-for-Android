package com.d4rk.android.apps.apptoolkit.app.compass.ui

import android.graphics.Typeface
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.ScreenLockRotation
import androidx.compose.material.icons.outlined.WifiTetheringError
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// -- Sensor State (copied from original) --
@Stable
interface CompassSensorState {
    val currentBearing: Float
    val isAvailable: Boolean
}

class PreviewCompassSensorState(initialBearing: Float = 0f) : CompassSensorState {
    private var _bearing by mutableFloatStateOf(initialBearing)
    override val currentBearing get() = _bearing
    override val isAvailable = true

    @Composable
    fun SimulateBearingChanges() {
        LaunchedEffect(Unit) {
            // kotlinx.coroutines.delay(1000) // Initial delay
            while (true) {
                kotlinx.coroutines.delay(50) // Update frequency
                _bearing = (_bearing + 0.5f) % 360 // Slower rotation for preview
            }
        }
    }
}

@Composable
fun rememberCompassSensorState(): CompassSensorState =
        remember { PreviewCompassSensorState(320f) } // Start at 320 for image match

// -- Colors and Styles (derived from image) --
val DarkBackground = Color(0xFF0A0A0A) // Very dark gray, almost black like image
val CompassTitleColor = Color.White
val IconColor = Color.White
val CompassTickColor = Color.White
val CompassNumberColor = Color.White
val CompassCardinalColor = Color.White
val CurrentBearingIndicatorColor = Color(0xFFADCFF7) // Light blue from image
val CenterDegreeColor = Color.White
val CenterDirectionColor = Color.White
val BottomTextColor = Color(0xFFB0B0B0) // Slightly dimmer white

// Helper for interactive preview detection
val LocalInspectionMode = compositionLocalOf { false }


// -- Main App Composable for Preview --
@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun CompassAppPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = DarkBackground,
            onBackground = Color.White,
            surface = DarkBackground,
            onSurface = Color.White,
            primary = CurrentBearingIndicatorColor,
            onPrimary = Color.Black,
            surfaceVariant = Color(0xFF2C2C2E),
            onSurfaceVariant = Color(0xFFCCCCCC)
        )
    ) {
        val sensorState = rememberCompassSensorState()
        // Activate simulation for interactive preview
        if (sensorState is PreviewCompassSensorState && LocalInspectionMode.current) {
            sensorState.SimulateBearingChanges()
        }

        Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
            CompassScreen(
                paddingValues = PaddingValues(top = 12.dp), // Simulate some top system padding
                sensorState = sensorState
            )
        }
    }
}


// -- The Screen --
@Composable
fun CompassScreen(
    paddingValues: PaddingValues,
    sensorState: CompassSensorState
) {
    Column(
        modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
    ) {
        CompassTopBar(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                    .weight(1f) // Occupy available vertical space
                    .fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the compass assembly
        ) {
            if (!sensorState.isAvailable) {
                Text(
                    "Compass sensor not available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                // This Box defines the size and centers the CompassDisplay and CentralBearingText
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                            .fillMaxWidth(0.88f) // Compass assembly takes up 88% of screen width
                            .aspectRatio(1f)     // Make it a square
                ) {
                    CompassDisplay(
                        bearing = sensorState.currentBearing,
                        modifier = Modifier.fillMaxSize() // CompassDisplay fills this square
                    )
                    CentralBearingText(
                        bearing = sensorState.currentBearing
                        // CentralBearingText is implicitly centered by the parent Box
                    )
                }
            }
        }

        CompassBottomBar(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}

@Composable
fun CompassTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Compass",
            color = CompassTitleColor,
            fontSize = 30.sp,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.ScreenLockRotation, // Icon like crossed out phone rotation
            contentDescription = "Screen Rotation Locked",
            tint = IconColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Icon(
            imageVector = Icons.Outlined.WifiTetheringError, // Placeholder for sensor accuracy/calibration
            contentDescription = "Sensor Status",
            tint = IconColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Settings",
            tint = IconColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun CompassDisplay(
    bearing: Float,
    modifier: Modifier = Modifier // This modifier will define the size of the CompassDisplay
) {
    val animatedBearing by animateFloatAsState(
        targetValue = bearing,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bearingAnimation"
    )

    val density = LocalDensity.current

    // Paints are remembered and depend on density
    val textPaint = remember(density, CompassNumberColor) { // Add color to key if it can change
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = CompassNumberColor.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = density.run { 12.sp.toPx() }
        }
    }
    val cardinalPaint = remember(density, CompassCardinalColor) { // Add color to key
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = CompassCardinalColor.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = density.run { 20.sp.toPx() }
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    // State to hold the size of the component once measured
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
                .onSizeChanged { newSize ->
                    componentSize = newSize
                }
                .clipToBounds(), // Clip drawing to bounds, good practice with dynamic sized drawing
        contentAlignment = Alignment.TopCenter // Horizontally centers the fixed indicator
    ) {
        // Only proceed with drawing if the component has a non-zero size
        if (componentSize != IntSize.Zero) {
            val canvasWidth = componentSize.width.toFloat()
            val canvasHeight = componentSize.height.toFloat() // Assuming parent makes it square

            // Calculate dimensions based on the actual measured size
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            // Overall radius for the compass elements
            val compassRoseRadius = minOf(canvasWidth, canvasHeight) / 2f * 0.92f
            // Radius for drawing ticks
            val tickCircleRadius = compassRoseRadius * 0.9f
            // Radius for placing degree numbers
            val numberTextRadius = compassRoseRadius
            // Radius for placing N, E, S, W
            val cardinalTextRadius = tickCircleRadius * 0.70f

            // Canvas for the rotating compass rose
            Canvas(
                modifier = Modifier
                        .matchParentSize() // Canvas takes the full size of the parent Box
                        .graphicsLayer { rotationZ = -animatedBearing }
            ) {
                // Draw Ticks
                for (deg in 0 until 360 step 2) {
                    val angleRad = Math.toRadians(deg.toDouble() - 90.0).toFloat() // 0 deg is North

                    val isMajor = deg % 30 == 0
                    val isMedium = deg % 10 == 0 && !isMajor
                    val tickStartFactor: Float
                    val strokeWidthPx: Float

                    when {
                        isMajor -> {
                            tickStartFactor = 0.80f
                            strokeWidthPx = with(density) { 2.dp.toPx() }
                        }
                        isMedium -> {
                            tickStartFactor = 0.88f
                            strokeWidthPx = with(density) { 1.5.dp.toPx() }
                        }
                        else -> { // Minor ticks
                            tickStartFactor = 0.92f
                            strokeWidthPx = with(density) { 1.dp.toPx() }
                        }
                    }

                    val start = Offset(
                        center.x + tickCircleRadius * tickStartFactor * cos(angleRad),
                        center.y + tickCircleRadius * tickStartFactor * sin(angleRad)
                    )
                    val end = Offset(
                        center.x + tickCircleRadius * cos(angleRad),
                        center.y + tickCircleRadius * sin(angleRad)
                    )
                    drawLine(CompassTickColor, start, end, strokeWidth = strokeWidthPx, cap = StrokeCap.Butt)
                }

                // Draw Numbers and Cardinal Letters
                for (deg in 0 until 360 step 30) {
                    val angleRad = Math.toRadians(deg.toDouble() - 90.0).toFloat()

                    // Degree Numbers
                    val textX = center.x + numberTextRadius * cos(angleRad)
                    val textY = center.y + numberTextRadius * sin(angleRad) + textPaint.textSize / 3f
                    drawIntoCanvas { nativeCanvas ->
                        nativeCanvas.nativeCanvas.save()
                        nativeCanvas.nativeCanvas.translate(textX, textY)
                        nativeCanvas.nativeCanvas.rotate(animatedBearing + deg.toFloat()) // Counter-rotate text
                        nativeCanvas.nativeCanvas.drawText(deg.toString(), 0f, 0f, textPaint)
                        nativeCanvas.nativeCanvas.restore()
                    }

                    // Cardinal Letters
                    val cardinalLetter = when (deg) {
                        0 -> "N"; 90 -> "E"; 180 -> "S"; 270 -> "W"; else -> null
                    }
                    cardinalLetter?.let {
                        val cardinalX = center.x + cardinalTextRadius * cos(angleRad)
                        val cardinalY = center.y + cardinalTextRadius * sin(angleRad) + cardinalPaint.textSize / 3f
                        drawIntoCanvas { nativeCanvas ->
                            nativeCanvas.nativeCanvas.save()
                            nativeCanvas.nativeCanvas.translate(cardinalX, cardinalY)
                            nativeCanvas.nativeCanvas.rotate(animatedBearing + deg.toFloat()) // Counter-rotate text
                            nativeCanvas.nativeCanvas.drawText(it, 0f, 0f, cardinalPaint)
                            nativeCanvas.nativeCanvas.restore()
                        }
                    }
                }
            }

            // Fixed Current Bearing Indicator (Blue Bar)
            val indicatorWidth = 6.dp
            val indicatorHeight = 30.dp
            val indicatorHeightPx = with(density) { indicatorHeight.toPx() }

            // Calculate the Y offset for the indicator.
            // This offset is from the top edge of the parent Box (which has Alignment.TopCenter for its children).
            val indicatorOffsetY = with(density) {
                // (canvasHeight / 2f) is the Y-coordinate of the center of the compass.
                // tickCircleRadius is the radius of the circle on which ticks end.
                // (indicatorHeightPx * 0.1f) is a small gap above the tick circle.
                // The result is the Y-coordinate for the TOP of the indicator.
                ((canvasHeight / 2f) - tickCircleRadius - (indicatorHeightPx * 0.1f)).toDp()
            }

            Box(
                modifier = Modifier
                        // .align(Alignment.TopCenter) // Already handled by parent Box's contentAlignment
                        .offset(y = indicatorOffsetY)
                        .width(indicatorWidth)
                        .height(indicatorHeight)
                        .background(CurrentBearingIndicatorColor)
            )
        }
    }
}


@Composable
fun CentralBearingText(
    bearing: Float,
    modifier: Modifier = Modifier
) {
    val roundedBearing = (bearing.toInt() % 360 + 360) % 360

    val direction = remember(roundedBearing) { // Using ranges from image example (320 = Northwest)
        when {
            roundedBearing >= 338 || roundedBearing < 23 -> "North"
            roundedBearing < 68 -> "Northeast"
            roundedBearing < 113 -> "East"
            roundedBearing < 158 -> "Southeast"
            roundedBearing < 203 -> "South"
            roundedBearing < 248 -> "Southwest"
            roundedBearing < 293 -> "West"
            else -> "Northwest" // 292.5 to 337.5
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$roundedBearing°",
            color = CenterDegreeColor,
            fontSize = 72.sp,
            fontWeight = FontWeight.Light // Thin font for large degrees
        )
        Text(
            text = direction,
            color = CenterDirectionColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun CompassBottomBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Explore,
            contentDescription = "North Type",
            tint = BottomTextColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Magnetic North",
            color = BottomTextColor,
            fontSize = 16.sp
        )
    }
}