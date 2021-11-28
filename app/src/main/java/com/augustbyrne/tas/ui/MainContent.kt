package com.augustbyrne.tas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Sort
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
import com.augustbyrne.tas.ui.notes.NoteListUI
import com.augustbyrne.tas.ui.notes.NoteViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

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

@ExperimentalMaterial3Api
@Composable
fun MainAppBar(modifier: Modifier = Modifier, myViewModel: NoteViewModel, scrollBehavior: TopAppBarScrollBehavior, onDrawerOpen: () -> Unit, onNavSettings: () -> Unit) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        title = {
            Text("Timed Activity System")
        },
        navigationIcon = {
            IconButton(
                onClick = onDrawerOpen
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            IconButton(onClick = {
                myViewModel.openSortPopup = true
            }) {
                Icon(
                    imageVector = Icons.Rounded.Sort,
                    contentDescription = "Sort"
                )
            }
/*            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Menu"
                )
            }
            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                expanded = expanded,
                onDismissRequest = { expanded = false },
                content = {
                    DropdownMenuItem(onClick = {
                        *//* Handle settings! *//*
                        expanded = false
                        onNavSettings()
                    }) {
                        Text("Settings")
                    }
                    Divider()
                    DropdownMenuItem(onClick = {
                        *//* Handle donate or send feedback! *//*
                        expanded = false
                    }) {
                        Text("Donate")
                    }
                }
            )*/
        }
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
            .clip(RoundedCornerShape(CornerSize(30.dp)))
            .clickable(
                onClick = { onClick() },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            )
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "item icon",
            //tint = MaterialTheme.colorScheme.onBackground
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            fontWeight = FontWeight.SemiBold,
            text = text,
            //color = MaterialTheme.colorScheme.onBackground
        )
    }
}

