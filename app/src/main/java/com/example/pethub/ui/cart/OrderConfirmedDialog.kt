package com.example.pethub.ui.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pethub.R

@Composable
fun OrderConfirmedDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF9F1)), // Cream Background
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Illustration
                Image(
                    painter = painterResource(id = R.drawable.pethub_logo_rvbg), // Your logo/image
                    contentDescription = "Order Confirmed",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Title
                Text(
                    text = "Order Confirmed!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2622) // Dark Text
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Subtitle
                Text(
                    text = "Kindly show your profile to\nour staff to pick up the order.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF5D534A), // Sidebar/Grey Text
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. OK Button
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDF1C6)), // Pastel Yellow
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "OK",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E) // Brown Text
                    )
                }
            }
        }
    }
}
