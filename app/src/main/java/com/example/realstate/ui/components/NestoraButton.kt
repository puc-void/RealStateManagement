package com.example.realstate.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NestoraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
    textColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "btn_scale")

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = colors.first().copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) { onClick() }
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 0.5.sp
        )
    }
}
