package com.augustbyrne.tas.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.augustbyrne.tas.data.db.entities.DataItem

/**
 * A reusable Dialog with a single text entry field that follows Material Design 3 specifications
 * @param headerName the string used for the header of the dialog box.
 * @param fieldName the optional label to be displayed inside the text field container.
 * @param initialValue the starting value inside the text field container.
 * @param maxChars the maximum number of character you can input in the text field.
 * @param singleLine the singleLine parameter of a TextField. When set to true, this text field becomes a single horizontally scrolling text field instead of wrapping onto multiple lines.
 * @param inputType the keyboard type to be used in the text field. This also transforms the visual representation of the input value (obscures passwords when KeyboardType.Password is chosen, for example).
 * @param onDismissRequest the action to take when a dismiss is requested (back press or cancel button is clicked).
 * @param onAccepted the action to take when the ok button is clicked. The value of the text field container is returned, to be acted upon.
 */
@Composable
fun EditOneFieldDialog(headerName: String, fieldName: String? = null, initialValue: String, maxChars: Int? = null, singleLine: Boolean = true, inputType: KeyboardType = KeyboardType.Text, onDismissRequest: () -> Unit, onAccepted: (returnedValue: String) -> Unit) {
    var fieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialValue,
                selection = TextRange(initialValue.length)
            )
        )
    }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        title = {
            Text(headerName)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester = focusRequester),
                    label = { if (fieldName != null) Text(fieldName) },
                    value = fieldValue,
                    onValueChange = {
                        if (maxChars == null) {
                            fieldValue = it
                        } else {
                            if (it.text.length <= maxChars) {
                                fieldValue = it
                            }
                        }
                    },
                    maxLines = 4,
                    singleLine = singleLine,
                    visualTransformation = if (inputType == KeyboardType.Password) PasswordVisualTransformation(
                        '●'
                    ) else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = inputType,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onAccepted(fieldValue.text)
                            focusManager.clearFocus()
                        }
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                if (maxChars != null) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        text = "${fieldValue.text.length}/$maxChars"
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAccepted(fieldValue.text) }
            ) {
                Text("Ok")
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditDataItemDialog(initialDataItem: DataItem, onDismissRequest: () -> Unit, onAccepted: (returnedValue: DataItem) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val newItem = remember { initialDataItem.id == 0 }
    var timeUnitValue by remember { mutableStateOf(initialDataItem.unit) }
    var activityFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialDataItem.activity,
                selection = TextRange(initialDataItem.activity.length)
            )
        )
    }
    var timeFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = if (initialDataItem.time == 0) "" else initialDataItem.time.toString(),
                selection = TextRange(initialDataItem.time.toString().length)
            )
        )
    }
    var timeError by rememberSaveable { mutableStateOf(false) }
    var activityError by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        title = {
            Text(
                text = "${
                    if (newItem) {
                        "Add"
                    } else {
                        "Edit"
                    }
                } Activity"
            )
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    label = { Text("Activity") },
                    modifier = Modifier
                        .weight(0.5f)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    singleLine = true,
                    value = activityFieldValue,
                    onValueChange = {
                        activityFieldValue = it
                    },
                    isError = activityError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    label = { Text("Time") },
                    modifier = Modifier
                        .weight(0.25f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    singleLine = true,
                    value = timeFieldValue,
                    onValueChange = {
                        timeFieldValue = it
                    },
                    isError = timeError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onAccepted(
                                initialDataItem.copy(
                                    activity = activityFieldValue.text,
                                    time = timeFieldValue.text.toInt(),
                                    unit = timeUnitValue
                                )
                            )
                            focusManager.clearFocus()
                        }
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        expanded = true
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text(
                        text =
                        when (timeUnitValue) {
                            0 -> "sec"
                            1 -> "min"
                            2 -> "hr"
                            else -> "unit"
                        }
                    )
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Rounded.ArrowDropUp
                        } else {
                            Icons.Rounded.ArrowDropDown
                        },
                        contentDescription = "time increment selector"
                    )
                    DropdownMenu(
                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            timeUnitValue = 0
                        }) {
                            Text(text = "seconds")
                        }
                        DropdownMenuItem(onClick = {
                            expanded = false
                            timeUnitValue = 1
                        }) {
                            Text(text = "minutes")
                        }
                        DropdownMenuItem(onClick = {
                            expanded = false
                            timeUnitValue = 2
                        }) {
                            Text(text = "hours")
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    activityError = activityFieldValue.text.isEmpty()
                    timeFieldValue.text.toIntOrNull().let {
                        timeError = it == null || it == 0
                    }
                    if (!activityError && !timeError) {
                        onAccepted(
                            initialDataItem.copy(
                                activity = activityFieldValue.text,
                                time = timeFieldValue.text.toInt(),
                                unit = timeUnitValue
                            )
                        )
                    }
                }
            ) {
                Text("Ok")
            }
        }
    )
}


@Composable
fun SortNotesByDialog(currentSortType: SortType, onValueSelected: (SortType?) -> Unit) {
    AlertDialog(
        onDismissRequest = {
        onValueSelected(null)
    },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        title = {
            Text("Sort By")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize(30.dp)))
                        .clickable(
                            onClick = { onValueSelected(SortType.Creation) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple()
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSortType == SortType.Creation,
                        onClick = {
                            onValueSelected(SortType.Creation)
                        }
                    )
                    Spacer(Modifier.width(16.dp))
                    Text("Creation Date")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize(30.dp)))
                        .clickable(
                            onClick = { onValueSelected(SortType.LastEdited) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple()
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSortType == SortType.LastEdited,
                        onClick = {
                            onValueSelected(SortType.LastEdited)
                        }
                    )
                    Spacer(Modifier.width(16.dp))
                    Text("Last Edited")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize(30.dp)))
                        .clickable(
                            onClick = { onValueSelected(SortType.Order) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple()
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSortType == SortType.Order,
                        onClick = {
                            onValueSelected(SortType.Order)
                        }
                    )
                    Spacer(Modifier.width(16.dp))
                    Text("Custom")
                }
            }
        },
        confirmButton = {}
    )
}