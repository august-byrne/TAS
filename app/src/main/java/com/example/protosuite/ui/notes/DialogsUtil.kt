package com.example.protosuite.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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


@Composable
fun EditOneFieldDialog(headerName: String, fieldName: String, initialValue: String, inputType: KeyboardType = KeyboardType.Text, onDismissRequest: () -> Unit, onAccepted: (returnedValue: String) -> Unit) {
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
                    label = { Text(fieldName) },
                    value = fieldValue,
                    onValueChange = {
                        fieldValue = it
                    },
                    singleLine = true,
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