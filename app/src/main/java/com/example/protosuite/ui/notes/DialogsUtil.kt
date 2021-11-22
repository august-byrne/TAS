package com.example.protosuite.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.protosuite.data.db.entities.DataItem

/**
 * A reusable Dialog with a single text entry field that follows Material Design 3 specifications
 * @param headerName the string used for the header of the dialog box.
 * @param fieldName the optional label to be displayed inside the text field container.
 * @param initialValue the starting value inside the text field container.
 * @param singleLine the singleLine parameter of a TextField. When set to true, this text field becomes a single horizontally scrolling text field instead of wrapping onto multiple lines.
 * @param inputType the keyboard type to be used in the text field. This also transforms the visual representation of the input value (obscures passwords when KeyboardType.Password is chosen, for example).
 * @param onDismissRequest the action to take when a dismiss is requested (back press or cancel button is clicked).
 * @param onAccepted the action to take when the ok button is clicked. The value of the text field container is returned, to be acted upon.
 */
@Composable
fun EditOneFieldDialog(headerName: String, fieldName: String? = null, initialValue: String, singleLine: Boolean = true, inputType: KeyboardType = KeyboardType.Text, onDismissRequest: () -> Unit, onAccepted: (returnedValue: String) -> Unit) {
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
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .wrapContentHeight(),
            //.requiredWidthIn(min = 280.dp, max = 560.dp),
            shape = androidx.compose.material.MaterialTheme.shapes.medium.copy(
                CornerSize(28.dp)
            ),
            backgroundColor = MaterialTheme.colorScheme.surface,
            //elevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = headerName,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester = focusRequester),
                    label = { if (fieldName != null) Text(fieldName) },
                    value = fieldValue,
                    onValueChange = {
                        fieldValue = it
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
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onAccepted(fieldValue.text) }
                    ) {
                        Text(
                            text = "Ok",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditDataItemDialog(initialDataItem: DataItem, onDismissRequest: () -> Unit, onAccepted: (returnedValue: DataItem) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
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
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .wrapContentHeight(),
            shape = androidx.compose.material.MaterialTheme.shapes.medium.copy(
                CornerSize(28.dp)
            ),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Edit Activity",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        label = { Text("Activity") },
                        modifier = Modifier
                            .weight(weight = 0.5F)
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                        singleLine = true,
                        value = activityFieldValue,
                        onValueChange = {
                            activityFieldValue = it
                        },
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
                            .weight(weight = 0.25F),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                        singleLine = true,
                        value = timeFieldValue,
                        onValueChange = {
                            timeFieldValue = it
                        },
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
                    Box(
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    focusManager.clearFocus()
                                    expanded = true
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier.wrapContentSize()
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
                        }
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
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onAccepted(
                                initialDataItem.copy(
                                    activity = activityFieldValue.text,
                                    time = timeFieldValue.text.toInt(),
                                    unit = timeUnitValue
                                )
                            )
                        }
                    ) {
                        Text(
                            text = "Ok",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SortPopupUI(currentSortType: SortType, onValueSelected: (SortType?) -> Unit) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = {
            onValueSelected(null)
        },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = androidx.compose.material.MaterialTheme.shapes.medium.copy(CornerSize(28.dp)),
            modifier = Modifier
                .wrapContentHeight(),
            elevation = 24.dp,
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .width(280.dp)
                        .padding(top = 24.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentHeight(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = "Sort By"
                    )
                }
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(top = 8.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .clickable {
                                    onValueSelected(SortType.Creation)
                                },
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Creation date")
                            RadioButton(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                selected = currentSortType == SortType.Creation,
                                onClick = {
                                    onValueSelected(SortType.Creation)
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .clickable {
                                    onValueSelected(SortType.LastEdited)
                                },
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Last edited")
                            RadioButton(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                selected = currentSortType == SortType.LastEdited,
                                onClick = {
                                    onValueSelected(SortType.LastEdited)
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .clickable {
                                    onValueSelected(SortType.Order)
                                },
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Custom")
                            RadioButton(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                selected = currentSortType == SortType.Order,
                                onClick = {
                                    onValueSelected(SortType.Order)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}