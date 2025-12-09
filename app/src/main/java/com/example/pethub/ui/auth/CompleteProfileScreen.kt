package com.example.pethub.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun CompleteProfileScreen (
    viewModel: RegisterViewModel = hiltViewModel()
){

    val uiState by viewModel.uiState.collectAsState()
    val textFieldColour = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFEDE4D1),
        unfocusedContainerColor = Color(0xFFEDE4D1),

        focusedBorderColor = Color(0xFFA28970),
        unfocusedBorderColor = Color(0xFFA28970),

        focusedLabelColor = Color(0xFFA28970),
        unfocusedLabelColor = Color(0xFFA28970)
    )
    var houseNo by remember { mutableStateOf("") }
    var streetName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()                 // Make it full screen
            .background(Color(0xFFF8F7E9))       // Apply background first
            .padding(24.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ){
            Image(
                painter = painterResource(R.drawable.pethub_rvbg),
                contentDescription = "PetHub Logo",
                modifier = Modifier.width(100.dp)
            )
        }

        //Finish your profile!
        Text(
            text = "Finish your profile!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom=10.dp)
        )

        Text(
            text = "Username",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = uiState.username,
            onValueChange = viewModel::onUsernameChange,
            colors = textFieldColour,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(50.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFA28970)
                )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Phone Number",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = uiState.phone,
            onValueChange = viewModel::onPhoneChange,
            colors = textFieldColour,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(50.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFA28970)
                )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Address",
            fontSize = 24.sp,
            color = Color(0xFFA28970),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Unit / House No",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = houseNo,
            onValueChange = {
                houseNo = it
                viewModel.onAddressUpdated(houseNo,streetName,city,postcode,state)},
            colors = textFieldColour,
            singleLine=true,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFA28970)
                )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Street / Building Name",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = streetName,
            onValueChange = {
                streetName = it
                viewModel.onAddressUpdated(houseNo,streetName,city,postcode,state)},
            colors = textFieldColour,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFA28970)
                )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()){
            Column() {
                Text(
                    text = "City",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                OutlinedTextField(
                    value = city,
                    onValueChange = {
                        city = it
                        viewModel.onAddressUpdated(houseNo,streetName,city,postcode,state)},
                    colors = textFieldColour,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(50.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFA28970)
                        )
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column() {
                Text(
                    text = "Postcode",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                OutlinedTextField(
                    value = postcode,
                    onValueChange = {
                        postcode = it
                        viewModel.onAddressUpdated(houseNo,streetName,city,postcode,state)},
                    colors = textFieldColour,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(50.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFA28970)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "State",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = state,
            onValueChange = {
                state = it
                viewModel.onAddressUpdated(houseNo,streetName,city,postcode,state)},
            colors = textFieldColour,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFA28970)
                )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFAC7F5E),   // Background color
                    contentColor = Color.White      // Text color
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
}

@Composable
fun CompleteProfileScreenContent (
    uiState: RegisterUiState
){

    val textFieldColour = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFEDE4D1),
        unfocusedContainerColor = Color(0xFFEDE4D1),

        focusedBorderColor = Color(0xFFA28970),
        unfocusedBorderColor = Color(0xFFA28970),

        focusedLabelColor = Color(0xFFA28970),
        unfocusedLabelColor = Color(0xFFA28970)
    )
    var houseNo = ""
    var streetName = ""
    var city = ""
    var postcode = ""
    var state = ""


    Column(
        modifier = Modifier
            .fillMaxSize()                 // Make it full screen
            .background(Color(0xFFF8F7E9))       // Apply background first
            .padding(24.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ){
            Image(
                painter = painterResource(R.drawable.pethub_rvbg),
                contentDescription = "PetHub Logo",
                modifier = Modifier.width(100.dp)
            )
        }

        //Finish your profile!
        Text(
            text = "Finish your profile!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
            .padding(bottom=10.dp)
        )

        Text(
            text = "Username",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = uiState.username,
            onValueChange = { },
            colors = textFieldColour,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 12.sp
            ),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(40.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFA28970)
                )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Phone Number",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = uiState.phone,
            onValueChange = { },
            colors = textFieldColour,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(40.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFA28970)
                )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Address",
            fontSize = 24.sp,
            color = Color(0xFFA28970),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = "Unit / House No",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = houseNo,
            onValueChange = { },
            colors = textFieldColour,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(40.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFA28970)
                )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Street / Building Name",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = streetName,
            onValueChange = { },
            colors = textFieldColour,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(40.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFA28970)
                )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()){
            Column() {
                Text(
                    text = "City",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                OutlinedTextField(
                    value = city,
                    onValueChange = { },
                    colors = textFieldColour,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(40.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFA28970)
                        )
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column() {
                Text(
                    text = "Postcode",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                OutlinedTextField(
                    value = postcode,
                    onValueChange = { },
                    colors = textFieldColour,
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(40.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFA28970)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "State",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = state,
            onValueChange = { },
            colors = textFieldColour,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(40.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFA28970)
                )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFAC7F5E),   // Background color
                    contentColor = Color.White      // Text color
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
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_4,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun CompleteProfileScreenPreview(){
    CompleteProfileScreenContent(
        RegisterUiState(
            username = "",
            email = "",
            password = "",
            confirmPassword = ""
        )
    )
}
