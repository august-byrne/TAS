package com.augustbyrne.tas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.augustbyrne.tas.R
import com.augustbyrne.tas.ui.notes.EditOneFieldDialog
import com.augustbyrne.tas.ui.timer.PreferenceManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUI(onNavBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val localCoroutineScope = rememberCoroutineScope()
    var showAdHiderPopup by rememberSaveable { mutableStateOf(false) }
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
                    .clip(RoundedCornerShape(CornerSize(30.dp)))
                    .clickable(
                        onClick = {
                            showAdHiderPopup = true
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    )
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
                    .clip(RoundedCornerShape(CornerSize(30.dp)))
                    .clickable(
                        onClick = {
                            uriHandler.openUri("https://github.com/august-byrne/ProtoSuite")
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    )
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
                    .clip(RoundedCornerShape(CornerSize(30.dp)))
                    .clickable(
                        onClick = {
                            localCoroutineScope.launch {
                                PreferenceManager(context).setIsDarkTheme(!darkModeState)
                            }
                        },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    )
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
                    }
                )
            }
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            if (showAdHiderPopup) {
                EditOneFieldDialog(
                    headerName = "Speak Friend & Enter",
                    fieldName = "Password",
                    initialValue = "",
                    inputType = KeyboardType.Password,
                    onDismissRequest = { showAdHiderPopup = false },
                    onAccepted = { userInput ->
                        localCoroutineScope.launch {
                            PreferenceManager(context).setShowAds(
                                userInput != context.getString(R.string.no_ads_password)
                            )
                            showAdHiderPopup = false
                        }
                    }
                )
            }
        }
    }
}
