package com.augustbyrne.tas.ui.timer

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTimer(onNavigateTimerStart: () -> Unit, onNavBack: () -> Unit) {
    val context = LocalContext.current
    var timeValue by rememberSaveable { mutableStateOf(0) }
    val formattedTimerLength = String.format(
        "%01d:%02d:%02d",
        timeValue.div(3600),
        timeValue.div(60).mod(60),
        timeValue.mod(60)
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(text = "Quick Timer")
                },
                navigationIcon = {
                    IconButton(onClick = onNavBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = "Hour", style = MaterialTheme.typography.titleLarge)
                Text(text = "Minute", style = MaterialTheme.typography.titleLarge)
                Text(text = "Second", style = MaterialTheme.typography.titleLarge)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilledTonalButton(
                    onClick = {
                        if (timeValue + 3600 >= 35999) {
                            timeValue = 35999
                        } else {
                            timeValue += 3600
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "plus 1 hour")
                }
                FilledTonalButton(onClick = {
                    if (timeValue + 60 >= 35999) {
                        timeValue = 35999
                    } else {
                        timeValue += 60
                    }
                },
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "plus 1 minute")
                }
                FilledTonalButton(onClick = {
                    if (timeValue + 1 >= 35999) {
                        timeValue = 35999
                    } else {
                        timeValue += 1
                    }
                },
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "plus 1 second")
                }
            }
            Text(
                modifier = Modifier.wrapContentWidth(),
                style = MaterialTheme.typography.displayLarge,
                fontSize = 96.sp,
                text = formattedTimerLength
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilledTonalButton(onClick = {
                    if (timeValue - 3600 <= 0) {
                        timeValue = 0
                    } else {
                        timeValue -= 3600
                    }
                },
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Remove, contentDescription = "minus 1 hour")
                }
                FilledTonalButton(onClick = {
                    if (timeValue - 60 <= 0) {
                        timeValue = 0
                    } else {
                        timeValue -= 60
                    }
                },
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Remove, contentDescription = "minus 1 minute")
                }
                FilledTonalButton(onClick = {
                    if (timeValue - 1 <= 0) {
                        timeValue = 0
                    } else {
                        timeValue -= 1
                    }
                },
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Remove, contentDescription = "minus 1 second")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (timeValue != 0) {
                            TimerService.initTimerService(
                                NoteItem(0, null, null, 0, "Timer", ""),
                                listOf(
                                    DataItem(
                                        0,
                                        0,
                                        0,
                                        "",
                                        timeValue,
                                        0
                                    )
                                )
                            )
                            onNavigateTimerStart()
                            Intent(context, TimerService::class.java).also {
                                it.action = "ACTION_START_OR_RESUME_SERVICE"
                                context.startService(it)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                FilledTonalButton(
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    onClick = { timeValue = 0 }
                ) {
                    Text("Reset")
                }
            }
        }
    }
}
