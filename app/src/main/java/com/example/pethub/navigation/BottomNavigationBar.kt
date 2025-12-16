package com.example.pethub.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pethub.R
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.CreamLight
import com.example.pethub.ui.theme.DarkBrown
import com.example.pethub.ui.theme.MutedBrown

/**
 * Data class for Bottom Navigation items
 */
data class BottomNavItemData(
    val label: String,
    val route: String,
    @DrawableRes val iconRes: Int
)

/**
 * Bottom navigation bar
 */
@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItemData(label = "Home", route = "home", iconRes = R.drawable.customer_home),
        BottomNavItemData(label = "Services", route = "services", iconRes = R.drawable.admin_footer_service),
        BottomNavItemData(label = "Shop", route = "shop", iconRes = R.drawable.admin_footer_stock),
        BottomNavItemData(label = "Profile", route = "profile", iconRes = R.drawable.admin_footer_home)
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = CreamLight,
            contentColor = DarkBrown
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                BottomNavItem(
                    iconRes = item.iconRes,
                    label = item.label,
                    isSelected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

/**
 * Bottom navigation item
 */
@Composable
private fun BottomNavItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = if (isSelected) DarkBrown else MutedBrown,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = label,
            modifier = Modifier.padding(top = 2.dp),
            color = if (isSelected) DarkBrown else MutedBrown,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
