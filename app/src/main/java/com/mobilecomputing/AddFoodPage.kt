package com.mobilecomputing

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun getTopInset(view: View, density: Density): Dp {
    // Get the top inset (status bar + notch)
    return with(density) {
        ViewCompat.getRootWindowInsets(view)
            ?.getInsets(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout())
            ?.top?.toDp() ?: 0.dp
    }
}

@Composable
fun AddFoodPage(onAddFood: (Food) -> Unit) {

    val (name, setName) = remember { mutableStateOf("") }
    val (description, setDescription) = remember { mutableStateOf("") }

    Column() {
        Spacer(Modifier.size(getTopInset(LocalView.current, LocalDensity.current)+24.dp))

        Text(
            style = MaterialTheme.typography.titleLarge,
            fontSize = 8.em,
            text = "Create a new food"
        )
        Text("Name")
        TextField(value = name, onValueChange = { v -> setName(v) }, singleLine = true)
        Text("Description")
        TextField(value = description, onValueChange = { v -> setDescription(v) }, singleLine = false)
        Button(onClick = {onAddFood(Food(name, description))}) {
            Text("Create")
        }
    }
}