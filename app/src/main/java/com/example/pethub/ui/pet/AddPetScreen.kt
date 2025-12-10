package com.example.pethub.ui.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.CreamLight
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    onPetAdded: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AddPetViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current

    var petName by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("") }
    var breed by rememberSaveable { mutableStateOf("") }
    var remarks by rememberSaveable { mutableStateOf("") }
    var dob by rememberSaveable { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val ownerDetails by viewModel.ownerDetails.collectAsState()

    //Drop down list
    var sex by remember { mutableStateOf("Select") }
    val sexOptions = listOf("Male", "Female")
    var expandedSex by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf("Select") }
    val weightOptions = listOf("1-5kg", "6-10kg", "10-20kg", ">20kg")
    var expandedWeight by remember { mutableStateOf(false) }

    // Define custom colors for TextFields
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = CreamLight,
        unfocusedContainerColor = CreamLight,
        disabledContainerColor = CreamLight,
        focusedBorderColor = CreamDark,
        unfocusedBorderColor = CreamDark,
        focusedLabelColor = CreamDark,
        unfocusedLabelColor = CreamDark
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            dob = sdf.format(Date(it))
                        }
                        showDatePicker = false
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Pet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                // Set TopAppBar colors
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamLight,
                    titleContentColor = CreamDark,
                    navigationIconContentColor = CreamDark
                )
            )
        }
    ) { padding ->
        // Use a Box to apply the background color to the entire screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    // Make the column scrollable
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //First row
                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Pet's Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors = textFieldColors // Apply custom colors
                )

                //Second Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text("Type") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Right) }
                        ),
                        colors = textFieldColors // Apply custom colors
                    )
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Breed") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = textFieldColors // Apply custom colors
                    )
                }

                // Third Row
                OutlinedTextField(
                    value = dob,
                    onValueChange = {},
                    label = { Text("    DD / MM / YYYY   ") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Select Date",
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    },
                    colors = textFieldColors // Apply custom colors
                )

                // Fourth Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedSex,
                        onExpandedChange = { expandedSex = !expandedSex },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = sex,
                            onValueChange = { },
                            label = { Text("Sex") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSex) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = textFieldColors // Apply custom colors
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSex,
                            onDismissRequest = { expandedSex = false }
                        ) {
                            sexOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        sex = option
                                        expandedSex = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedWeight,
                        onExpandedChange = { expandedWeight = !expandedWeight },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { },
                            label = { Text("Weight") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWeight) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = textFieldColors // Apply custom colors
                        )
                        ExposedDropdownMenu(
                            expanded = expandedWeight,
                            onDismissRequest = { expandedWeight = false }
                        ) {
                            weightOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        weight = option
                                        expandedWeight = false
                                    }
                                )
                            }
                        }
                    }
                }

                //Fifth Row
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = textFieldColors // Apply custom colors
                )

                // Sixth Row
                OutlinedTextField(
                    value = ownerDetails?.custName ?: "Loading...",
                    onValueChange = {},
                    label = { Text("Owner's Name") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors // Apply custom colors
                )

                // Seventh Row
                OutlinedTextField(
                    value = ownerDetails?.custEmail ?: "Loading...",
                    onValueChange = {},
                    label = { Text("Owner's Email") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors // Apply custom colors
                )

                // Last Row
                OutlinedTextField(
                    value = ownerDetails?.custPhone ?: "Loading...",
                    onValueChange = {},
                    label = { Text("Owner's Contact") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors // Apply custom colors
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onPetAdded()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    // Style the button
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CreamDark,
                        contentColor = Color.White
                    )
                ) {
                    Text("Add Pet")
                }

                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}
