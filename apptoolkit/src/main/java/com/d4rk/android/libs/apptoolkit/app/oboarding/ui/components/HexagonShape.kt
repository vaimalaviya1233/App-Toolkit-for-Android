package com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components

import androidx.collection.FloatFloatPair // Import FloatFloatPair
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.PointTransformer
import androidx.graphics.shapes.RoundedPolygon
import kotlin.math.min

@Composable
fun HexagonShapeDemo() {
    val hexagon = remember {
        RoundedPolygon(
            numVertices = 6, rounding = CornerRounding(0.2f) // 20% rounding
        )
    }

    val clip: Shape = remember(hexagon) {
        RoundedPolygonShape(polygon = hexagon)
    }

    Box(
        modifier = Modifier
                .size(200.dp)
                .clip(clip)
                .background(MaterialTheme.colorScheme.secondary)
    ) {
        Text(
            text = "Hello Compose",
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

class RoundedPolygonShape(private val polygon: RoundedPolygon) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()

        // Calculate scale factor based on target size
        val scale = min(size.width, size.height) / 2f

        // Apply scale and translate polygon to center
        val transformer = PointTransformer { x, y ->
            val tx = (x - polygon.centerX) * scale + size.width / 2f
            val ty = (y - polygon.centerY) * scale + size.height / 2f
            FloatFloatPair(tx, ty) // Use FloatFloatPair
        }

        val transformedPolygon = polygon.transformed(transformer)

        val cubics = transformedPolygon.cubics
        if (cubics.isNotEmpty()) {
            val first = cubics.first()
            path.moveTo(first.anchor0X, first.anchor0Y)
            for (cubic in cubics) {
                path.cubicTo(
                    cubic.control0X,
                    cubic.control0Y,
                    cubic.control1X,
                    cubic.control1Y,
                    cubic.anchor1X,
                    cubic.anchor1Y
                )
            }
            path.close()
        }

        return Outline.Generic(path)
    }

    // Removed the duplicate createOutline method
}