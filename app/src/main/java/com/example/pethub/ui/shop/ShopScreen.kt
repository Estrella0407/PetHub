package com.example.pethub.ui.shop

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pethub.data.model.Product
import com.example.pethub.navigation.BottomNavigationBar
import java.util.Locale

// --- Custom Colors ---
val CreamBg = Color(0xFFFBF9F1)
val SidebarTextNormal = Color(0xFF5D534A)
val SidebarTextSelected = Color(0xFF2C2622)
val DividerColor = Color(0xFF8D6E63)
val CartButtonColor = Color(0xFFFDF1C6)

// --- CONSTANTS ---
val CategoryItemHeight = 80.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: ShopViewModel = hiltViewModel(),
    onNavigateToCart: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    // 1. Collect Data from Firebase ViewModel
    val products by viewModel.products.collectAsState()

    // 2. Local UI State (or move to ViewModel if you prefer)
    var selectedCategory by remember { mutableStateOf("Pet Food") }

    val cartItemCount by viewModel.cartItemCount.collectAsState()

    val categories = listOf("Pet Food", "Pet Treats", "Pet Toys", "Grooming Products")

    Scaffold(
        containerColor = CreamBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shop",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = SidebarTextSelected
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PetHub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SidebarTextSelected
                    )
                    Box(modifier = Modifier.size(6.dp).background(SidebarTextSelected, CircleShape))
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
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
            Surface(
                onClick = onNavigateToCart,
                color = CartButtonColor,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cart",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box {
                        Icon(Icons.Outlined.ShoppingCart, null, tint = Color(0xFF4E342E))
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = Color(0xFFE57373),
                                modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-5).dp)
                            ) { Text(cartItemCount.toString()) }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- SIDEBAR MENU ---
            SidebarMenu(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it }
            )

            // --- FILTER PRODUCTS ---
            // This filters the list downloaded from Firebase based on local selection
            // val filteredProducts = products.filter { it.category == selectedCategory }
            val filteredProducts = products.filter { product ->
                product.category.trim().equals(selectedCategory.trim(), ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredProducts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No items found", color = SidebarTextNormal)
                        }
                    }
                } else {
                    items(filteredProducts) { product ->
                        ProductItem(
                            product = product,
                            onAddClick = { viewModel.addToCart(product) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun SidebarMenu(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    val selectedIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)
    val dotSize = 24.dp
    val lineWidth = 4.dp
    val initialTopOffset = 40.dp

    val targetYOffset by animateDpAsState(
        targetValue = initialTopOffset + (selectedIndex * CategoryItemHeight) + (CategoryItemHeight / 2) - (dotSize / 2),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "dotAnimation"
    )

    Row(
        modifier = Modifier.width(120.dp).fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(top = 10.dp)
        ) {
            Text(
                text = "Menu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SidebarTextNormal,
                modifier = Modifier.padding(start = 16.dp, bottom = 20.dp)
            )

            categories.forEach { category ->
                Box(
                    modifier = Modifier
                        .height(CategoryItemHeight)
                        .fillMaxWidth()
                        .clickable { onCategorySelect(category) },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = category.replace(" ", "\n"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Medium,
                        color = if (category == selectedCategory) SidebarTextSelected else SidebarTextNormal,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier.width(dotSize).fillMaxHeight().padding(top = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(lineWidth)
                    .background(DividerColor.copy(alpha = 0.3f), RoundedCornerShape(50))
                    .align(Alignment.Center)
            )
            Box(
                modifier = Modifier
                    .offset(y = targetYOffset)
                    .size(dotSize)
                    .background(DividerColor, CircleShape)
                    .border(3.dp, CreamBg, CircleShape)
                    .align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(70.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SidebarTextSelected
            )
            Text(
                text = product.description, // Ensure your Product model has this field, or use product.category
                style = MaterialTheme.typography.bodySmall,
                color = SidebarTextNormal,
                maxLines = 2
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
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4E342E)
                )

                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = SidebarTextNormal,
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.5.dp, SidebarTextNormal, CircleShape)
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}
