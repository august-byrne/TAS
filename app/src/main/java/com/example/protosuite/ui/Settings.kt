package com.example.protosuite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.protosuite.ui.notes.NoteViewModel
import com.example.protosuite.ui.timer.PreferenceManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUI(myViewModel: NoteViewModel, onNavBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val localCoroutineScope = rememberCoroutineScope()
    val showAdHiderPopup = rememberSaveable { mutableStateOf(false) }
    val darkModeState by PreferenceManager(context).isDarkThemeFlow.collectAsState(initial = false)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
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
                    .clickable {
                        showAdHiderPopup.value = true
                    }
                    .padding(16.dp),
                text = "Remove Ads",
                style = MaterialTheme.typography.bodyLarge
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable {
                        uriHandler.openUri("https://github.com/august-byrne/ProtoSuite")
                    }
                    .padding(16.dp),
                text = "Project Github",
                style = MaterialTheme.typography.bodyLarge
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .clickable {
                        localCoroutineScope.launch {
                            PreferenceManager(context).setIsDarkTheme(!darkModeState)
                        }
                        //myViewModel.isDarkTheme = !myViewModel.isDarkTheme
                    }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dark Theme",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = darkModeState,
                    onCheckedChange = {
                        localCoroutineScope.launch {
                            PreferenceManager(context).setIsDarkTheme(it)
                        }
                        //myViewModel.isDarkTheme = it
                    }
                )
            }
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            if (showAdHiderPopup.value) {
                RemoveAdsPopupUI(
                    closeAdsPopup = { showAdHiderPopup.value = false },
                    setShowAdState = { newAdState ->
                        localCoroutineScope.launch {
                            PreferenceManager(context).setShowAds(newAdState)
                        }
                        //PreferenceManager(context).showAds = newAdState
                        //myViewModel.adState = newAdState
                    }
                )
            }
        }
    }
}

@Composable
fun RemoveAdsPopupUI(closeAdsPopup: () -> Unit, setShowAdState: (Boolean) -> Unit) {
    val textBox = rememberSaveable { mutableStateOf("") }
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = {
            closeAdsPopup()
        },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = androidx.compose.material.MaterialTheme.shapes.medium.copy(CornerSize(16.dp)),
            modifier = Modifier
                .wrapContentHeight()
                .width(IntrinsicSize.Max),
            elevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    text = "Speak friend and enter"
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                    value = textBox.value,
                    onValueChange = {textBox.value = it}
                )
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        if (textBox.value == "mellon") {
                            setShowAdState(false)
                        } else {
                            setShowAdState(true)
                        }
                        closeAdsPopup()
                    }) {
                        Text("Enter")
                    }
                }
            }
        }
    }
}