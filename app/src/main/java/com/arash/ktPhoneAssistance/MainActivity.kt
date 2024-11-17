package com.arash.ktPhoneAssistance

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arash.ktPhoneAssistance.ui.theme.KtphoneassistanceTheme
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            }
        }


        // Initialize DevicePolicyManager
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        // Request Device Admin Permission if not already granted
        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to lock screen")
            startActivity(intent)
        }

        setContent {
            KtphoneassistanceTheme {
                AppUI(
                    onLockButtonClick = { lockScreen() },
                    onScreenshotButtonClick = { onScreenshotButtonClick() }
                )
            }
        }
    }

    private fun onScreenshotButtonClick() {
        takeScreenshot()
        Toast.makeText(this, "Screenshot captured successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun lockScreen() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            println("Lock Screen button clicked. Locking screen.")
            devicePolicyManager.lockNow()
        } else {
            println("Device Admin is not active!")
        }
    }

    private fun takeScreenshot() {
        println("Screenshot button clicked. Capturing screenshot.")
        // Root View of your Activity
        val rootView = window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(rootView.drawingCache)
        rootView.isDrawingCacheEnabled = false

        // Save the screenshot to storage
        saveBitmap(bitmap)
    }

//    private fun saveBitmap(bitmap: Bitmap) {
//        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//        val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshots")
//        if (!storageDir.exists()) {
//            storageDir.mkdirs()
//        }
//
//        val file = File(storageDir, "Screenshot_$timeStamp.png")
//        try {
//            val outputStream: OutputStream = FileOutputStream(file)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//            outputStream.flush()
//            outputStream.close()
//            // Notify the user
//            println("Screenshot saved: ${file.absolutePath}")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}

    private fun saveBitmap(bitmap: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Screenshot_$timeStamp.png"

        val resolver = applicationContext.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/Screenshots"
            )
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                println("Screenshot saved to Pictures/Screenshots")
            }
        } ?: println("Failed to save screenshot.")
    }
}


@Composable
fun AppUI(onLockButtonClick: () -> Unit, onScreenshotButtonClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Space between buttons
        ) {
            Button(
                onClick = { onLockButtonClick() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Lock Screen")
            }

            Button(
                onClick = { onScreenshotButtonClick() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Take Screenshot")
            }
        }
    }
}
