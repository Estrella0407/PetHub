package com.example.pethub.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.CreamFair
import com.example.pethub.ui.theme.VibrantBrown
import com.example.pethub.ui.theme.getTextFieldColors

@Composable
fun CompleteProfileScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onProfileCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Local state for address fields
    var houseNo by remember { mutableStateOf("") }
    var streetName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }

    // Update ViewModel when address changes
    val updateAddress = {
        viewModel.onAddressUpdated(houseNo, streetName, city, postcode, state)
    }

    LaunchedEffect(uiState.completed) {
        if (uiState.completed) {
            onProfileCompleted()
        }
    }

    CompleteProfileContent(
        uiState = uiState,
        houseNo = houseNo,
        streetName = streetName,
        city = city,
        postcode = postcode,
        state = state,
        onUsernameChange = viewModel::onUsernameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onHouseNoChange = { houseNo = it; updateAddress() },
        onStreetNameChange = { streetName = it; updateAddress() },
        onCityChange = { city = it; updateAddress() },
        onPostcodeChange = { postcode = it; updateAddress() },
        onStateChange = { state = it; updateAddress() },
        onCompleteClick = viewModel::completeProfile
    )
}

@Composable
fun CompleteProfileContent(
    uiState: RegisterUiState,
    houseNo: String,
    streetName: String,
    city: String,
    postcode: String,
    state: String,
    onUsernameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onHouseNoChange: (String) -> Unit,
    onStreetNameChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onPostcodeChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onCompleteClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(24.dp),
    ) {
        item {
            ProfileHeader()
        }

        item {
            BasicInfoSection(
                username = uiState.username,
                phone = uiState.phone,
                onUsernameChange = onUsernameChange,
                onPhoneChange = onPhoneChange
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            AddressSection(
                houseNo = houseNo,
                streetName = streetName,
                city = city,
                postcode = postcode,
                state = state,
                onHouseNoChange = onHouseNoChange,
                onStreetNameChange = onStreetNameChange,
                onCityChange = onCityChange,
                onPostcodeChange = onPostcodeChange,
                onStateChange = onStateChange
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            CompleteButton(onClick = onCompleteClick)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Image(
                painter = painterResource(R.drawable.pethub_rvbg),
                contentDescription = "PetHub Logo",
                modifier = Modifier.width(100.dp)
            )
        }
        Text(
            text = "Finish your profile!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
    }
}

@Composable
fun BasicInfoSection(
    username: String,
    phone: String,
    onUsernameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileTextField(
            label = "Username",
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth(0.95f)
        )

        ProfileTextField(
            label = "Phone Number",
            value = phone,
            onValueChange = onPhoneChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(0.95f)
        )
    }
}

@Composable
fun AddressSection(
    houseNo: String,
    streetName: String,
    city: String,
    postcode: String,
    state: String,
    onHouseNoChange: (String) -> Unit,
    onStreetNameChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onPostcodeChange: (String) -> Unit,
    onStateChange: (String) -> Unit
) {
    Text(
        text = "Address",
        fontSize = 24.sp,
        color = CreamDark,
        fontWeight = FontWeight.Bold
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileTextField(
            label = "Unit / House No",
            value = houseNo,
            onValueChange = onHouseNoChange,
            modifier = Modifier.fillMaxWidth(0.6f)
        )

        ProfileTextField(
            label = "Street / Building Name",
            value = streetName,
            onValueChange = onStreetNameChange,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(0.6f)) {
                ProfileTextField(
                    label = "City",
                    value = city,
                    onValueChange = onCityChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(0.4f)) {
                ProfileTextField(
                    label = "Postcode",
                    value = postcode,
                    onValueChange = onPostcodeChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        ProfileTextField(
            label = "State",
            value = state,
            onValueChange = onStateChange,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}

@Composable
fun CompleteButton(onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = VibrantBrown,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Complete",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))

        // Using BasicTextField for precise height control
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                color = Color.Black
            ),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            modifier = modifier
                .height(40.dp)
                .background(
                    color = CreamFair,
                    shape = RoundedCornerShape(5.dp)
                )
                .border(
                    width = 2.dp,
                    color = CreamDark,
                    shape = RoundedCornerShape(5.dp)
                ),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = androidx.compose.ui.Alignment.CenterStart,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    innerTextField()
                }
            }
        )
    }
}


@Preview(
    showBackground = true,
    device = Devices.PIXEL_4,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun CompleteProfileScreenPreview() {
    // Mock UI State
    val mockUiState = RegisterUiState(
        username = "User123",
        email = "test@example.com",
        password = "password",
        confirmPassword = "password"
    )

    CompleteProfileContent(
        uiState = mockUiState,
        houseNo = "12A",
        streetName = "Pet Street",
        city = "Pet City",
        postcode = "12345",
        state = "Selangor",
        onUsernameChange = {},
        onPhoneChange = {},
        onHouseNoChange = {},
        onStreetNameChange = {},
        onCityChange = {},
        onPostcodeChange = {},
        onStateChange = {},
        onCompleteClick = {}
    )
}
