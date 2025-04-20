package com.example.cybersapienttask.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp)
        ) {
            // Animated illustration
            TaskIllustration(
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Tasks Yet",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start creating tasks to organize your day and boost your productivity!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddTask,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Create My First Task")
            }
        }
    }
}

@Composable
fun TaskIllustration(
    modifier: Modifier = Modifier
) {
    var animationStarted by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "illustration"
    )

    // Start animation when the component is first shown
    LaunchedEffect(Unit) {
        animationStarted = true
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)

        // Draw clipboard
        val clipboardWidth = width * 0.8f
        val clipboardHeight = height * 0.9f
        val clipboardLeft = (width - clipboardWidth) / 2
        val clipboardTop = (height - clipboardHeight) / 2

        // Clipboard background
        drawRoundRect(
            //  color = MaterialTheme.colorScheme.surfaceVariant,
            color = Color.Blue,

            topLeft = Offset(clipboardLeft, clipboardTop),
            size = Size(clipboardWidth, clipboardHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
        )

        // Clipboard clip
        drawRoundRect(
            //   color = MaterialTheme.colorScheme.primary,
            color = Color.Green,
            topLeft = Offset(center.x - width * 0.15f, clipboardTop - height * 0.05f),
            size = Size(width * 0.3f, height * 0.1f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
        )

        // Draw lines representing tasks (animated)
        val lineStartY = clipboardTop + clipboardHeight * 0.25f
        val lineSpacing = clipboardHeight * 0.15f
        val lineLength = clipboardWidth * 0.7f * animationProgress

        for (i in 0 until 4) {
            val lineY = lineStartY + i * lineSpacing
            drawLine(
                //   color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                color = Color.Magenta,

                start = Offset(clipboardLeft + clipboardWidth * 0.15f, lineY),
                end = Offset(clipboardLeft + clipboardWidth * 0.15f + lineLength, lineY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Draw checkmark if animation is complete
        if (animationProgress > 0.9f) {
            val checkPath = Path().apply {
                val checkSize = width * 0.15f
                val checkStartX = clipboardLeft + clipboardWidth * 0.75f
                val checkStartY = clipboardTop + clipboardHeight * 0.65f

                moveTo(checkStartX - checkSize, checkStartY)
                lineTo(checkStartX, checkStartY + checkSize)
                lineTo(checkStartX + checkSize, checkStartY - checkSize)
            }

            drawPath(
                path = checkPath,
                //     color = MaterialTheme.colorScheme.primary,
                color = Color.Magenta,

                style = Stroke(
                    width = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}