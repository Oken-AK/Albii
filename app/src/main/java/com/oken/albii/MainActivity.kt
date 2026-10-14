package com.oken.albii

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.oken.albii.ui.main.MainScreen
import com.oken.albii.ui.permission.PermissionGate
import com.oken.albii.ui.theme.AlbiiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlbiiTheme {
                PermissionGate {
                    MainScreen()
                }
            }
        }
    }
}
