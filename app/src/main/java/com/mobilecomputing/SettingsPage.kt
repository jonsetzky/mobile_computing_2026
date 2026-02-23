package com.mobilecomputing

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun ToggleSetting(
    label: String,
    value: Boolean,
    setValue: (Boolean) -> Unit,
    enabled: Boolean,
    disabledMessage: String?
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(4f)
                .fillMaxWidth()
        ) {
            Text(style = MaterialTheme.typography.bodyMedium, text = label)
            if (!enabled && disabledMessage != null) Text(
                style = MaterialTheme.typography.bodySmall, text = disabledMessage
            )

        }
        Spacer(Modifier.weight(1f))
        Switch(value, onCheckedChange = setValue, enabled = enabled)
    }
}

@Composable
fun SettingsPage(navigateBack: () -> Unit) {
    val context = LocalContext.current

    val (cameraDenied, setCameraDenied) = remember { mutableStateOf(false) }
    val (hasCameraPermission, setHasCameraPermission) = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val (notificationDenied, setNotificationDenied) = remember { mutableStateOf(false) }
    val (hasNotificationPermission, setHasNotificationPermission) = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(Unit) {
        val isgr = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        );
        Log.i("PERM", "isgr: $isgr");

    }
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            PackageManager.PERMISSION_DENIED
            setHasNotificationPermission(isGranted)
            if (!isGranted) setNotificationDenied(true);
        }
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            setHasCameraPermission(isGranted)
            if (!isGranted) setCameraDenied(true);
        }


    Column() {
        Spacer(Modifier.size(getTopInset(LocalView.current, LocalDensity.current) + 24.dp))

        Button(
            onClick = {
                navigateBack();
            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Settings",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
        }

        Column(modifier = Modifier.padding(all = 12.dp)) {
            Text(
                style = MaterialTheme.typography.headlineLarge, text = "Settings"
            )
            ToggleSetting(
                "Allow notifications",
                hasNotificationPermission,
                { newVal ->
                    if (newVal) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.revokeSelfPermissionOnKill(android.Manifest.permission.POST_NOTIFICATIONS)
                        setNotificationDenied(true)
                        setHasNotificationPermission(false)
                    }
                },
                enabled = !notificationDenied,
                disabledMessage = "Notifications can be enabled in the system settings"
            )
            ToggleSetting(
                "Allow use of camera",
                hasCameraPermission,
                { newVal ->
                    if (newVal) {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    } else {
                        context.revokeSelfPermissionOnKill(android.Manifest.permission.CAMERA)
                        setCameraDenied(true)
                        setHasCameraPermission(false)
                    }
                },
                enabled = !cameraDenied,
                disabledMessage = "Please restart the app to change this or use the system settings."
            )
        }
    }
}