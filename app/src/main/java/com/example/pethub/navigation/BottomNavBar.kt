package com.example.pethub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun PetHubBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFFFDF8E4), // Cream background matching design
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem("Home", "home", Icons.Outlined.Home, Icons.Filled.Home),
            BottomNavItem("Services", "services", Icons.Outlined.Pets, Icons.Filled.Pets),
            BottomNavItem("Shop", "shop", Icons.Outlined.ShoppingBag, Icons.Filled.ShoppingBag),
            BottomNavItem("Profile", "profile", Icons.Outlined.Person, Icons.Filled.Person)
        )

        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (selected) Color(0xFF5D4037) else Color(0xFF8D6E63)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color(0xFF5D4037) else Color(0xFF8D6E63)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFFFE0B2) // Light peach highlight
                )
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)
