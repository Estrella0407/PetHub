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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.data.model.Customer
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.CreamLight
import com.example.pethub.ui.theme.getTextFieldColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddPetScreen(
    onPetAdded: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AddPetViewModel = hiltViewModel()
) {
    val ownerDetails by viewModel.ownerDetails.collectAsState()

    // Form State
    var petName by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("") }
    var breed by rememberSaveable { mutableStateOf("") }
    var remarks by rememberSaveable { mutableStateOf("") }
    var dob by rememberSaveable { mutableStateOf("") }
    var sex by rememberSaveable { mutableStateOf("Select") }
    var weight by rememberSaveable { mutableStateOf("Select") }

    AddPetContent(
        petName = petName,
        onPetNameChange = { petName = it },
        type = type,
        onTypeChange = { type = it },
        breed = breed,
        onBreedChange = { breed = it },
        remarks = remarks,
        onRemarksChange = { remarks = it },
        dob = dob,
        onDobChange = { dob = it },
        sex = sex,
        onSexChange = { sex = it },
        weight = weight,
        onWeightChange = { weight = it },
        ownerDetails = ownerDetails,
        onNavigateBack = onNavigateBack,
        onSaveClick = {
             viewModel.savePet(
                 petName = petName,
                 type = type,
                 breed = breed,
                 remarks = remarks,
                 dob = dob,
                 sex = sex,
                 weight = weight,
                 onSuccess = onPetAdded
             )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetContent(
    petName: String,
    onPetNameChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    breed: String,
    onBreedChange: (String) -> Unit,
    remarks: String,
    onRemarksChange: (String) -> Unit,
    dob: String,
    onDobChange: (String) -> Unit,
    sex: String,
    onSexChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    ownerDetails: Customer?,
    onNavigateBack: () -> Unit,
    onSaveClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Pet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamLight,
                    titleContentColor = CreamDark,
                    navigationIconContentColor = CreamDark
                )
            )
        }
    ) { padding ->
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                PetInfoSection(
                    petName = petName,
                    onPetNameChange = onPetNameChange,
                    type = type,
                    onTypeChange = onTypeChange,
                    breed = breed,
                    onBreedChange = onBreedChange
                )

                PhysicalAttributesSection(
                    dob = dob,
                    onDobChange = onDobChange,
                    sex = sex,
                    onSexChange = onSexChange,
                    weight = weight,
                    onWeightChange = onWeightChange
                )

                PetTextField(
                    value = remarks,
                    onValueChange = onRemarksChange,
                    label = "Remarks",
                    imeAction = ImeAction.Done,
                    modifier = Modifier.fillMaxWidth()
                )

                OwnerInfoSection(ownerDetails)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth(),
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

@Composable
fun PetInfoSection(
    petName: String,
    onPetNameChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    breed: String,
    onBreedChange: (String) -> Unit
) {
    PetTextField(
        value = petName,
        onValueChange = onPetNameChange,
        label = "Pet's Name",
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PetTextField(
            value = type,
            onValueChange = onTypeChange,
            label = "Type",
            modifier = Modifier.weight(1f)
        )
        PetTextField(
            value = breed,
            onValueChange = onBreedChange,
            label = "Breed",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PhysicalAttributesSection(
    dob: String,
    onDobChange: (String) -> Unit,
    sex: String,
    onSexChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit
) {
    PetDatePickerField(
        date = dob,
        onDateSelected = onDobChange
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PetDropdownField(
            value = sex,
            options = listOf("Male", "Female"),
            label = "Sex",
            onOptionSelected = onSexChange,
            modifier = Modifier.weight(1f)
        )
        PetDropdownField(
            value = weight,
            options = listOf("1-5kg", "6-10kg", "10-20kg", ">20kg"),
            label = "Weight",
            onOptionSelected = onWeightChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun OwnerInfoSection(ownerDetails: Customer?) {
    PetTextField(
        value = ownerDetails?.custName ?: "Loading...",
        onValueChange = {},
        label = "Owner's Name",
        readOnly = true,
        modifier = Modifier.fillMaxWidth()
    )
    PetTextField(
        value = ownerDetails?.custEmail ?: "Loading...",
        onValueChange = {},
        label = "Owner's Email",
        readOnly = true,
        modifier = Modifier.fillMaxWidth()
    )
    PetTextField(
        value = ownerDetails?.custPhone ?: "Loading...",
        onValueChange = {},
        label = "Owner's Contact",
        readOnly = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource? = null
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(imeAction = if (readOnly) ImeAction.None else imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
            onDone = { focusManager.clearFocus() }
        ),
        colors = getTextFieldColors(),
        trailingIcon = trailingIcon,
        interactionSource = interactionSource ?: remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDropdownField(
    value: String,
    options: List<String>,
    label: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        PetTextField(
            value = value,
            onValueChange = {},
            label = label,
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDatePickerField(
    date: String,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val focusManager = LocalFocusManager.current

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            onDateSelected(sdf.format(Date(it)))
                        }
                        showDatePicker = false
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box {
        PetTextField(
            value = date,
            onValueChange = {},
            label = "DD / MM / YYYY",
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Select Date",
                    modifier = Modifier.clickable { showDatePicker = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }, 
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDatePicker = true }
        )
    }
}
