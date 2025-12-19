package com.example.pethub.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.data.model.Order
import com.example.pethub.data.model.ProductOrder
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    viewModel: OrderDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is OrderDetailUiState.Loading -> LoadingScreen()
                is OrderDetailUiState.Error -> ErrorScreen(message = state.message, onRetry = { viewModel.loadOrderDetails() })
                is OrderDetailUiState.Success -> {
                    OrderDetailContent(order = state.order, productOrders = state.productOrders)
                }
            }
        }
    }
}

@Composable
fun OrderDetailContent(order: Order, productOrders: List<ProductOrder>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Order Summary Card ---
        item {
            OrderSummaryCard(order)
        }

        // --- Product List Card ---
        item {
            ProductListCard(productOrders)
        }

        // --- Total Price Card ---
        item {
            TotalPriceCard(order)
        }
    }
}

@Composable
private fun OrderSummaryCard(order: Order) {
    val statusColor = getStatusColor(order.status)

    val pickupDate = order.pickupDateTime
    val formattedPickupDate = if (pickupDate != null) {
        SimpleDateFormat("EEE, dd MMM yyyy 'at' hh:mm a", Locale.getDefault()).format(pickupDate.toDate())
    } else {
        "Not scheduled"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CreamFair)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Order #${order.orderId.take(8)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = order.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            HorizontalDivider()
            OrderDetailRow(label = "Pickup Time", value = formattedPickupDate)
        }
    }
}

@Composable
private fun ProductListCard(productOrders: List<ProductOrder>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CreamFair)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Items Ordered", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            if (productOrders.isEmpty()) {
                Text("No items found for this order.", color = Color.Gray)
            } else {
                productOrders.forEach { product ->
                    ProductItemRow(product)
                }
            }
        }
    }
}

@Composable
private fun ProductItemRow(product: ProductOrder) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${product.quantity}x", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
        Text(product.productName, modifier = Modifier.weight(1f))
        Text("$${String.format("%.2f", product.priceAtPurchase)}", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TotalPriceCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CreamFair)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total Amount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "$${String.format("%.2f", order.totalPrice)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = DarkBrown
            )
        }
    }
}

@Composable
fun OrderDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = MutedBrown,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Normal,
            color = DarkBrown,
            modifier = Modifier.weight(1f)
        )
    }
}

