package com.smartsort.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartsort.app.MainViewModel
import com.smartsort.app.ui.settings.SettingsScreen
import com.smartsort.app.ui.theme.Palette

@Composable
fun AppRoot(vm: MainViewModel) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showSettings by remember { mutableStateOf(false) }
    var settingsOpened by remember { mutableStateOf(false) }

    LaunchedEffect(vm) {
        vm.toasts.collect { message -> snackbar.showSnackbar(message) }
    }

    // 关闭设置页时立刻落盘，不等防抖；首次启动 showSettings 就是 false，用标记跳过
    LaunchedEffect(showSettings) {
        if (showSettings) {
            settingsOpened = true
        } else if (settingsOpened) {
            vm.flush()
        }
    }

    BackHandler(enabled = showSettings) { showSettings = false }

    Box(Modifier.fillMaxSize().background(Palette.Bg)) {
        MainScreen(
            state = state,
            onOpen = { vm.openBin(it) },
            onClose = { vm.closeBin(it) },
            onAllOpen = { vm.allOpen() },
            onAllClose = { vm.allClose() },
            onKeepOpen = { vm.keepOpen() },
            onReconnect = { vm.connect() },
            onSettings = { showSettings = true }
        )

        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            SettingsScreen(
                state = state,
                onBack = { showSettings = false },
                onDraft = { vm.onDraft(it) }
            )
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}
