package com.mobilecomputing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mobilecomputing.ui.theme.MobileComputingTheme
import kotlinx.serialization.Serializable


@Serializable
object AddFood
@Serializable
data class Food(val name: String, val textBody: String)

@Composable
@Preview
fun App() {
    val context = LocalContext.current;
    val navController = rememberNavController();

    val loremIpsum = context.getString(R.string.lorem_ipsum)
    val food1 = Food("A cheesecake", loremIpsum)
    val food2 = Food("A woodhat", loremIpsum)


    NavHost(
        navController = navController,
        startDestination = food1
    ) {
        composable<Food> { backStackEntry ->
            val food: Food = backStackEntry.toRoute()
            FoodPage(
                food = food,
                onAddFoodClick = { navController.navigate(route = AddFood) }
            )
        }
        composable<AddFood> {
            AddFoodPage()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MobileComputingTheme {
                App()
            }
        }
    }
}

