package com.augustbyrne.tas.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
fun MainBottomNavBar(
    navBackStackEntry: NavBackStackEntry?,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navIndex = when (navBackStackEntry?.destination?.route) {
        "home" -> {
            0
        }
        "general_timer" -> {
            1
        }
        "settings" -> {
            2
        }
        else -> {
            null
        }
    }
    if (navIndex != null) {
        NavigationBar(
            modifier = modifier
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, "Routines") },
                label = { Text("Routines") },
                selected = navIndex == 0,
                onClick = {
                    if (navBackStackEntry?.destination?.route != "home") {
                        navController.navigate("home") {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
            )
            NavigationBarItem(
                icon = { Icon(Icons.Rounded.Timer, "Quick Timer") },
                label = { Text("Quick Timer") },
                selected = navIndex == 1,
                onClick = {
                    if (navBackStackEntry?.destination?.route != "general_timer") {
                        navController.navigate("general_timer") {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
            )
            NavigationBarItem(
                icon = { Icon(Icons.Rounded.Settings, "Settings") },
                label = { Text("Settings") },
                selected = navIndex == 2,
                onClick = {
                    if (navBackStackEntry?.destination?.route != "settings") {
                        navController.navigate("settings") {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    }
}

@Composable
fun AutoSizingText(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    text: String
) {
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
                if (mutableTextStyle.fontSize > 18.sp) {
                    mutableTextStyle =
                        mutableTextStyle.copy(fontSize = mutableTextStyle.fontSize * 0.9)
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        },
        textAlign = TextAlign.Center
    )
}
