package com.example.protosuite.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.protosuite.ui.values.NotesTheme


@Composable
fun CalendarUI(){
    NotesTheme(darkTheme = false) {
        Column(
            modifier = Modifier
                .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {  }) {
                Text(text = "Button 1")
            }
            Spacer(Modifier.width(4.dp))
            Button(onClick = {  }) {
                Text(text = "Button 2")
            }
        }
    }
}


@Preview
@Composable
private fun CalendarUITest(){
    CalendarUI()
}