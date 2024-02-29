@file:OptIn(ExperimentalMaterial3Api::class)

package com.k2fsa.sherpa.onnx.tts.engine.ui

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.service.DownloadModelService
import com.k2fsa.sherpa.onnx.tts.engine.ui.models.ModelManagerScreen
import com.k2fsa.sherpa.onnx.tts.engine.ui.settings.SettingsScreen
import com.k2fsa.sherpa.onnx.tts.engine.ui.theme.SherpaOnnxTtsEngineTheme
import com.k2fsa.sherpa.onnx.tts.engine.utils.navigateSingleTop
import com.k2fsa.sherpa.onnx.tts.engine.utils.toast

const val TAG = "sherpa-onnx-tts-engine"

val LocalNavController = compositionLocalOf<NavController> {
    error("No NavController provided")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        startService(Intent(this, DownloadModelService::class.java).apply {
//            data =
//                "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-nl_BE-nathalie-x_low.tar.bz2".toUri()
//            putExtra(
//                DownloadModelService.EXTRA_FILE_NAME,
//                "test_25mb.tar.bz2"
//            )
//        })

        setContent {
            var lastBackDownTime by remember { mutableLongStateOf(0L) }
            BackHandler() {
                val duration = 2000
                SystemClock.elapsedRealtime().let {
                    if (it - lastBackDownTime <= duration) {
                        finish()
                    } else {
                        lastBackDownTime = it
                        toast(R.string.press_back_again_to_exit)
                    }
                }
            }
            NotificationPermissionChecker()
            SherpaOnnxTtsEngineTheme {
                val navController = rememberNavController()
                navController.enableOnBackPressed(false)
                val entryState by navController.currentBackStackEntryAsState()
                Column {
                    CompositionLocalProvider(LocalNavController provides navController) {
                        NavHost(
                            modifier = Modifier.weight(1f),
                            navController = navController,
                            startDestination = NavRoutes.ModelManager.id
                        ) {
                            composable(NavRoutes.ModelManager.id) {
                                ModelManagerScreen()
                            }

                            composable(NavRoutes.Settings.id) {
                                SettingsScreen()
                            }
                        }
                    }

                    fun containsRoute(route: String): Boolean {
                        return entryState?.destination?.route?.contains(route) ?: false
                    }

                    NavigationBar {
                        @Composable
                        fun Item(strId: Int, id: String, icon: ImageVector) {
                            NavigationBarItem(
                                alwaysShowLabel = false,
                                selected = containsRoute(id),
                                onClick = {
                                    navController.navigateSingleTop(id, popUpToMain = true)
                                },
                                icon = { Icon(icon, contentDescription = null) },
                                label = {
                                    Text(stringResource(strId))
                                }
                            )
                        }

                        Item(
                            R.string.model_manager,
                            NavRoutes.ModelManager.id,
                            Icons.Default.Home
                        )

                        Item(
                            R.string.settings,
                            NavRoutes.Settings.id,
                            Icons.Default.Settings
                        )
                    }
                }
            }
        }
    }
}