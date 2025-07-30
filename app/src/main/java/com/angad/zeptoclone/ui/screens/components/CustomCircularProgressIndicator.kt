package com.angad.zeptoclone.ui.screens.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CustomCircularProgressIndicator(
    modifier: Modifier = Modifier,
    outerColor: Color = Color(0xFFE91E63),
    innerColor: Color = Color(0xFFE91E63),
    outerStrokeWidth: Float = 6f,
    innerStrokeWidth: Float = 6f
) {
//    Create infinite transition for animation rotations
    val infiniteTransition = rememberInfiniteTransition(label = "progress_rotation")

//    Animate outer rotation from  0 to 360 degree(clockwise)
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

//    Animate inner rotation from 360 to 0 degree(anti-clockwise)
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    Box(modifier = Modifier.size(48.dp)) {
        Canvas(modifier = Modifier.size(48.dp)) {
            val center = size.width / 2

            //  Draw outer arc
            drawArc(
                color = outerColor,
                startAngle = outerRotation,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(
                    width = outerStrokeWidth,
                    cap = StrokeCap.Round
                )
            )

            //  Draw inner arc
            drawArc(
                color = outerColor,
                startAngle = innerRotation,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(
                    width = innerStrokeWidth,
                    cap = StrokeCap.Round
                ),
                size = size * 0.7f,
                topLeft = Offset(
                    center - (center * 0.7f),
                    center - (center * 0.7f)
                )
            )
        }
    }
}
