package com.example.pethub.ui.pet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable // Added for the Remove button
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pethub.data.model.Pet
import com.example.pethub.ui.pet.PetProfileUiState
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.CreamLight
import com.example.pethub.util.calculateAge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(
    viewModel: PetProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // The launcher is kept here so it can be called from an Edit button later
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.onImageSelected(it) }
        }
    )

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text("Pet Profile", fontWeight = FontWeight.Bold, color = CreamDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is PetProfileUiState.Loading -> LoadingScreen()
            is PetProfileUiState.Error -> ErrorScreen(message = state.message, onRetry = { /*TODO*/ })
            is PetProfileUiState.Success -> {
                // State to manage which field is being edited
                var currentlyEditing by remember { mutableStateOf<String?>(null) }

                val pet = state.pet
                val age = calculateAge(pet.dateOfBirth)
                val formattedDob = pet.dateOfBirth?.let {
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(it))
                } ?: "Not set"

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    item {
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = pet.imageUrl,
                                contentDescription = pet.petName,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                // Image is not clickable
                                contentScale = ContentScale.Crop
                            )
                            if (state.isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(120.dp),
                                    strokeWidth = 4.dp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = pet.petName, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = CreamDark)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // --- Details Card ---
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamLight)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    EditablePetInfoField(
                                        label = "Gender",
                                        value = pet.sex,
                                        isEditing = currentlyEditing == "Gender",
                                        onEditClick = { currentlyEditing = "Gender" },
                                        onValueChange = { /* TODO: viewModel.updateGender(it) */ },
                                        modifier = Modifier.weight(1f)
                                    )
                                    EditablePetInfoField(
                                        label = "Age",
                                        value = age?.toString() ?: "N/A",
                                        isEditing = false, // Age is calculated, not editable
                                        onEditClick = { /* Do nothing */ },
                                        onValueChange = {},
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                EditablePetInfoField(
                                    label = "Date of Birth",
                                    value = formattedDob,
                                    isEditing = currentlyEditing == "Date of Birth",
                                    onEditClick = { currentlyEditing = "Date of Birth" },
                                    onValueChange = { /* TODO: viewModel.updateDob(it) */ }
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    EditablePetInfoField(
                                        label = "Breed",
                                        value = pet.breed,
                                        isEditing = currentlyEditing == "Breed",
                                        onEditClick = { currentlyEditing = "Breed" },
                                        onValueChange = { /* TODO: viewModel.updateBreed(it) */ },
                                        modifier = Modifier.weight(1f)
                                    )
                                    EditablePetInfoField(
                                        label = "Weight (kg)",
                                        value = pet.weight.toString(),
                                        isEditing = currentlyEditing == "Weight (kg)",
                                        onEditClick = { currentlyEditing = "Weight (kg)" },
                                        onValueChange = { /* TODO: viewModel.updateWeight(it) */ },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                EditablePetInfoField(
                                    label = "Remarks",
                                    value = pet.remarks,
                                    isEditing = currentlyEditing == "Remarks",
                                    onEditClick = { currentlyEditing = "Remarks" },
                                    onValueChange = { /* TODO: viewModel.updateRemarks(it) */ }
                                )

                                // --- Remove Button ---
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .clickable { /* TODO: Show confirmation dialog */ },
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Remove", textDecoration = TextDecoration.Underline)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Cancel, contentDescription = "Remove Pet", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // --- Save Button ---
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                // When save is clicked, stop editing all fields
                                currentlyEditing = null
                                /* TODO: Call ViewModel to save all changes */
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CreamDark)
                        ) {
                            Text("Save", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditablePetInfoField(
    label: String,
    value: String?,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    Column(modifier = modifier) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CreamDark)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, color = CreamDark),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    readOnly = !isEditing // Text field is read-only unless isEditing is true
                )
            }
            // The pencil icon is now a button that enables editing for this field
            IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit $label", modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
        }
        Divider(color = Color.Gray, thickness = 1.dp)
    }
}
