package com.ctom.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ctom.player.ui.CtomPlayerApp
import com.ctom.player.ui.theme.CtomPlayerTheme

class MainActivity : ComponentActivity() {
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        setContentView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView()
    }

    private fun setContentView() {
        val permissions = requiredPermissions()
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        setContent {
            CtomPlayerTheme {
                CtomPlayerApp(
                    hasMediaPermission = missing.isEmpty(),
                    initialDestination = intent.getStringExtra(EXTRA_DESTINATION),
                    requestMediaPermission = {
                        if (missing.isEmpty()) setContentView()
                        else permissionsLauncher.launch(missing.toTypedArray())
                    },
                )
            }
        }
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    companion object {
        private const val EXTRA_DESTINATION = "destination"
    }
}