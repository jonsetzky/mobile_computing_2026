package com.mobilecomputing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
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
    val food3 = Food("A short hat", "Short description")

    var foods = arrayOf(food1, food2, food3);

    NavHost(
        navController = navController,
        startDestination = food1,
    ) {
        composable<Food>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) { backStackEntry ->
            val food: Food = backStackEntry.toRoute()
            val nextFoodIndex = foods.indexOf(food) + 1
            val nextFood = if (nextFoodIndex == foods.size) null else foods[foods.indexOf(food) + 1]

            val onNextFoodClick: (() -> Unit)? =
                if (nextFood == null) null else fun() { navController.navigate(route = nextFood) }
            val onPrevFoodClick: (() -> Unit)? =
                if (food == foods.first())  null else fun() { navController.navigateUp() }
            FoodPage(
                food = food,
                onAddFoodClick = { navController.navigate(route = AddFood) },
                onNextFoodClick = onNextFoodClick,
                onPrevFoodClick = onPrevFoodClick
            )
        }
        composable<AddFood> {
            AddFoodPage(onAddFood = {food: Food ->
                foods += food
                navController.navigateUp()
            })
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

