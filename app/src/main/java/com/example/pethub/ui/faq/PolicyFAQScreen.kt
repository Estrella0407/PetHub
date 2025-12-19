package com.example.pethub.ui.faqimport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.ui.faq.PolicyFAQViewModel
import com.example.pethub.ui.faq.QAItem
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.DarkBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyFAQScreen(
    viewModel: PolicyFAQViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text("Policy FAQ", fontWeight = FontWeight.Bold,color=DarkBrown) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamBackground,
                    titleContentColor = CreamDark,
                    navigationIconContentColor = CreamDark
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.questions) { qaItem ->
                ExpandableFAQItem(qaItem = qaItem)
            }
        }
    }
}

@Composable
private fun ExpandableFAQItem(qaItem: QAItem) {
    var isExpanded by remember { mutableStateOf(false) }

    val formattedAnswer = buildAnnotatedString {
        val boldRegex = """\*\*(.*?)\*\*""".toRegex()
        var lastIndex = 0

        boldRegex.findAll(qaItem.answer).forEach { matchResult ->
            val range = matchResult.range
            // Append text before the bold part
            append(qaItem.answer.substring(lastIndex, range.first))
            // Append bold text
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(matchResult.groupValues[1])
            }
            lastIndex = range.last + 1
        }
        // Append remaining text
        if (lastIndex < qaItem.answer.length) {
            append(qaItem.answer.substring(lastIndex))
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = qaItem.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            Text(
                text = formattedAnswer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
            )
        }
        Divider(color = CreamDark.copy(alpha = 0.2f))
    }
}
