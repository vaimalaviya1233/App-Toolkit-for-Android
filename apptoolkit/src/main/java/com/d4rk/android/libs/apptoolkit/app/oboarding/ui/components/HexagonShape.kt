package com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components

import androidx.collection.FloatFloatPair
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Cubic
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.PointTransformer
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import kotlin.math.min

val defaultRounding = CornerRounding(radius = 0.15f)

val shapePool = listOf(
    RoundedPolygon(3, rounding = defaultRounding),
    RoundedPolygon(4, rounding = defaultRounding),
    RoundedPolygon(5, rounding = defaultRounding),
    RoundedPolygon(6, rounding = defaultRounding),
    RoundedPolygon(8, rounding = defaultRounding),
    RoundedPolygon(12, rounding = defaultRounding),
    RoundedPolygon.star(5, innerRadius = 0.5f, rounding = defaultRounding),
    RoundedPolygon.star(7, innerRadius = 0.6f, rounding = defaultRounding),
)

@Composable
fun AnimatedMorphingShapeContainer(imageVector: ImageVector) {
    var startShape by remember { mutableStateOf(shapePool[1]) }
    var endShape by remember { mutableStateOf(shapePool.random()) }
    var morph by remember(startShape, endShape) { mutableStateOf(Morph(startShape, endShape)) }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow
                )
            )
            startShape = endShape
            endShape = shapePool.filterNot { it === startShape }.random()
            morph = Morph(startShape, endShape)
        }
    }

    val shape: Shape = remember(morph, progress.value) {
        CubicPathShape(morph.asCubics(progress.value))
    }

    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}

class RoundedPolygonShape(private val polygon: RoundedPolygon) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val path = Path()
        val scale = min(size.width, size.height) / 2f

        val transformer = PointTransformer { x, y ->
            val tx = (x - polygon.centerX) * scale + size.width / 2f
            val ty = (y - polygon.centerY) * scale + size.height / 2f
            FloatFloatPair(tx, ty)
        }

        val transformed = polygon.transformed(transformer)
        val curves = transformed.cubics
        if (curves.isNotEmpty()) {
            path.moveTo(curves.first().anchor0X, curves.first().anchor0Y)
            for (c in curves) {
                path.cubicTo(
                    c.control0X, c.control0Y, c.control1X, c.control1Y, c.anchor1X, c.anchor1Y
                )
            }
            path.close()
        }

        return Outline.Generic(path)
    }
}

class CubicPathShape(private val cubicItems: List<Cubic>) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val scale = min(size.width, size.height) / 2f
        val path = Path()

        if (cubicItems.isNotEmpty()) {
            val first = cubicItems.first()
            path.moveTo(
                first.anchor0X * scale + size.width / 2f, first.anchor0Y * scale + size.height / 2f
            )
            for (c in cubicItems) {
                path.cubicTo(
                    c.control0X * scale + size.width / 2f,
                    c.control0Y * scale + size.height / 2f,
                    c.control1X * scale + size.width / 2f,
                    c.control1Y * scale + size.height / 2f,
                    c.anchor1X * scale + size.width / 2f,
                    c.anchor1Y * scale + size.height / 2f
                )
            }
            path.close()
        }

        return Outline.Generic(path)
    }
}