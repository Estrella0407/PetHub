package com.example.pethub.ui.faq

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.DarkBrown

/**
 * FAQ Screen that lists all the help topics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    viewModel: FAQViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onTopicClick: (topicId: String) -> Unit // Callback for when a topic is clicked
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            FaqTopBar(onNavigateBack = onNavigateBack)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is FaqUiState.Loading -> LoadingScreen()
                is FaqUiState.Error -> ErrorScreen(message = state.message, onRetry = { /* TODO */ })
                is FaqUiState.Success -> {
                    FaqContent(
                        topics = state.topics,
                        onTopicClick = onTopicClick
                    )
                }
            }
        }
    }
}

/**
 * Top App Bar for the FAQ Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FaqTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            // Placeholder to keep title centered, matching home screen's action items
            Spacer(modifier = Modifier.width(48.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CreamBackground,
            titleContentColor = CreamDark,
            navigationIconContentColor = CreamDark
        )
    )
}

/**
 * The main content of the FAQ screen, displaying the list of topics.
 */
@Composable
private fun FaqContent(
    topics: List<FaqTopic>,
    onTopicClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // List of FAQ topics
        items(topics) { topic ->
            FaqTopicItem(
                topic = topic,
                onClick = { onTopicClick(topic.id) }
            )
        }
    }
}

/**
 * A single row item for an FAQ topic.
 */
@Composable
private fun FaqTopicItem(
    topic: FaqTopic,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = topic.title,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        // Divider added to separate each topic
        Divider(color = DarkBrown.copy(alpha = 0.2f))
    }
}
