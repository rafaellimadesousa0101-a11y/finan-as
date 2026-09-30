package com.example.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Slate400

@Composable
fun MinFinanceBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        BottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNavigate(screen.route)
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.titlePtBr,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.titlePtBr,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Emerald500,
                    selectedTextColor = Emerald500,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400,
                    indicatorColor = Emerald500.copy(alpha = 0.12f)
                ),
                modifier = Modifier.testTag("nav_item_${screen.route}")
            )
        }
    }
}
