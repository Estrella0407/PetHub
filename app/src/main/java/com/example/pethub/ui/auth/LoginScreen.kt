package com.example.pethub.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.pethub.R
import com.example.pethubself.ui.components.AuthenticationSwitch
import com.example.pethubself.ui.components.AuthenticationTextField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            onLoginSuccess()
            viewModel.onLoginHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.returnbutton),
                contentDescription = "Return Button",
                modifier = Modifier
                    .size(30.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.logo_nobg),
                contentDescription = "Logo",
                modifier = Modifier
                    .padding(start = 55.dp)
                    .size(150.dp)
            )
        }

        Text(
            text = "Login",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        AuthenticationTextField(
            value = uiState.email,
            onValueChange = viewModel::updateEmail,
            label = "Email",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.profile_icon),
                    contentDescription = "Email Icon",
                    modifier = Modifier.size(40.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthenticationTextField(
            value = uiState.password,
            onValueChange = viewModel::updatePassword,
            label = "Password",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.password_key_icon),
                    contentDescription = "Password Icon",
                    modifier = Modifier.size(40.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (uiState.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(imageVector = image, contentDescription = if (uiState.passwordVisible) "Hide password" else "Show password")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

//        OutlinedTextField(
//            value = uiState.password,
//            onValueChange = viewModel::updatePassword,
//            label = { Text("Password") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true,
//            visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//            trailingIcon = {
//                val image = if (uiState.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
//                IconButton(onClick = viewModel::togglePasswordVisibility) {
//                    Icon(imageVector = image, contentDescription = if (uiState.passwordVisible) "Hide password" else "Show password")
//                }
//            }
//        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        )
        {
            AuthenticationSwitch(
                isChecked = uiState.rememberMe,
                onCheckedChange = {viewModel.onRememberMeChanged(it)},
                placeholder = "Remember me?"
            )
            TextButton(
                onClick = { onNavigateToRegister()},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Register Account",
                    textDecoration = TextDecoration.Underline,
                    textAlign = TextAlign.End,
                    modifier= Modifier.fillMaxWidth()
                )
            }
        }

        if (uiState.errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFAC7F5E)
            ),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text(text = "Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { viewModel.loginAsGuest() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Continue as Guest",
                textDecoration = TextDecoration.Underline
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(text = "Don't have an account? ", color = Color.Gray)
//            Text(
//                text = "Register",
//                color = MaterialTheme.colorScheme.primary,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.clickable { onNavigateToRegister() }
//            )
//        }
    }
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7E9)),
        color = Color(0xFFF8F7E9)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.returnbutton),
                    contentDescription = "Return Button",
                    modifier = Modifier
                        .size(30.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.logo_nobg),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .padding(start = 55.dp)
                        .size(150.dp)
                )
            }

            Text(
                text = "Login",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black//MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            AuthenticationTextField(
                value = uiState.email,
                onValueChange = {},
                label = "Email",
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.profile_icon),
                        contentDescription = "Email Icon",
                        modifier = Modifier.size(40.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

//        OutlinedTextField(
//            value = uiState.email,
//            onValueChange = viewModel::updateEmail,
//            label = { Text("Email") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true,
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
//        )
            Spacer(modifier = Modifier.height(16.dp))

            AuthenticationTextField(
                value = uiState.password,
                onValueChange = { },
                label = "Password",
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.password_key_icon),
                        contentDescription = "Email Icon",
                        modifier = Modifier.size(40.dp)
                    )
                },
                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image =
                        if (uiState.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = image,
                            contentDescription = if (uiState.passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

//            OutlinedTextField(
//                value = uiState.password,
//                onValueChange = { },
//                label = { Text("Password") },
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                trailingIcon = {
//                    val image =
//                        if (uiState.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
//                    IconButton(onClick = {}) {
//                        Icon(
//                            imageVector = image,
//                            contentDescription = if (uiState.passwordVisible) "Hide password" else "Show password"
//                        )
//                    }
//                }
//            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            )
            {
                AuthenticationSwitch(
                    isChecked = uiState.rememberMe,
                    onCheckedChange = {  },
                    placeholder = "Remember me?"
                )
                TextButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Register Account",
                        textDecoration = TextDecoration.Underline,
                        textAlign = TextAlign.End,
                        modifier= Modifier.fillMaxWidth()
                    )
                }
            }

            if (uiState.errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .width(200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFAC7F5E)
                ),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(text = "Login")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Continue as Guest",
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Don't have an account? ", color = Color.Gray)
                Text(
                    text = "Register",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
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
fun LoginScreenPreview(){
    LoginScreenContent(
        LoginUiState(
            email = "preview@example.com",
            password = "",
            passwordVisible = false,
            isLoading = false,
            isLoginSuccessful = false,
            errorMessage = "Example error"
        ),
        {},
        {}
    )
}
