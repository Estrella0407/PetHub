package com.example.pethub.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pethub.data.model.Product
import com.example.pethub.navigation.PetHubBottomBar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: ShopViewModel = hiltViewModel(),
    onNavigateToCart: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()

    val categories = listOf("Pet Food", "Pet Treats", "Pet Toys", "Grooming Products")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Shop",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2C2C)
                    )
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PetHub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C2C2C)
                        )
                        Box(
                             modifier = Modifier
                                 .size(6.dp)
                                 .background(Color(0xFF2C2C2C), CircleShape)
                                 .align(Alignment.Bottom)
                                 .padding(bottom = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFDF8E4) // Light cream background
                )
            )
        },
        bottomBar = {
            PetHubBottomBar(
                currentRoute = "shop",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "services" -> onNavigateToServices()
                        "shop" -> { /* Stay */ }
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCart,
                containerColor = Color(0xFFF5E6C8),
                contentColor = Color(0xFF5D4037),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Cart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge { Text(cartItemCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart"
                    )
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFDF8E4)) // Main background
        ) {
            // Sidebar Menu
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    // Vertical Line
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(Color(0xFF8D6E63))
                            .align(Alignment.CenterEnd)
                    )

                    Column(
                         verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        categories.forEach { category ->
                            CategoryItem(
                                name = category,
                                isSelected = category == selectedCategory,
                                onClick = { viewModel.selectCategory(category) }
                            )
                        }
                    }
                }
            }

            // Product List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val filteredProducts = products.filter { 
                    if (selectedCategory == "Grooming Products") 
                        it.category == "Grooming Products" 
                    else 
                        it.category == selectedCategory 
                }
                
                items(filteredProducts) { product ->
                    ProductItem(
                        product = product,
                        onAddClick = { viewModel.addToCart(product) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
             verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name.replace(" ", "\n"), // Split text for vertical layout feel
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF2C2C2C) else Color(0xFF5D4037),
                modifier = Modifier.weight(1f),
                lineHeight = 18.sp
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF5D4037), CircleShape)
                )
            } else {
                 Spacer(modifier = Modifier.width(12.dp)) // Placeholder for dot alignment
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Image
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(80.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Placeholder if no image
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Product Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C)
            )
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5D4037)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RM${String.format(Locale.getDefault(), "%.2f", product.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
                
                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to cart",
                        tint = Color(0xFF2C2C2C),
                         modifier = Modifier
                             .border(1.5.dp, Color(0xFF2C2C2C), CircleShape)
                             .padding(2.dp)
                             .size(20.dp)

                    )
                }
            }
        }
    }
}
