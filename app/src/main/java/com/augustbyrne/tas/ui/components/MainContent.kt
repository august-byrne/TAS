package com.augustbyrne.tas.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavDrawer(drawerState: DrawerState, onNavSettings: () -> Unit, onNavTimer: () -> Unit, content: @Composable () -> Unit) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "TAS",
                    style = MaterialTheme.typography.titleLarge
                )
                NavigationDrawerItem(
                    label = { Text(text = "Quick Timer", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Rounded.Timer, "Icon") },
                    selected = false,
                    onClick = onNavTimer
                )
                Divider(modifier = Modifier.padding(top = 8.dp))
                NavigationDrawerItem(
                    label = { Text(text = "Settings", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Rounded.Settings, "Icon") },
                    selected = false,
                    onClick = onNavSettings
                )
            }
        },
        content = content
    )
}
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomNavBar(navBackStackEntry: NavBackStackEntry?, navController: NavController, scrollBehavior: TopAppBarScrollBehavior) {
    val navIndex = when(navBackStackEntry?.destination?.id) {
        navController.findDestination("home")!!.id -> { 0 }
        navController.findDestination("general_timer")!!.id -> { 1 }
        navController.findDestination("settings")!!.id -> { 2 }
        else -> { -1 }
    }
    if (navIndex != -1) {
        val offsetLimit = with(LocalDensity.current) { -80.dp.toPx() }
        SideEffect {
            if (scrollBehavior.offsetLimit != offsetLimit) {
                scrollBehavior.offsetLimit = offsetLimit
            }
        }
        val height = LocalDensity.current.run {
            80.dp.toPx() + (1.25 * scrollBehavior.offset)
        }

        NavigationBar(
            modifier = Modifier.heightIn(0.dp, height.dp)
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Rounded.PlaylistPlay, "Routines") },
                label = { Text("Routines") },
                selected = navIndex == 0,
                onClick = {
                    if (navBackStackEntry?.destination?.id != navController.findDestination("home")!!.id) {
                        navController.navigate("home")
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
                        navController.navigate("general_timer")
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
                        navController.navigate("settings")
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
        textAlign = TextAlign.Center
    )
}
