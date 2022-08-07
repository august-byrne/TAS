package com.augustbyrne.tas.ui.timer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.data.db.entities.NoteWithItems
import com.augustbyrne.tas.ui.components.AutoSizingText
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuickTimer(onNavigateTimerStart: (noteWithData: NoteWithItems) -> Unit, onNavBack: () -> Unit) {
    var timeValue by rememberSaveable { mutableStateOf(0) }
    val formattedTimerLength = String.format(
        "%02d:%02d:%02d",
        timeValue.div(3600),
        timeValue.div(60).mod(60),
        timeValue.mod(60)
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(text = "Quick Timer")
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
/*                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            Pair(0F, MaterialTheme.colorScheme.secondaryContainer),
                            Pair(1F, MaterialTheme.colorScheme.tertiaryContainer)
                        )
                    )
                )*/,
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RepeatingButton(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    shape = RoundedCornerShape(topStart = 16.dp),
                    onClick = {
                        if (timeValue + 3600 >= 86399) {
                            timeValue = 86399
                        } else {
                            timeValue += 3600
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        Color.Transparent,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    //Icon(imageVector = Icons.Rounded.Add, contentDescription = "plus 1 hour")
                }
                VerticalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer)
                RepeatingButton(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        if (timeValue + 60 >= 86399) {
                            timeValue = 86399
                        } else {
                            timeValue += 60
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        Color.Transparent,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    //Icon(imageVector = Icons.Rounded.Add, contentDescription = "plus 1 minute")
                }
                VerticalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer)
                RepeatingButton(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    shape = RoundedCornerShape(topEnd = 16.dp),
                    onClick = {
                        if (timeValue + 1 >= 86399) {
                            timeValue = 86399
                        } else {
                            timeValue += 1
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        Color.Transparent,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    //Icon(imageVector = Icons.Rounded.Add, contentDescription = "plus 1 second")
                }
            }
            Divider(color = MaterialTheme.colorScheme.onSurface)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RepeatingButton(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {}
                        ),
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        if (timeValue - 3600 <= 0) {
                            timeValue = 0
                        } else {
                            timeValue -= 3600
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        Color.Transparent,//MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    //Icon(imageVector = Icons.Rounded.Remove, contentDescription = "minus 1 hour")
                }
                VerticalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer)
                RepeatingButton(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        if (timeValue - 60 <= 0) {
                            timeValue = 0
                        } else {
                            timeValue -= 60
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        Color.Transparent,//MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    //Icon(imageVector = Icons.Rounded.Remove, contentDescription = "minus 1 minute")
                }
                VerticalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer)
                RepeatingButton(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        if (timeValue - 1 <= 0) {
                            timeValue = 0
                        } else {
                            timeValue -= 1
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        Color.Transparent,//MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    //Icon(imageVector = Icons.Rounded.Remove, contentDescription = "minus 1 second")
                }
            }
        }
        Box(
            modifier = Modifier.padding(it).fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(36.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.weight(1f),
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "plus 1 hour"
                    )
                    Icon(
                        modifier = Modifier.weight(1f),
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "plus 1 minute"
                    )
                    Icon(
                        modifier = Modifier.weight(1f),
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "plus 1 second"
                    )
                }
                Spacer(Modifier.height(184.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.weight(1f),
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "minus 1 hour"
                    )
                    Icon(
                        modifier = Modifier.weight(1f),
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "minus 1 minute"
                    )
                    Icon(
                        modifier = Modifier.weight(1f),
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "minus 1 second"
                    )
                }
            }
        }
        Box(
            modifier = Modifier.padding(it).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(72.dp)) // minHeight 40 + padding (8*4)
                ElevatedCard(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .wrapContentSize(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    AutoSizingText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                        text = formattedTimerLength
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilledTonalButton(
                        colors = ButtonDefaults.buttonColors(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = { timeValue = 0 }
                    ) {
                        Text("Reset")
                    }
                    Button(
                        onClick = {
                            if (timeValue != 0) {
                                onNavigateTimerStart(
                                    NoteWithItems(
                                        NoteItem(title = "Timer"),
                                        listOf(DataItem(time = timeValue))
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = "Start",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    thickness: Dp = 1.dp
) {
    Box(
        modifier
            .fillMaxHeight()
            .width(thickness)
            .background(color = color)
    )
}

@Composable
fun RepeatingButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    maxDelayMillis: Long = 600,
    minDelayMillis: Long = 50,
    delayDecayFactor: Float = 0.7f,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier.repeatingClickable(
            interactionSource = interactionSource,
            enabled = enabled,
            maxDelayMillis = maxDelayMillis,
            minDelayMillis = minDelayMillis,
            delayDecayFactor = delayDecayFactor
        ) { onClick() },
        onClick = {},
        enabled = enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        shape = shape,
        border = border,
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

fun Modifier.repeatingClickable(
    interactionSource: InteractionSource,
    enabled: Boolean,
    maxDelayMillis: Long = 1000,
    minDelayMillis: Long = 5,
    delayDecayFactor: Float = 0.2f,
    onClick: () -> Unit
): Modifier = composed {

    val currentClickListener by rememberUpdatedState(onClick)

    pointerInput(interactionSource, enabled) {
        forEachGesture {
            coroutineScope {
                awaitPointerEventScope {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val heldButtonJob = launch {
                        var currentDelayMillis = maxDelayMillis
                        while (enabled && down.pressed) {
                            currentClickListener()
                            delay(currentDelayMillis)
                            val nextMillis = currentDelayMillis - (currentDelayMillis * delayDecayFactor)
                            currentDelayMillis = nextMillis.toLong().coerceAtLeast(minDelayMillis)
                        }
                    }
                    waitForUpOrCancellation()
                    heldButtonJob.cancel()
                }
            }
        }
    }
}