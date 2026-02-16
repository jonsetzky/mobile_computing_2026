package com.mobilecomputing

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil3.compose.AsyncImage
import coil3.toUri
import com.mobilecomputing.db.Food
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.jar.Manifest
import kotlin.io.path.toPath

fun genImageUri(context: Context): URI {
    val uid = UUID.randomUUID().toString()
    val path = FileSystems.getDefault().getPath(context.filesDir.toString(), uid);
    return path.toUri()
}

fun saveImageFromUri(context: Context, uri: Uri): URI {
    val file = File(genImageUri(context))
    val resolver = context.contentResolver
    resolver.openInputStream(uri).use { stream ->
        stream?.copyTo(file.outputStream())
    }
    return file.toURI()
}


fun getTopInset(view: View, density: Density): Dp {
    // Get the top inset (status bar + notch)
    return with(density) {
        ViewCompat.getRootWindowInsets(view)
            ?.getInsets(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout())?.top?.toDp()
            ?: 0.dp
    }
}

@Composable
fun AddFoodPage(onAddFood: (Food) -> Unit) {
    val context = LocalContext.current
    val (name, setName) = remember { mutableStateOf("") }
    val (description, setDescription) = remember { mutableStateOf("") }
    val (imageUri, setImageUri) = remember { mutableStateOf("") }
    val (savedImageUri, setSavedImageUri) = remember { mutableStateOf("") }

    val (cameraImageUri, setCameraImageUri) = remember { mutableStateOf("") }
    val (hasCameraPermission, setHasCameraPermission) = remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        setHasCameraPermission(isGranted)
    }
    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            if (cameraImageUri.isEmpty()) {
                Log.e("PhotoPicker", "Cannot save image because cameraImageUri is null!")
                return@rememberLauncherForActivityResult
            }
            Log.i("CAMERA", "Successfully took a picture!")

            Log.d("PhotoPicker", "Taken image URI: $cameraImageUri")
            setImageUri(cameraImageUri)
            val saved = saveImageFromUri(context, cameraImageUri.toUri())
            setSavedImageUri(saved.toString())
            Log.d("PhotoPicker", "Saved taken image to URI: $saved")
        }
    }

    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                setImageUri(uri.toString())
                val saved = saveImageFromUri(context, uri)
                setSavedImageUri(saved.toString())
                Log.d("PhotoPicker", "Saved URI: $saved")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    Column() {
        Spacer(Modifier.size(getTopInset(LocalView.current, LocalDensity.current) + 24.dp))

        Text(
            style = MaterialTheme.typography.titleLarge, fontSize = 8.em, text = "Create a new food"
        )
        Text("Name")
        TextField(value = name, onValueChange = { v -> setName(v) }, singleLine = true)
        Text("Description")
        TextField(
            value = description, onValueChange = { v -> setDescription(v) }, singleLine = false
        )
        Spacer(Modifier.size(10.dp))
        Button(
            onClick = { pickMedia.launch(arrayOf("image/*")) },
        ) {
            Text(
                fontSize = 8.em, text = "Add image"
            )
        }
        Button(
            onClick = { if (hasCameraPermission) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "JPEG_${timeStamp}_"
                val storageDir = context.getExternalFilesDir(null)
                val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )
                setCameraImageUri(uri.toString())
                cameraLauncher.launch(uri)
            } else {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            },
        ) {
            Text(
                fontSize = 8.em, text = "Take image"
            )
        }
        Spacer(Modifier.size(56.dp))
        Button(
            onClick = { onAddFood(Food(name = name, description = description, imageUrl = savedImageUri)) },
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        ) {

            Text(
                fontSize = 8.em, text = "Create"
            )
        }
        AsyncImage(model = imageUri, contentDescription = null)
    }
}