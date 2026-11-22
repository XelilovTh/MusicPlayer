package com.tehmaz.musicplayer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tehmaz.musicplayer.ui.theme.GlassBorderGradient
import com.tehmaz.musicplayer.ui.theme.GlassSurface

/**
 * Frosted glass container matching the reference styling:
 * Translucent dark espresso body, rounded corners, and subtle gold gradient stroke.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = GlassSurface,
    borderBrush: Brush = GlassBorderGradient,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(color = Color(0xFFF5B853).copy(alpha = 0.2f)),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color(0xFFF5B853).copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                border = BorderStroke(borderWidth, borderBrush),
                shape = shape
            )
            .then(clickableModifier),
        content = content
    )
}

/**
 * Circular frosted glass icon button.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    backgroundColor: Color = Color(0xFF231812).copy(alpha = 0.75f),
    borderColor: Color = Color(0x40F5B853),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = Color(0xFFF5B853)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}
