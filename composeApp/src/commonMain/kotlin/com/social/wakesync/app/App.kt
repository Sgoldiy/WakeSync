package com.social.wakesync.app

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.social.wakesync.feature.auth.AuthScreen
import com.social.wakesync.feature.home.MainHomeScreen
import com.social.wakesync.feature.onboarding.OnboardingScreen1
import com.social.wakesync.feature.onboarding.OnboardingScreen2
import com.social.wakesync.feature.onboarding.OnboardingScreen3
import com.social.wakesync.feature.permission.PermissionScreen
import com.social.wakesync.feature.profile.ProfileScreen
import com.social.wakesync.feature.splash.WakeSyncSplashScreen
import com.social.wakesync.feature.home.AlarmState
import com.social.wakesync.feature.home.AlarmPuzzleSolo
import com.social.wakesync.feature.home.AlarmPuzzleDuo
import com.social.wakesync.feature.home.AlarmPuzzleGroup
import com.social.wakesync.feature.home.StreakSaveScreen
import com.social.wakesync.feature.home.StreakBrokenScreen
import com.social.wakesync.feature.home.HomeViewModel
import com.social.wakesync.feature.home.AlarmLockScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.inter_variable
import myapplication.composeapp.generated.resources.space_grotesk_variable
import com.social.wakesync.ui.theme.AppColorPalette
import com.social.wakesync.ui.utils.BackHandler

@Composable
fun App(
    viewModel: MainViewModel,
    initiallyAuthenticated: Boolean,
    isPermissionsGranted: Boolean,
    onGoogleSignInRequested: ((String?) -> Unit) -> Unit,
    onDismissAlarm: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var authLoading by remember { mutableStateOf(false) }

    // Initialize Auth state
    LaunchedEffect(Unit) {
        viewModel.checkAuthState(initiallyAuthenticated, isPermissionsGranted)
    }

    val titleFamily = FontFamily(
        Font(Res.font.space_grotesk_variable, FontWeight.W700),
    )
    val interFamily = FontFamily(
        Font(Res.font.inter_variable, FontWeight.W600),
        Font(Res.font.inter_variable, FontWeight.W400),
    )

    MaterialTheme {
        val homeViewModel: HomeViewModel = viewModel { HomeViewModel() }
        val homeUiState by homeViewModel.uiState.collectAsState()

        var showLockScreen by remember(AlarmState.isRinging) {
            mutableStateOf(AlarmState.isRinging)
        }

        if (AlarmState.isRinging) {
            val onAlarmSolved = {
                val activeId = AlarmState.activeAlarmId ?: ""
                homeViewModel.recordAlarmWin(activeId, AlarmState.activeAlarmMode)
                onDismissAlarm()
                AlarmState.showStreakSave = true
            }
            val onAlarmFailed = {
                val activeId = AlarmState.activeAlarmId ?: ""
                homeViewModel.recordAlarmLoss(activeId, AlarmState.activeAlarmMode)
                onDismissAlarm()
                AlarmState.showStreakBroken = true
            }

            if (showLockScreen) {
                // Group avatars — shown only for Group/Duo mode
                val groupAvatars = when (AlarmState.activeAlarmMode) {
                    "Group" -> listOf("🐺", "🦊", "🐻", "🦁")
                    "Duo"   -> listOf(homeUiState.avatarEmoji.ifEmpty { "🤯" })
                    else    -> emptyList()
                }
                AlarmLockScreen(
                    alarmTime = homeUiState.nextAlarmTime,
                    alarmName = "${AlarmState.activeAlarmMode} Alarm",
                    mode = AlarmState.activeAlarmMode,
                    challengeName = AlarmState.activeAlarmChallenge,
                    groupAvatars = groupAvatars,
                    titleFamily = titleFamily,
                    interFamily = interFamily,
                    onWake = { showLockScreen = false }
                )
            } else {
                when (AlarmState.activeAlarmMode) {
                    "Duo" -> {
                        AlarmPuzzleDuo(
                            onDismiss = onAlarmSolved,
                            onFailure = onAlarmFailed,
                            titleFamily = titleFamily,
                            interFamily = interFamily,
                            userName = homeUiState.userName.ifEmpty { "You" },
                            userAvatar = homeUiState.avatarEmoji.ifEmpty { "🤯" },
                            alarmId = AlarmState.activeAlarmId,
                            currentUserId = homeViewModel.getCurrentUserUid(),
                            onListenToDuoAlarm = { id -> homeViewModel.listenToDuoAlarm(id) },
                            onSetDuoAlarmWinner = { id, uid -> homeViewModel.setDuoAlarmWinner(id, uid) }
                        )
                    }
                    "Group" -> {
                        AlarmPuzzleGroup(
                            onDismiss = onAlarmSolved,
                            titleFamily = titleFamily,
                            interFamily = interFamily
                        )
                    }
                    else -> {
                        AlarmPuzzleSolo(
                            onDismiss = onAlarmSolved,
                            onFailure = onAlarmFailed,
                            titleFamily = titleFamily,
                            interFamily = interFamily
                        )
                    }
                }
            }
        } else if (AlarmState.showStreakSave) {
            StreakSaveScreen(
                streakDays = homeUiState.streak,
                finishPosition = 1,
                totalParticipants = if (AlarmState.activeAlarmMode == "Solo") 1 else 2,
                sleepingFriends = if (AlarmState.activeAlarmMode == "Solo") emptyList() else listOf("🦁"),
                friendsLostCount = 0,
                onShareClick = { /* TODO: share logic */ },
                onBackToHome = { AlarmState.showStreakSave = false },
                titleFamily = titleFamily,
                interFamily = interFamily
            )
        } else if (AlarmState.showStreakBroken) {
            StreakBrokenScreen(
                previousStreak = homeUiState.streak + 3,
                currentStreak = homeUiState.streak,
                punishmentText = "20 pushups 💪",
                punishmentDetail = "Photo proof required · Due 8:30 AM",
                onCompletePunishment = { AlarmState.showStreakBroken = false },
                onUseInsurance = { /* TODO: insurance/premium logic */ },
                onBackToHome = { AlarmState.showStreakBroken = false },
                titleFamily = titleFamily,
                interFamily = interFamily
            )
        } else {
            when (val state = uiState) {
                is MainUiState.Initial -> {
                    // To avoid flicker, show Splash or nothing while we start the check
                    WakeSyncSplashScreen(onFinished = { viewModel.setOnboarding() })
                }
                is MainUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().background(AppColorPalette.VoidBg),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColorPalette.CyanCta)
                    }
                }
                is MainUiState.Onboarding1, is MainUiState.Onboarding2, is MainUiState.Onboarding3 -> {
                    val step = when(state) {
                        is MainUiState.Onboarding1 -> 1
                        is MainUiState.Onboarding2 -> 2
                        else -> 3
                    }
                    
                    SwipeableOnboardingContainer(
                        step = step,
                        onStepChange = { newStep ->
                            if (newStep > step) viewModel.nextOnboarding()
                            else viewModel.previousOnboarding()
                        }
                    ) {
                        when (step) {
                            1 -> OnboardingScreen1(onNext = { viewModel.nextOnboarding() })
                            2 -> {
                                BackHandler { viewModel.previousOnboarding() }
                                OnboardingScreen2(onNext = { viewModel.nextOnboarding() })
                            }
                            else -> {
                                BackHandler { viewModel.previousOnboarding() }
                                OnboardingScreen3(onLetsGo = { viewModel.nextOnboarding() })
                            }
                        }
                    }
                }
                is MainUiState.Auth -> {
                    BackHandler { viewModel.previousOnboarding() }
                    AuthScreen(
                        isLoading = authLoading,
                        onGoogleSignIn = {
                            authLoading = true
                            onGoogleSignInRequested { message ->
                                authLoading = false
                                if (message == null) {
                                    viewModel.onAuthenticated(isPermissionsGranted)
                                }
                            }
                        }
                    )
                }
                is MainUiState.ProfileSetup -> {
                    BackHandler { viewModel.logout() }
                    ProfileScreen(
                        onProfileCreated = {
                            viewModel.onProfileCreated()
                        }
                    )
                }
                is MainUiState.Permissions -> {
                    BackHandler { /* Optional: show profile again? */ }
                    PermissionScreen(
                        onAllPermissionsGranted = {
                            viewModel.onPermissionsGranted()
                        }
                    )
                }
                is MainUiState.Home -> {
                    MainHomeScreen()
                }
            }
        }
    }
}

@Composable
private fun SwipeableOnboardingContainer(
    step: Int,
    onStepChange: (Int) -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(step) {
                var dragTotal = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        dragTotal += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        val threshold = 70f
                        when {
                            dragTotal < -threshold && step < 3 -> onStepChange(step + 1)
                            dragTotal > threshold && step > 1 -> onStepChange(step - 1)
                        }
                        dragTotal = 0f
                    },
                    onDragCancel = {
                        dragTotal = 0f
                    },
                )
            },
    ) {
        content()
    }
}
