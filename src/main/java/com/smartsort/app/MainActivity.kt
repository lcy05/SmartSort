package com.smartsort.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsort.app.ui.AppRoot
import com.smartsort.app.ui.theme.SmartSortTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartSortTheme {
                val vm: MainViewModel = viewModel()
                AppRoot(vm)
            }
        }
    }
}
