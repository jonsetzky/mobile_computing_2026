package com.mobilecomputing

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.*
import com.mobilecomputing.db.AppDatabase
import com.mobilecomputing.db.Food
import com.mobilecomputing.db.FoodRepository
import com.mobilecomputing.db.FoodViewModel
import com.mobilecomputing.db.FoodViewModelFactory
import com.mobilecomputing.ui.theme.MobileComputingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.Serializable


@Serializable
object AddFood

@Serializable
object FoodView

@Composable
fun App(foodViewModel: FoodViewModel) {
    val navController = rememberNavController();


    NavHost(
        navController = navController,
        startDestination = FoodView,
    ) {
        composable<FoodView>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }) {
            val food by foodViewModel.currentFood.collectAsState()
            val isLast by foodViewModel.isLast.collectAsState()
            val isFirst by foodViewModel.isFirst.collectAsState()
//            val onNextFoodClick: (() -> Unit)? =
//                if (nextFood == null) null else fun() { navController.navigate(route = nextFood) }
//            val onPrevFoodClick: (() -> Unit)? =
//                if (prevFood == null) null else fun() { navController.navigate(route = prevFood) }

            if (food == null) {
                Text("waiting for food")
            } else {
                FoodPage(
                    food = food ?: throw NullPointerException("food is null."),
                    onAddFoodClick = { navController.navigate(route = AddFood) },
                    onNextFoodClick = if (isLast) null else fun() { foodViewModel.loadNextFood() },
                    onPrevFoodClick =if (isFirst) null else fun() { foodViewModel.loadPreviousFood() }
                )
            }
        }
        composable<AddFood> {
            AddFoodPage(onAddFood = { food: Food ->
                foodViewModel.insert(food)
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

            val applicationScope = CoroutineScope(SupervisorJob());

            Log.i("STATE", "creating database")
            val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
            val repository by lazy { FoodRepository(database.foodDao()) }

            val foodViewModel: FoodViewModel = viewModel(factory = FoodViewModelFactory(repository))

            MobileComputingTheme {
                App(foodViewModel)
            }
        }
    }
}

