package com.example.pethub.ui.pet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import com.example.pethub.ui.pet.PetProfileViewModel
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.DarkBrown
import com.example.pethub.ui.theme.CreamLight
import com.example.pethub.ui.theme.DarkBrown
import com.example.pethub.ui.theme.DarkBrown
import com.example.pethub.ui.theme.MutedBrown
import com.example.pethub.ui.theme.VibrantBrown
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.pethub.util.calculateAge
// 1. ADD THE CORRECT JAVA IMPORTS
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
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Observe one-time events
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PetProfileEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is PetProfileEvent.PetDeleted -> {
                    Toast.makeText(context, "Pet removed successfully", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            }
        }
    }

    // 👇 NEW: Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onImageSelected(uri)
            }
        }
    )
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Remove Pet") },
            text = { Text("Are you sure you want to remove this pet? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePet()
                    }
                ) {
                    Text("Remove", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = CreamBackground,
            titleContentColor = DarkBrown,
            textContentColor = DarkBrown
        )
    }

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text("Pet Profile", fontWeight = FontWeight.Bold, color = DarkBrown) },
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
                PetProfileContent(
                    modifier = Modifier.padding(padding),
                    state = state, // Pass the whole state to access upload progress
                    onImageClick = {
                        // Launch photo picker
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onPetChange = viewModel::onPetDataChanged,
                    onSaveClick = viewModel::savePetDetails,
                    onRemoveClick = { showDeleteDialog = true }
                )
            }
        }
    }
}

@Composable
fun PetProfileContent(
    modifier: Modifier = Modifier,
    state: PetProfileUiState.Success,
    onImageClick: () -> Unit, // Callback for image click
    onPetChange: (Pet) -> Unit,
    onSaveClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val pet = state.pet
    val age = calculateAge(pet.dateOfBirth?.time)
    val formattedDob = pet.dateOfBirth?.let {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        sdf.format(it)
    } ?: "Not set"

    // Local state for weight to prevent "fighting" the auto-formatting (e.g. typing "5." becoming "5.0")
    var weightInput by remember(pet.petId) { mutableStateOf(pet.weight?.toString() ?: "") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // --- Pet Image and Name ---
        item {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = pet.imageUrl,
                    contentDescription = pet.petName,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, DarkBrown, CircleShape)
                        .clickable(onClick = onImageClick),
                    contentScale = ContentScale.Crop,
                    fallback = rememberVectorPainter(Icons.Default.Pets) // Fallback icon
                )

                // Show Progress Indicator if uploading
                if (state.isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(120.dp),
                        color = DarkBrown,
                        strokeWidth = 4.dp
                    )
                }

                // Edit Icon overlay
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Image",
                    tint = DarkBrown,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(24.dp)
                        .background(Color.White, CircleShape)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = pet.petName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBrown
            )
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
                        // Gender Dropdown
                        GenderDropdownField(
                            value = pet.sex,
                            onValueChange = { onPetChange(pet.copy(sex = it)) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        EditablePetInfoField(
                            label = "Age",
                            value = age?.toString() ?: "N/A",
                            modifier = Modifier.weight(1f),
                            readOnly = true // Age is derived
                        )
                    }
                    EditablePetInfoField(
                        label = "Date of Birth",
                        value = formattedDob,
                        readOnly = true // Date editing requires DatePicker
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        EditablePetInfoField(
                            label = "Breed",
                            value = pet.breed,
                            modifier = Modifier.weight(1f),
                            onValueChange = { onPetChange(pet.copy(breed = it)) }
                        )
                        
                        // Weight Field with special handling
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Weight (kg)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBrown)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BasicTextField(
                                    value = weightInput,
                                    onValueChange = { newValue ->
                                        // Allow only digits and one dot
                                        if (newValue.all { it.isDigit() || it == '.' } && newValue.count { it == '.' } <= 1) {
                                            weightInput = newValue
                                            // Only update parent if it parses somewhat validly or is empty
                                            val doubleVal = newValue.toDoubleOrNull()
                                            onPetChange(pet.copy(weight = doubleVal))
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, color = DarkBrown),
                                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal, imeAction = ImeAction.Next)
                                )
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = Color.Gray)
                            }
                            Divider(color = Color.Gray, thickness = 1.dp)
                        }
                    }
                    EditablePetInfoField(
                        label = "Remarks",
                        value = pet.remarks ?: "",
                        onValueChange = { onPetChange(pet.copy(remarks = it)) }
                    )

                    // --- Remove Button ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable { onRemoveClick() },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remove",
                            textDecoration = TextDecoration.Underline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Cancel, contentDescription = "Remove Pet", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // --- Recommended Services ---
        if (state.recommendedServices.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Recommended Services",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBrown
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal list of services
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.recommendedServices.forEach { service ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamLight)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = service.serviceName,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBrown,
                                    maxLines = 1
                                )
                                Text(
                                    text = "RM${service.price}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Save Button ---
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBrown)
            ) {
                Text("Save", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun GenderDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Male", "Female")

    Column(modifier = modifier) {
        Text(text = "Gender", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBrown)
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = value.ifBlank { "Select Gender" },
                    fontSize = 18.sp,
                    color = DarkBrown,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = DarkBrown
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selection ->
                    DropdownMenuItem(
                        text = { Text(selection) },
                        onClick = {
                            onValueChange(selection)
                            expanded = false
                        }
                    )
                }
            }
        }
        Divider(color = Color.Gray, thickness = 1.dp)
    }
}

@Composable
fun EditablePetInfoField(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    onValueChange: (String) -> Unit = {}
) {
    Column(modifier = modifier) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBrown)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Null check outside to avoid null in BasicTextField value
            val displayValue = value ?: ""
            
            BasicTextField(
                value = displayValue,
                onValueChange = if (readOnly) { {} } else onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp), // Add touch target padding
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, color = if(readOnly) Color.Gray else DarkBrown),
                enabled = !readOnly,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            
            if (!readOnly) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
            }
        }
        Divider(color = Color.Gray, thickness = 1.dp)
    }
}
