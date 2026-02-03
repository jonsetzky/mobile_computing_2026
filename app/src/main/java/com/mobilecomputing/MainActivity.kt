package com.mobilecomputing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mobilecomputing.ui.theme.MobileComputingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileComputingTheme {
                FoodPage(
                    "A cheesecake",
                    getString(R.string.lorem_ipsum)
                )
            }
        }
    }
}

