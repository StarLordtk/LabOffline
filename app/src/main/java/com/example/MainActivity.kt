package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.model.RegistrationEntity
import com.example.ui.components.LimsBottomNavigation
import com.example.ui.components.LimsTopAppBar
import com.example.ui.components.ReportViewerDialog
import com.example.ui.components.RoleSwitcherDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LimsScreen
import com.example.ui.viewmodel.LimsViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LimsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                LimsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LimsApp(viewModel: LimsViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentStaff by viewModel.currentStaff.collectAsState()
    val allStaff by viewModel.staffList.collectAsState()
    val labSettings by viewModel.labSettings.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val selectedRegForResults by viewModel.selectedRegistrationForResults.collectAsState()
    val selectedRegForReport by viewModel.selectedRegistrationForReport.collectAsState()

    var showRoleSwitcherDialog by remember { mutableStateOf(false) }
    var showFinanceAnalytics by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Back handling
    BackHandler(enabled = selectedRegForResults != null || showFinanceAnalytics || currentScreen != LimsScreen.DASHBOARD) {
        when {
            selectedRegForResults != null -> viewModel.selectRegistrationForResults(null)
            showFinanceAnalytics -> showFinanceAnalytics = false
            currentScreen != LimsScreen.DASHBOARD -> viewModel.navigateTo(LimsScreen.DASHBOARD)
        }
    }

    Scaffold(
        topBar = {
            if (selectedRegForResults == null && !showFinanceAnalytics) {
                LimsTopAppBar(
                    labName = labSettings?.labName ?: "Apex Clinical Diagnostics",
                    currentStaff = currentStaff,
                    onRoleClick = { showRoleSwitcherDialog = true },
                    onSearchClick = {
                        viewModel.navigateTo(LimsScreen.SAMPLES_TESTS)
                    }
                )
            }
        },
        bottomBar = {
            if (selectedRegForResults == null && !showFinanceAnalytics) {
                LimsBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                selectedRegForResults != null -> {
                    ResultEntryScreen(
                        reg = selectedRegForResults!!,
                        viewModel = viewModel,
                        onBack = { viewModel.selectRegistrationForResults(null) },
                        onViewReport = {
                            val r = selectedRegForResults
                            viewModel.selectRegistrationForResults(null)
                            viewModel.selectRegistrationForReport(r)
                        }
                    )
                }
                showFinanceAnalytics -> {
                    FinanceAnalyticsScreen(
                        viewModel = viewModel,
                        onBack = { showFinanceAnalytics = false }
                    )
                }
                else -> {
                    when (currentScreen) {
                        LimsScreen.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigate = { screen -> viewModel.navigateTo(screen) },
                                onOpenRegistrationDetail = { reg ->
                                    viewModel.selectRegistrationForReport(reg)
                                },
                                onOpenResultEntry = { reg ->
                                    viewModel.selectRegistrationForResults(reg)
                                }
                            )
                        }
                        LimsScreen.REGISTRATION -> {
                            RegistrationScreen(
                                viewModel = viewModel,
                                onBookingSuccess = { regNumber ->
                                    viewModel.setSamplesStatusFilter("ALL")
                                    viewModel.navigateTo(LimsScreen.SAMPLES_TESTS)
                                }
                            )
                        }
                        LimsScreen.SAMPLES_TESTS -> {
                            SamplesAndTestsScreen(
                                viewModel = viewModel,
                                onOpenResultEntry = { reg ->
                                    viewModel.selectRegistrationForResults(reg)
                                },
                                onOpenReport = { reg ->
                                    viewModel.selectRegistrationForReport(reg)
                                }
                            )
                        }
                        LimsScreen.PATIENTS -> {
                            PatientDirectoryScreen(
                                viewModel = viewModel,
                                onNavigate = { screen -> viewModel.navigateTo(screen) },
                                onOpenRegistration = { reg ->
                                    viewModel.selectRegistrationForReport(reg)
                                }
                            )
                        }
                        LimsScreen.BILLING_FINANCE -> {
                            BillingScreen(
                                viewModel = viewModel,
                                onOpenAnalytics = { showFinanceAnalytics = true }
                            )
                        }
                        LimsScreen.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    // Role Switcher Dialog
    if (showRoleSwitcherDialog) {
        RoleSwitcherDialog(
            currentStaff = currentStaff,
            allStaff = allStaff,
            onSelectStaff = { newStaff ->
                viewModel.switchStaff(newStaff)
                viewModel.showSnackbar("Switched user to ${newStaff.fullName} (${newStaff.role})")
            },
            onDismiss = { showRoleSwitcherDialog = false }
        )
    }

    // Report Viewer Dialog
    if (selectedRegForReport != null) {
        ReportViewerDialog(
            reg = selectedRegForReport!!,
            viewModel = viewModel,
            onDismiss = { viewModel.selectRegistrationForReport(null) }
        )
    }
}
