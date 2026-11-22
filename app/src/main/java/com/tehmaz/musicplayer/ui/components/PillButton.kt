package com.tehmaz.musicplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.GoldenAmberGradient
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary

/**
 * High-fidelity golden pill button with amber gradient and shadow glow.
 */
@Composable
fun GoldenPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = Color(0xFF140E0A),
    textColor: Color = Color(0xFF140E0A),
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 11.dp)
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                spotColor = AmberGoldPrimary.copy(alpha = 0.5f),
                ambientColor = AmberGoldPrimary.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(GoldenAmberGradient)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = Color.Black.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * Translucent glass pill button with gold border.
 */
@Composable
fun GlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFF261A13).copy(alpha = 0.65f))
            .border(
                BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0x66F5B853), Color(0x33F5B853))
                    )
                ),
                CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = AmberGoldPrimary),
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AmberGoldPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Selectable Category Pill used in Library and EQ presets.
 */
@Composable
fun CategoryPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) AmberGoldPrimary else Color(0xFF231812).copy(alpha = 0.7f),
        animationSpec = tween(durationMillis = 200),
        label = "pill_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF140E0A) else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "pill_text"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(
                BorderStroke(
                    1.dp,
                    if (isSelected) AmberGoldPrimary else Color(0x33F5B853)
                ),
                CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = AmberGoldPrimary.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Small pill badge, e.g. "✨ Discover Music".
 */
@Composable
fun DiscoverBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2E1C12).copy(alpha = 0.75f))
            .border(1.dp, Color(0x4DF5B853), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = AmberGoldPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        )
    }
}
