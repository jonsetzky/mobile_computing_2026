package com.mobilecomputing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import com.mobilecomputing.db.AppDatabase
import com.mobilecomputing.db.Food
import com.mobilecomputing.db.FoodRepository
import com.mobilecomputing.db.FoodViewModel
import com.mobilecomputing.db.FoodViewModelFactory
import com.mobilecomputing.ui.theme.MobileComputingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
            val foodCount by foodViewModel.foodCount.collectAsState()
            val currentFoodIndex by foodViewModel.currentFoodIndex.collectAsState()

            if (food == null) {
                Text("waiting for food")
            } else {
                FoodPage(
                    food = food ?: throw NullPointerException("food is null."),
                    onAddFoodClick = { navController.navigate(route = AddFood) },
                    onNextFoodClick = if (foodCount <= currentFoodIndex + 1) null else fun() { foodViewModel.loadNextFood() },
                    onPrevFoodClick = if (currentFoodIndex == 0) null else fun() { foodViewModel.loadPreviousFood() }
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
    companion object {
        const val CHANNEL_ID = "noti";
        var NOTIFICATION_ID = 0;
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "foodtok_notification_channel",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "foodtok notification channel"
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    private fun sendNotification(message: String) {
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("My notification")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array&lt;out String&gt;,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 10)
                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("STATE", "exited app");
        CoroutineScope(SupervisorJob()).launch {
            delay(5000)

            // this if condition from AI
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                Log.i("STATE", "sending invisibility notification")
                sendNotification("You haven't used FoodTok for a while. Consider finding your next meal!")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val applicationScope = CoroutineScope(SupervisorJob());

        val notificationChannel = createNotificationChannel();

        Log.i("STATE", "creating database")
        val database = AppDatabase.getDatabase(this, applicationScope)
        val repository = FoodRepository(database.foodDao())

        enableEdgeToEdge()
        setContent {
            val foodViewModel: FoodViewModel = viewModel(factory = FoodViewModelFactory(repository))

            MobileComputingTheme {
                App(foodViewModel)
            }
        }
    }
}

