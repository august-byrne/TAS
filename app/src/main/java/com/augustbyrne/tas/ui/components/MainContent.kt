package com.augustbyrne.tas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.statusBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavDrawer(drawerState: DrawerState, onNavSettings: () -> Unit, onNavTimer: () -> Unit, content: @Composable () -> Unit) {
    NavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            Column(modifier = Modifier.statusBarsPadding().fillMaxSize()) {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
                    text = "TAS",
                    style = MaterialTheme.typography.titleLarge
                )
                ItemButton(
                    modifier = Modifier.padding(end = 8.dp),
                    icon = Icons.Rounded.Timer,
                    text = "Quick Timer",
                    onClick = onNavTimer
                )
                Divider(modifier = Modifier.padding(top = 8.dp))
                ItemButton(
                    modifier = Modifier.padding(end = 8.dp),
                    icon = Icons.Rounded.Settings,
                    text = "Settings",
                    onClick = onNavSettings
                )
            }
        },
        content = content
    )
}

@Composable
fun AutoSizingText(modifier: Modifier = Modifier, textStyle: TextStyle = LocalTextStyle.current, text: String) {
    var readyToDraw by remember { mutableStateOf(false) }
    var mutableTextStyle by remember { mutableStateOf(textStyle) }
    Text(
        text = text,
        maxLines = 1,
        softWrap = false,
        overflow = if (readyToDraw) TextOverflow.Ellipsis else TextOverflow.Visible,
        style = mutableTextStyle,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { textLayoutResult: TextLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                if (mutableTextStyle.fontSize > 16.sp) {
                    mutableTextStyle =
                        mutableTextStyle.copy(fontSize = mutableTextStyle.fontSize * 0.9)
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        },
        textAlign = TextAlign.Center,
        color = Color.Black
    )
}

@Composable
fun ItemButton(modifier: Modifier = Modifier, icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp,
                    bottomStart = 0.dp,
                    topEnd = 30.dp,
                    bottomEnd = 30.dp
                )
            )
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            )
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "item icon"
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            fontWeight = FontWeight.SemiBold,
            text = text
        )
    }
}
