package com.example.protosuite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.protosuite.ui.notes.NoteViewModel
import com.example.protosuite.ui.timer.PreferenceManager
import com.example.protosuite.ui.values.blue200

@Composable
fun SettingsUI(myViewModel: NoteViewModel, onNavBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val showAdHiderPopup = rememberSaveable { mutableStateOf(false) }
    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxSize()
        ) {
            TopAppBar(
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
                            tint = Color.White
                        )
                    }
                }
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable {
                        showAdHiderPopup.value = true
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = "Remove Ads",
                style = MaterialTheme.typography.body1
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable {
                        uriHandler.openUri("https://github.com/august-byrne/ProtoSuite")
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = "Project Github",
                style = MaterialTheme.typography.body1
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .clickable {
                        myViewModel.isDarkTheme = !myViewModel.isDarkTheme
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dark Theme",
                    style = MaterialTheme.typography.body1
                )
                Switch(
                    checked = myViewModel.isDarkTheme,
                    onCheckedChange = {
                    myViewModel.isDarkTheme = it
                    }
                )
            }
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        if (showAdHiderPopup.value) {
            RemoveAdsPopupUI(
                closeAdsPopup = { showAdHiderPopup.value = false },
                setShowAdState = { newAdState ->
                    PreferenceManager(context).showAds = newAdState
                    myViewModel.adState = newAdState
                }
            )
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
            shape = MaterialTheme.shapes.medium.copy(CornerSize(16.dp)),
            modifier = Modifier
                .wrapContentHeight()
                .width(IntrinsicSize.Max),
            elevation = 24.dp,
            backgroundColor = Color.White
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
                        .background(blue200)
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h6,
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