package com.vusports.bc220200768

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.navigation.compose.rememberNavController
import com.vusports.bc220200768.ui.theme.VuSportsSocietyTheme
import com.vusports.bc220200768.ui.theme.VuSportsSocietyTheme
import com.vusports.bc220200768.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VuSportsSocietyTheme  {
                Surface {
                    val navController = rememberNavController()
                    AppNavigation(navController)
                }
            }
        }
    }
}
