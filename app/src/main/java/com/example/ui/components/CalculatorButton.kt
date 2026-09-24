package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ButtonType {
    NUMBER,
    OPERATOR,
    ACTION,
    EQUALS,
    SCIENTIFIC
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.NUMBER,
    testTag: String = "btn_$text",
    height: Dp = 64.dp,
    fontSize: TextUnit = 24.sp,
    isSelected: Boolean = false,
    content: (@Composable () -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "btnScale"
    )

    val (bgColor, contentColor) = when (type) {
        ButtonType.NUMBER -> {
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }
        ButtonType.OPERATOR -> {
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }
        ButtonType.ACTION -> {
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.error
        }
        ButtonType.EQUALS -> {
            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        }
        ButtonType.SCIENTIFIC -> {
            if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) to MaterialTheme.colorScheme.onSurface
            }
        }
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .height(height)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = text,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = if (type == ButtonType.EQUALS || type == ButtonType.OPERATOR) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
