package com.mobilecomputing

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.toRoute
import com.mobilecomputing.db.AppDatabase
import com.mobilecomputing.db.Food
import com.mobilecomputing.db.FoodComment
import com.mobilecomputing.db.FoodRepository
import com.mobilecomputing.db.FoodViewModel
import com.mobilecomputing.db.FoodViewModelFactory
import com.mobilecomputing.ui.theme.MobileComputingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


@Serializable
data class AddFood(val openCamera: Boolean = false)

@Serializable
object FoodView

@Serializable
object SettingsView

@Composable
fun App(foodViewModel: FoodViewModel, events: Flow<MainEvent>) {
    val navController = rememberNavController();

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is MainEvent.OpenAddFoodCamera -> navController.navigate(AddFood(true))
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = FoodView,
    ) {
        composable<FoodView>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }) {
            val food by foodViewModel.currentFood.collectAsState()
            val hasNextFood by foodViewModel.hasNextFood.collectAsState();
            val hasPrevFood by foodViewModel.hasPrevFood.collectAsState();

            if (food == null) {
                Text("waiting for food")
            } else {
                FoodPage(
                    food = food ?: throw NullPointerException("food is null."),
                    onAddFoodClick = { navController.navigate(route = AddFood(false)) },
                    onNextFoodClick = if (!hasNextFood) null else fun() { foodViewModel.loadNextFood() },
                    onPrevFoodClick = if (!hasPrevFood) null else fun() { foodViewModel.loadPreviousFood() },
                    onAddComment = { foodId: Int, newComment: String ->
                        foodViewModel.insert(FoodComment(foodId = foodId, content = newComment));
                    },
                    onSettingsClick = {
                        Log.i("NAV", "to settings");
                        navController.navigate(route = SettingsView)
                    })
            }
        }
        composable<AddFood>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }) { backStackEntry ->
            val openCamera = backStackEntry.toRoute<AddFood>().openCamera
            AddFoodPage(onAddFood = { food: Food ->
                foodViewModel.insert(food)
                navController.navigateUp()
            }, openCamera = openCamera)
        }
        composable<SettingsView>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }) {
            SettingsPage(navigateBack = {
                navController.navigateUp()
            })
        }
    }
}

fun magnitude(vec: FloatArray): Float {
    return sqrt(vec[0].pow(2) + vec[1].pow(2) + vec[2].pow(2))
}

class MainActivity : ComponentActivity() {
    companion object {
        const val CHANNEL_ID = "noti";
        var NOTIFICATION_ID = 0;
    }

    private val events = MutableSharedFlow<MainEvent>()

    private class SensorListener(val context: MainActivity) : SensorEventListener {
        companion object {
            const val HISTORY_SIZE = 20
            const val HISTORY_ENTRY_LEN = 5
            const val SHAKE_COOLDOWN = 50 // SHAKE_COOLDOWN/samplespersecond = 50/20 = 2.5 seconds
        }

        var index: Int = 0

        // [x, y, z, mag, sharp?]
        val history: Array<FloatArray> = Array(HISTORY_SIZE) { FloatArray(HISTORY_ENTRY_LEN) }
        var shakeCooldown: Int = 0

        override fun onSensorChanged(event: SensorEvent?) {
            // this is called ~20 times a second for accelerometer
            if (event == null) {
                return
            }

            val values = event.values;

            val magnitude = magnitude(values)
            var sharp = 0f
//            Log.i("ACCELEROMETER", "${magnitude}: ${values[0]},${values[1]},${values[2]}")

            // check for shake every 5th update => 4 times a second
            if (index % 5 == 0 && shakeCooldown <= 0) {
                val sum = FloatArray(HISTORY_ENTRY_LEN)
                for (e in history) {
                    for (i in 0..<HISTORY_ENTRY_LEN) {
                        sum[i] += e[i]
                    }
                }
                val sumMag = magnitude(floatArrayOf(sum[0], sum[1], sum[2]))
                val magSum = sum[3]
//                Log.i("ACCELEROMETER", "${sum[0]},${sum[1]},${sum[2]},${sum[3]},${sum[4]}: sumMag ${sumMag}, magSum ${magSum}")
                if (magSum > sumMag * 10) {
//                    Log.i("ACCELEROMETER", "sharp!")
                    sharp = 1f
                }

                // a total of 3 sharps indicates a shake
                if (sum[4] >= 3) {
//                    this@SensorListener.context.showAlert(
//                        "TODO: Open camera",
//                        "Shaking the device would open the camera for adding a new food."
//                    )
                    this@SensorListener.context.navToAddFoodWithCamera()
                    Log.i("ACCELEROMETER", "shake!")
                    shakeCooldown = SHAKE_COOLDOWN
                    index = 0
                    return;
                }
            }

            history[index] = values + floatArrayOf(magnitude, sharp)
            index = (index + 1) % HISTORY_SIZE
            shakeCooldown = max(0, shakeCooldown - 1)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            //TODO("Not yet implemented")
        }

    }

    public fun showAlert(title: String, message: String) {
        runOnUiThread {
            AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
    }

    private fun navToAddFoodWithCamera() {
        lifecycleScope.launch {
            events.emit(MainEvent.OpenAddFoodCamera())
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "foodtok_notification_channel", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "foodtok notification channel"
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    private fun sendNotification(message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder =
            NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("My notification")
                .setContentText(message).setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setContentIntent(pendingIntent)
                .setAutoCancel(true);

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("NOTIFICATION", "Cannot send notification due to missing permissions")
            }
            // notificationId is a unique int for each notification that you must define.
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private var backgroundJob: Job? = null

    override fun onStop() {
        super.onStop()
        Log.d("STATE", "exited app");
        backgroundJob = lifecycleScope.launch {
            delay(5000)

            // this if condition from AI
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                Log.i("STATE", "trying to send invisibility notification")
                sendNotification("You haven't used FoodTok for a while. Consider finding your next meal!")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        backgroundJob?.cancel();
    }

    override fun onResume() {
        super.onResume()
        sensorListener = SensorListener(this)
        mAccel?.also { accel ->
            sensorManager.registerListener(sensorListener, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
        sensorListener = null
    }

    private lateinit var sensorManager: SensorManager
    private var mAccel: Sensor? = null
    private var sensorListener: SensorListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val applicationScope = CoroutineScope(SupervisorJob());

        val notificationChannel = createNotificationChannel();

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)


        Log.i("STATE", "creating database")
        val database = AppDatabase.getDatabase(this, applicationScope)
        val repository = FoodRepository(database.foodDao())

        enableEdgeToEdge()
        setContent {
            val foodViewModel: FoodViewModel = viewModel(factory = FoodViewModelFactory(repository))

            MobileComputingTheme {
                App(foodViewModel, events)
            }
        }

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array&lt;out String&gt;,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 10
                )
                // todo the following doesn't work:
//                ActivityCompat.OnRequestPermissionsResultCallback { requestCode, permissions, grantResults ->
//                    if (requestCode != 10) return@OnRequestPermissionsResultCallback;
//                    if (grantResults.contains(PackageManager.PERMISSION_DENIED)) {
//                        showAlert(
//                            "Warning",
//                            "Allowing notifications helps you stay up to date with latest updates!"
//                        )
//                    }
//                }

                //return@with
            }

        }
    }
}

sealed class MainEvent {
    class OpenAddFoodCamera() : MainEvent()
}