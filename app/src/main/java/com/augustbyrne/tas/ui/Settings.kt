package com.augustbyrne.tas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.augustbyrne.tas.R
import com.augustbyrne.tas.ui.components.EditOneFieldDialog
import com.augustbyrne.tas.ui.components.RadioItemsDialog
import com.augustbyrne.tas.ui.notes.NoteViewModel
import com.augustbyrne.tas.ui.values.blue500
import com.augustbyrne.tas.util.DarkMode
import com.augustbyrne.tas.util.TimerTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUI(myViewModel: NoteViewModel, onNavBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val localCoroutineScope = rememberCoroutineScope()
    var showAdHiderPopup by rememberSaveable { mutableStateOf(false) }
    var showDarkModeDialog by rememberSaveable { mutableStateOf(false) }
    var showTimerThemeDialog by rememberSaveable { mutableStateOf(false) }
    val darkModeState by myViewModel.isDarkThemeFlow.observeAsState(initial = DarkMode.System)
    val timerThemeState by myViewModel.timerThemeFlow.observeAsState(initial = TimerTheme.Original)
    Scaffold(
        topBar = {
            SmallTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(text = "Settings")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                text = "Theme",
                style = MaterialTheme.typography.titleSmall,
                color = blue500
            )
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            showDarkModeDialog = true
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Dark mode",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = when (darkModeState) {
                        DarkMode.System -> "Follow system"
                        DarkMode.Off -> "Off"
                        DarkMode.On -> "On"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            showTimerThemeDialog = true
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Timer theme",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = when (timerThemeState) {
                        TimerTheme.Original -> "Original"
                        TimerTheme.Vibrant -> "Vibrant"
                        TimerTheme.VaporWave -> "Vaporwave"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Divider(modifier = Modifier.fillMaxWidth())
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                text = "Other",
                style = MaterialTheme.typography.titleSmall,
                color = blue500
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(
                        onClick = {
                            showAdHiderPopup = true
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    )
                    .padding(16.dp),
                text = "Remove ads",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(
                        onClick = {
                            uriHandler.openUri("https://github.com/august-byrne/ProtoSuite")
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    )
                    .padding(16.dp),
                text = "Project github",
                style = MaterialTheme.typography.bodyLarge
            )
            if (showAdHiderPopup) {
                EditOneFieldDialog(
                    headerName = "Speak friend & enter",
                    fieldName = "Password",
                    initialValue = "",
                    inputType = KeyboardType.Password,
                    onDismissRequest = { showAdHiderPopup = false },
                    onAccepted = { userInput ->
                        localCoroutineScope.launch {
                            myViewModel.setShowAds(
                                userInput != context.getString(R.string.no_ads_password)
                            )
                            showAdHiderPopup = false
                        }
                    }
                )
            }
            if (showTimerThemeDialog) {
                RadioItemsDialog(
                    title = "Timer theme",
                    radioItemNames = listOf("original", "vibrant", "vapor wave"),
                    currentState = timerThemeState.theme,
                    onClickItem = { indexClicked ->
                        localCoroutineScope.launch {
                            myViewModel.setTimerTheme(TimerTheme.getTheme(indexClicked))
                        }
                        showTimerThemeDialog = false
                    },
                    onDismissRequest = {
                        showTimerThemeDialog = false
                    }
                )
            }
            if (showDarkModeDialog) {
                RadioItemsDialog(
                    title = "Dark mode",
                    radioItemNames = listOf("Follow system", "Off", "On"),
                    currentState = darkModeState.mode,
                    onClickItem = {
                        showDarkModeDialog = false
                        localCoroutineScope.launch {
                            myViewModel.setIsDarkTheme(DarkMode.getMode(it))
                        }
                    },
                    onDismissRequest = { showDarkModeDialog = false }
                )
            }
        }
    }
}
