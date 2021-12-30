package com.augustbyrne.tas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Notes
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
import androidx.navigation.NavController
import com.augustbyrne.tas.ui.notes.NoteViewModel

/*
@ExperimentalPagerApi
@Composable
fun MainUI(myViewModel: NoteViewModel, onNavigate: (noteId: Int) -> Unit, onNavigateStart: () -> Unit) {
    val pages = remember { listOf("Notes", "Timer", "Calendar") }
    Column(Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(initialPage = 0)
        val composableScope = rememberCoroutineScope()
        TabRow(
            // Our selected tab is our current page
            selectedTabIndex = pagerState.currentPage,
            // Override the indicator, using the provided pagerTabIndicatorOffset modifier
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }
        ) {
            // Add tabs for all of our pages
            pages.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        composableScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            count = pages.size
        ) { page ->
            when (page) {
                0 -> {
                    NoteListUI(myViewModel,
                        { noteId: Int ->
                            onNavigate(noteId)
                        },
                        {
                            onNavigateStart()
                        },
                        {},{})
                }
                1 -> {
                    //TimerUI(myViewModel)
                }
                2 -> {
                    //CalendarUI()
                }
            }
        }
    }
}
*/

@Composable
fun NavBar(modifier: Modifier = Modifier, myViewModel: NoteViewModel, navController: NavController) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            icon = {
                if (myViewModel.selectedNavBarItem == 0) {
                    Icon(
                        Icons.Rounded.Notes,
                        contentDescription = "routines"
                    )
                } else {
                    Icon(
                        Icons.Outlined.Notes,
                        contentDescription = "routines"
                    )
                }
            },
            label = { Text("Routines") },
            selected = myViewModel.selectedNavBarItem == 0,
            onClick = {
                myViewModel.selectedNavBarItem = 0
                navController.navigate("home")
            })
        NavigationBarItem(
            icon = {
                if (myViewModel.selectedNavBarItem == 1) {
                    Icon(
                        Icons.Rounded.Timer,
                        contentDescription = "quick timer"
                    )
                } else {
                    Icon(
                        Icons.Outlined.Timer,
                        contentDescription = "quick timer"
                    )
                }
            },
            label = { Text("Timer") },
            selected = myViewModel.selectedNavBarItem == 1,
            onClick = {
                myViewModel.selectedNavBarItem = 1
                navController.navigate("general_timer")
            })
        NavigationBarItem(
            icon = {
                if (myViewModel.selectedNavBarItem == 2) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "settings"
                    )
                } else {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "settings"
                    )
                }
            },
            label = { Text("Settings") },
            selected = myViewModel.selectedNavBarItem == 2,
            onClick = {
                myViewModel.selectedNavBarItem = 2
                navController.navigate("settings")
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavDrawer(drawerState: DrawerState, onNavSettings: () -> Unit, onNavTimer: () -> Unit, content: @Composable () -> Unit) {
    NavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            Text(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
                text = "TAS",
                style = MaterialTheme.typography.titleLarge
            )
/*            Text(
                modifier = Modifier.padding(8.dp),
                text = "* Any Drag/Drop (for whole notes or activity items)"
            )*/
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

