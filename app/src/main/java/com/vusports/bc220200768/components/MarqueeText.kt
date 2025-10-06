package com.vusports.bc220200768.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    color: Color = Color.White,
    backgroundColor: Color = Color(0xFF00BFA6),
    animationDuration: Int = 6000, // faster
    gapPx: Int = 50 // extra spacing so the text fully clears the box
) {
    var textWidth by remember { mutableStateOf(0) }
    var boxWidth by remember { mutableStateOf(0) }
    val anim = remember { Animatable(0f) }

    LaunchedEffect(textWidth, boxWidth, animationDuration) {
        if (textWidth == 0 || boxWidth == 0) return@LaunchedEffect
        while (true) {
            // start off-screen at right
            anim.snapTo(boxWidth.toFloat())
            // slide to left until fully out of view (including gap)
            anim.animateTo(
                targetValue = -(textWidth + gapPx).toFloat(),
                animationSpec = tween(durationMillis = animationDuration, easing = LinearEasing)
            )
            // loop restarts -> text will re-enter from right
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(backgroundColor)
            .clipToBounds()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = color,
            style = textStyle,
            softWrap = false,
            modifier = Modifier
                .onGloballyPositioned { layout ->
                    textWidth = layout.size.width
                    boxWidth = layout.parentCoordinates?.size?.width ?: 0
                }
                .offset { IntOffset(anim.value.roundToInt(), 0) }
        )
    }
}
