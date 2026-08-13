package com.ctom.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ctom.player.ui.theme.IceBlue
import com.ctom.player.ui.theme.OceanSurface
import com.ctom.player.ui.theme.StrokeBlue
import com.ctom.player.ui.theme.TextPrimary

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    val clickable = if (onClick != null) {
        Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { role = Role.Button }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(OceanSurface.copy(alpha = 0.92f), Color(0xFF0B1C2A).copy(alpha = 0.72f)),
                ),
            )
            .border(1.dp, Brush.linearGradient(listOf(StrokeBlue, IceBlue.copy(alpha = 0.12f))), shape)
            .then(clickable),
        content = { content() },
    )
}

@Composable
fun LiquidButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    LiquidGlassCard(
        modifier = modifier
            .defaultMinSize(minHeight = 46.dp)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
            color = IceBlue,
            fontSize = 11.sp,
            letterSpacing = 1.3.sp,
        )
    }
}

@Composable
fun LiquidIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color = IceBlue,
) {
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = description, tint = tint)
    }
}

@Composable
fun LiquidBottomBar(content: @Composable RowScope.() -> Unit) {
    LiquidGlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}