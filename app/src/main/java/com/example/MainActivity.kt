package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.calculator.AppTab
import com.example.calculator.CalculatorViewModel
import com.example.calculator.ThemeMode
import com.example.ui.CalculatorScreen
import com.example.ui.HistoryBottomSheet
import com.example.ui.TipCalculatorScreen
import com.example.ui.UnitConverterScreen
import com.example.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            CalculatorTheme(darkTheme = isDark) {
                CalculatorApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CalculatorApp(viewModel: CalculatorViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val showHistorySheet by viewModel.showHistorySheet.collectAsState()
    val historyList by viewModel.history.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.CALCULATOR,
                    onClick = { viewModel.setTab(AppTab.CALCULATOR) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                    label = { Text("Calculator") },
                    modifier = Modifier.testTag("tab_calculator")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.CONVERTER,
                    onClick = { viewModel.setTab(AppTab.CONVERTER) },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Converter") },
                    label = { Text("Converter") },
                    modifier = Modifier.testTag("tab_converter")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.TIP,
                    onClick = { viewModel.setTab(AppTab.TIP) },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Tip & Split") },
                    label = { Text("Tip & Split") },
                    modifier = Modifier.testTag("tab_tip")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tabTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                AppTab.CALCULATOR -> CalculatorScreen(viewModel = viewModel)
                AppTab.CONVERTER -> UnitConverterScreen(viewModel = viewModel)
                AppTab.TIP -> TipCalculatorScreen(viewModel = viewModel)
            }
        }

        if (showHistorySheet) {
            HistoryBottomSheet(
                historyList = historyList,
                onDismiss = { viewModel.toggleHistorySheet(false) },
                onSelectItem = { item -> viewModel.onSelectHistory(item) },
                onDeleteItem = { id -> viewModel.onDeleteHistory(id) },
                onClearAll = { viewModel.onClearAllHistory() }
            )
        }
    }
}
