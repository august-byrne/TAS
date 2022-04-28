package com.augustbyrne.tas.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomNavBar(navBackStackEntry: NavBackStackEntry?, navController: NavController, modifier: Modifier = Modifier) {
    val navIndex = when(navBackStackEntry?.destination?.id) {
        navController.findDestination("home")!!.id -> { 0 }
        navController.findDestination("general_timer")!!.id -> { 1 }
        navController.findDestination("settings")!!.id -> { 2 }
        else -> { null }
    }
    if (navIndex != null) {
        NavigationBar(
            modifier = modifier
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Rounded.PlaylistPlay, "Routines") },
                label = { Text("Routines") },
                selected = navIndex == 0,
                onClick = {
                    if (navBackStackEntry?.destination?.id != navController.findDestination("home")!!.id) {
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
                    if (navBackStackEntry?.destination?.id != navController.findDestination("general_timer")!!.id) {
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
                    if (navBackStackEntry?.destination?.id != navController.findDestination("settings")!!.id) {
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
