package com.example.pethub.ui.StockManagement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pethub.R
import com.example.pethub.data.repository.StockItem
import com.example.pethub.navigation.AdminBottomNavigationBar
import com.example.pethub.ui.admin.AdminTopBar
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamFair
import com.example.pethub.ui.theme.DarkBrown

@Composable
fun StockManagementScreen(
    viewModel: StockManagementViewModel = hiltViewModel(),
    onNavigateToAdminHome: () -> Unit,
    onNavigateToAdminServices: () -> Unit,
    onNavigateToAdminScanner: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AdminTopBar(onLogoutClick = {
                viewModel.logout(onLogoutClick)
            })
        },
        bottomBar = {
            AdminBottomNavigationBar(
                modifier = Modifier,
                currentRoute = "admin_stocks",
                onNavigate = { route ->
                    when (route) {
                        "admin_home" -> onNavigateToAdminHome()
                        "admin_stocks" -> { /* Current */ }
                        "admin_services" -> onNavigateToAdminServices()
                        "admin_scanner" -> onNavigateToAdminScanner()
                    }
                }
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Stock Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is StockUiState.Loading -> LoadingScreen()
                is StockUiState.Error -> ErrorScreen(message = state.message) { }
                is StockUiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(state.stocks) { item ->
                            StockProductItem(
                                item = item,
                                onStockChange = { newStock ->
                                    viewModel.updateStock(item.documentId, newStock)
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 16.dp),
                                color = Color.LightGray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockProductItem(
    item: StockItem,
    onStockChange: (Int) -> Unit
) {
    var stockText by remember(item.stockCount) { mutableStateOf(item.stockCount.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Image
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.productName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                error = painterResource(id = R.drawable.pethub_rvbg) // Placeholder
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.productName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Stock",
                    fontSize = 16.sp,
                    color = Color.Black
                )

                // Stock Edit Pill
                Row(
                    modifier = Modifier
                        .background(CreamFair, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = stockText,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                stockText = it
                                it.toIntOrNull()?.let { newStock ->
                                    onStockChange(newStock)
                                }
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(IntrinsicSize.Min).minWidth(30.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

fun Modifier.minWidth(minWidth: androidx.compose.ui.unit.Dp) = this.then(
    Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val width = maxOf(placeable.width, minWidth.roundToPx())
        layout(width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
)
