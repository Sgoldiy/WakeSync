package com.social.wakesync.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import com.social.wakesync.ui.utils.BackHandler
import kotlin.random.Random

data class RivalItem(
    val username: String,
    val avatar: String,
    val streak: Int,
    val rank: String,
    val mutualCount: Int
)

@Composable
fun FindRivalsScreen(
    onBack: () -> Unit,
    onRivalSelected: (String) -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var searchQuery by remember { mutableStateOf("") }

    val suggestedRivals = remember {
        listOf(
            RivalItem("5amclub_dani", "🐺", 89, "#3", 3),
            RivalItem("earlybird_rin", "🐦", 44, "#12", 1),
            RivalItem("grindset.alex", "🦅", 29, "#21", 2),
            RivalItem("zero_snooze", "🐊", 66, "#5", 0)
        )
    }

    val filteredRivals = remember(searchQuery) {
        suggestedRivals.filter {
            it.username.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                com.social.wakesync.ui.utils.LongArrowBackIcon(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Find Rivals",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily
            )
        }

        // Search Username Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F1524))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search username...",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 15.sp,
                            fontFamily = interFamily
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = interFamily
                        ),
                        cursorBrush = SolidColor(AppColorPalette.CyanCta),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions Row (Sync Contacts & Share Code)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sync Contacts
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF061A24))
                    .border(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .clickable { /* Sync contacts action */ },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📱", fontSize = 16.sp)
                    Text(
                        text = "Sync Contacts",
                        color = AppColorPalette.CyanCta,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }

            // Share Code
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F1524))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .clickable { /* Share code action */ },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔗", fontSize = 16.sp)
                    Text(
                        text = "Share Code",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Suggested Rivals Section
        Text(
            text = "SUGGESTED RIVALS",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = interFamily,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredRivals) { rival ->
                RivalRow(
                    rival = rival,
                    onAddClick = { onRivalSelected(rival.username) },
                    interFamily = interFamily
                )
            }
        }
    }
}

@Composable
private fun RivalRow(
    rival: RivalItem,
    onAddClick: () -> Unit,
    interFamily: FontFamily
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circular Avatar Container with Wolf/etc.
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1F30)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = rival.avatar, fontSize = 24.sp)
            }

            // Username and stats
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = rival.username,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🔥", fontSize = 11.sp)
                    Text(
                        text = "${rival.streak} · ${rival.rank} · ${rival.mutualCount} mutual",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontFamily = interFamily
                    )
                }
            }
        }

        // Add Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .border(BorderStroke(1.dp, AppColorPalette.CyanCta), RoundedCornerShape(99.dp))
                .clickable { onAddClick() }
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Add",
                color = AppColorPalette.CyanCta,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = interFamily
            )
        }
    }
}
