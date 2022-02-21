package com.augustbyrne.tas.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.augustbyrne.tas.R
import com.augustbyrne.tas.ui.timer.TimerService
import com.augustbyrne.tas.ui.timer.TimerText
import com.augustbyrne.tas.ui.values.*
import com.augustbyrne.tas.util.TimerState
import com.augustbyrne.tas.util.TimerTheme
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun ThemedBackground(timerTheme: TimerTheme) {
    val timerLengthMilli: Long by TimerService.timerLengthMilli.observeAsState(1L)
    val totalTimerLengthMilli: Long by TimerService.totalTimerLengthMilli.observeAsState(1L)
    val timerState: TimerState by TimerService.timerState.observeAsState(TimerState.Stopped)
    val progressInMilli: Long = 1000L - timerLengthMilli.times(1000L).div(totalTimerLengthMilli)
    Box(modifier = Modifier.fillMaxSize()) {
        when (timerTheme) {
            TimerTheme.Original -> {
                Box(
                    modifier = Modifier
                        .background(yellow50)
                        .fillMaxSize()
                )
            }
            TimerTheme.Vibrant -> {
                val infiniteTransition = rememberInfiniteTransition()
                val animationSpec: InfiniteRepeatableSpec<Color> = infiniteRepeatable(
                    animation = tween(3000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
                val colorFirst by infiniteTransition.animateColor(
                    initialValue = orange,
                    targetValue = peach,
                    animationSpec = animationSpec
                )
                val colorSecond by infiniteTransition.animateColor(
                    initialValue = peach,
                    targetValue = lightPurple,
                    animationSpec = animationSpec
                )
                val colorThird by infiniteTransition.animateColor(
                    initialValue = lightPurple,
                    targetValue = orange,
                    animationSpec = animationSpec
                )
                val animatedProgress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(20 * 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                val numberBubbles = 23
                val bubbleInfo = remember {
                    val bubbleInfos = mutableListOf<BubbleInfo>()
                    for (i in 0..numberBubbles) {
                        val offset = Offset(Math.random().toFloat(), Math.random().toFloat())
                        val offsetEnd = Offset(Math.random().toFloat(), Math.random().toFloat())
                        val radius = Math.random().toFloat() * 50
                        val radiusEnd = Math.random().toFloat() * 50

                        val bubblePoint = BubbleInfo(
                            offset,
                            offsetEnd,
                            Math.random().toFloat(),
                            radius,
                            radiusEnd
                        )
                        bubbleInfos.add(bubblePoint)
                    }
                    bubbleInfos
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val brushBackground = Brush.verticalGradient(
                        listOf(colorFirst, colorSecond, colorThird),
                        0f,
                        size.height,
                        TileMode.Mirror
                    )
                    drawRect(brushBackground)

                    for (bubble in bubbleInfo) {
                        val offsetAnimated = lerp(bubble.point, bubble.pointEnd, animatedProgress)
                        val radiusAnimated = lerp(bubble.radius, bubble.radiusEnd, animatedProgress)
                        // increase by a bigger scale to allow for bubbles to go off the screen
                        val sizeScaled = size * 1.4f
                        drawCircle(
                            bubbleColor,
                            radiusAnimated * density,
                            Offset(
                                offsetAnimated.x * sizeScaled.width,
                                offsetAnimated.y * sizeScaled.height
                            ),
                            alpha = bubble.alpha
                        )
                    }
                }
            }
            TimerTheme.VaporWave -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val brushTopBackground = Brush.verticalGradient(
                        listOf(pink200, pink100),
                        0f,
                        (size.height * (3f / 5f)),
                        TileMode.Decal
                    )
                    drawRect(
                        brush = brushTopBackground,
                        size = size.copy(height = size.height * 3f / 5f)
                    )
                    drawRect(
                        color = teal200,
                        size = size.copy(height = size.height * 3f / 5f),
                        topLeft = Offset(0f, size.height * 3f / 5f)
                    )
                    for (z in -10..16) {
                        drawLine(
                            color = blue500,
                            start = Offset(size.width * (z.toFloat() / 6f), size.height),
                            end = Offset(
                                size.width * ((0.4f * z.toFloat() / 10f) + 0.38f),
                                size.height * (3f / 5f)
                            ),
                            strokeWidth = 5f,
                            cap = StrokeCap.Butt
                        )
                    }
                    for (z in 0..17) {
                        drawLine(
                            color = blue500,
                            start = Offset(
                                0f,
                                size.height * (3f + 2f * (z.toFloat().div(17).pow(2))) / 5f
                            ),
                            end = Offset(
                                size.width,
                                size.height * (3f + 2f * (z.toFloat().div(17).pow(2))) / 5f
                            ),
                            strokeWidth = 5f,
                            cap = StrokeCap.Butt
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            //val centerY = remaining.height.toFloat() / 2f

                            // Locked to the bottom of the upper 3/5ths of the screen
                            // Makes sure the sun is always attached to the "horizon"
                            val yLocation = (space.height * (3f / 5f)) - (size.height * 0.98f)

                            val x = centerX * (1)
                            IntOffset(x.roundToInt(), yLocation.roundToInt())
                        }
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        painter = painterResource(id = R.drawable.vapor_wave_sun),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(
                            Color(
                                ColorUtils.blendARGB(
                                    yellowOrange.toArgb(),
                                    redOrange.toArgb(),
                                    progressInMilli.div(1000f)
                                )
                            )
                        ),
                        contentDescription = "sun"
                    )
                    if (timerState != TimerState.Delayed) {
                        TimerText(
                            modifier = Modifier.align(Alignment.Center),
                            timerState = timerState,
                            timerLengthMilli = timerLengthMilli,
                            totalTimerLengthMilli = totalTimerLengthMilli
                        )
                    } else {
                        Column(
                            modifier = Modifier.wrapContentSize().align(Alignment.Center),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(style = MaterialTheme.typography.displayMedium, text = "Starting in")
                            Text(
                                style = MaterialTheme.typography.displayMedium,
                                text = (timerLengthMilli.div(1000) + 1).coerceIn(0, 5).toString()
                            )
                        }
                    }
                }
                Image(
                    modifier = Modifier
                        .size(50.dp)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val y = (space.height * 0.625f) - size.height
                            val x = centerX * (1 + 0.35f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "left tree"
                )
                Image(
                    modifier = Modifier
                        .size(50.dp)
                        .graphicsLayer(rotationY = 180f)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val y = (space.height * 0.625f) - size.height
                            val x = centerX * (1 - 0.35f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "right tree"
                )
                Image(
                    modifier = Modifier
                        .size(100.dp)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val y = (space.height * 0.75f) - size.height
                            val x = centerX * (1 + 0.75f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "left tree"
                )
                Image(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer(rotationY = 180f)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val y = (space.height * 0.75f) - size.height
                            val x = centerX * (1 - 0.75f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "right tree"
                )
                Image(
                    modifier = Modifier
                        .size(160.dp)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val y = (space.height * 0.95f) - size.height
                            val x = centerX * (1 + 1.6f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "left tree"
                )
                Image(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer(rotationY = 180f)
                        .align { size, space, _ ->
                            val remaining =
                                IntSize(space.width - size.width, space.height - size.height)
                            val centerX = remaining.width.toFloat() / 2f
                            val y = (space.height * 0.95f) - size.height
                            val x = centerX * (1 - 1.6f)
                            IntOffset(x.roundToInt(), y.roundToInt())
                        },
                    painter = painterResource(id = R.drawable.vapor_wave_palm),
                    contentScale = ContentScale.Fit,
                    contentDescription = "right tree"
                )
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (timerState == TimerState.Delayed && timerTheme != TimerTheme.VaporWave) {
            Column(
                modifier = Modifier.wrapContentSize().align(Alignment.Center),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(style = MaterialTheme.typography.displayMedium, text = "Starting in")
                Text(
                    style = MaterialTheme.typography.displayMedium,
                    text = (timerLengthMilli.div(1000) + 1).coerceIn(0, 5).toString()
                )
            }
        }
    }
}


val orange = Color(0xFFF0A088)
val peach = Color(0xFFF38BAE)
val bubbleColor = Color(0xFFFFAB91)
val lightPurple = Color(0xFFD291DD)

data class BubbleInfo(
    val point: Offset,
    val pointEnd: Offset,
    val alpha: Float,
    val radius: Float,
    val radiusEnd: Float
)

fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}