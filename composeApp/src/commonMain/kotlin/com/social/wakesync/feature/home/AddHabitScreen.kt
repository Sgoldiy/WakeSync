package com.social.wakesync.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import com.social.wakesync.ui.utils.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    habit: Habit? = null,
    onBack: () -> Unit,
    onSave: (title: String, icon: HabitIconType, frequency: String, reminderTime: String, partnerUsername: String?, bondName: String?) -> Unit,
    friends: List<Friend>,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var title by remember { mutableStateOf(habit?.title ?: "") }
    var selectedIcon by remember { mutableStateOf(habit?.iconType ?: HabitIconType.RUN) }
    var selectedFrequency by remember { mutableStateOf(habit?.frequency ?: "Daily") }
    var reminderTime by remember { mutableStateOf(habit?.reminderTime ?: "6:15 AM") }
    var isAccountabilityEnabled by remember { mutableStateOf(habit?.partnerUsername?.isNotBlank() == true) }
    var selectedPartnerUsername by remember { mutableStateOf<String?>(habit?.partnerUsername) }
    var bondName by remember { mutableStateOf(habit?.bondName ?: "") }
    
    // Set default partner if list is not empty
    LaunchedEffect(friends) {
        if (selectedPartnerUsername == null && friends.isNotEmpty()) {
            selectedPartnerUsername = friends.firstOrNull()?.name
        }
    }

    val emoji = when (selectedIcon) {
        HabitIconType.RUN -> "🏃"
        HabitIconType.SHOWER -> "🚿"
        HabitIconType.NO_PHONE -> "📵"
        HabitIconType.READING -> "📚"
        HabitIconType.STRETCH -> "🧘"
    }

    Scaffold(
        containerColor = AppColorPalette.VoidBg,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColorPalette.VoidBg)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.04f))
                    ) {
                        Text(
                            text = "←",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = if (habit == null) "New Habit" else "Edit Habit",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Habit Name
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "HABIT NAME",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily,
                        letterSpacing = 1.sp
                    )

                    // Border colored cyan when text is present
                    val activeBorderColor = if (title.isNotBlank()) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.08f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppColorPalette.Surface)
                            .border(1.5.dp, activeBorderColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tappable Emoji Icon cycling on click
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.04f))
                                .clickable {
                                    // Cycle selected icon type on tap!
                                    selectedIcon = when (selectedIcon) {
                                        HabitIconType.RUN -> HabitIconType.SHOWER
                                        HabitIconType.SHOWER -> HabitIconType.NO_PHONE
                                        HabitIconType.NO_PHONE -> HabitIconType.READING
                                        HabitIconType.READING -> HabitIconType.STRETCH
                                        HabitIconType.STRETCH -> HabitIconType.RUN
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { 
                                Text(
                                    "Morning Run", 
                                    color = Color.White.copy(alpha = 0.2f),
                                    fontFamily = interFamily,
                                    fontSize = 15.sp
                                ) 
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = AppColorPalette.CyanCta,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Section 2: Frequency
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FREQUENCY",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Daily", "Weekdays", "Custom").forEach { freq ->
                            val isSelected = selectedFrequency == freq
                            val pillBorderColor = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.08f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.08f) else AppColorPalette.Surface)
                                    .border(1.5.dp, pillBorderColor, RoundedCornerShape(14.dp))
                                    .clickable { selectedFrequency = freq },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = freq,
                                    color = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFamily
                                )
                            }
                        }
                    }
                }

                // Section 3: Reminder Time
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "REMINDER TIME",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppColorPalette.Surface)
                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                            .clickable {
                                // Toggle simple reminder hour options on tap
                                reminderTime = when (reminderTime) {
                                    "6:15 AM" -> "7:00 AM"
                                    "7:00 AM" -> "8:30 AM"
                                    "8:30 AM" -> "9:00 PM"
                                    else -> "6:15 AM"
                                }
                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = reminderTime,
                            color = AppColorPalette.CyanCta,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFamily
                        )

                        Text("⏰", fontSize = 18.sp)
                    }
                }

                // Section 4: Accountability Partner Switch
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Accountability Partner",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                            Text(
                                text = "Friends can see and verify your check-ins",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontFamily = interFamily
                            )
                        }

                        Switch(
                            checked = isAccountabilityEnabled,
                            onCheckedChange = { isAccountabilityEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppColorPalette.CyanCta,
                                uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                            )
                        )
                    }
                }

                // Section 5: Choose Partner (Conditional on Switch)
                if (isAccountabilityEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CHOOSE PARTNER",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFamily,
                            letterSpacing = 1.sp
                        )

                        if (friends.isEmpty()) {
                            Text(
                                text = "No friends added yet to invite.",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 12.sp,
                                fontFamily = interFamily
                            )
                        } else {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(friends) { friend ->
                                    val isSelected = selectedPartnerUsername == friend.name
                                    val pillBorderColor = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.08f)
                                    Row(
                                        modifier = Modifier
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.08f) else AppColorPalette.Surface)
                                            .border(1.5.dp, pillBorderColor, RoundedCornerShape(12.dp))
                                            .clickable { selectedPartnerUsername = friend.name }
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = friend.avatar, fontSize = 14.sp)
                                        Text(
                                            text = friend.name,
                                            color = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.6f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFamily
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bond/Group Name input
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "BOND / GROUP NAME",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = interFamily,
                                letterSpacing = 1.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(AppColorPalette.Surface)
                                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.TextField(
                                    value = bondName,
                                    onValueChange = { bondName = it },
                                    placeholder = {
                                        Text(
                                            text = "Name your bond (e.g. Accountability Squad)",
                                            color = Color.White.copy(alpha = 0.2f),
                                            fontFamily = interFamily,
                                            fontSize = 14.sp
                                        )
                                    },
                                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = AppColorPalette.CyanCta,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // Create Habit button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val partner = if (isAccountabilityEnabled) selectedPartnerUsername else null
                        val name = if (isAccountabilityEnabled && bondName.isNotBlank()) bondName else null
                        onSave(title, selectedIcon, selectedFrequency, reminderTime, partner, name)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = AppColorPalette.CyanCta.copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColorPalette.CyanCta,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.08f),
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (habit == null) "Create Habit" else "Save Changes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = titleFamily
                    )
                    Text("🔄", fontSize = 14.sp)
                }
            }
        }
    }
}
