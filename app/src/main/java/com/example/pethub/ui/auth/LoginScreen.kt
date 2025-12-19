package com.example.pethub.ui.auth

//import androidx.hilt.navigation.compose.hiltViewModel
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.ui.components.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onReturnClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToAdminHome: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val credentialManager = remember {
        CredentialManager.create(context)
    }

    // Check the role inside LaunchedEffect
    LaunchedEffect(uiState.isLoginSuccessful, uiState.loggedInUserRole, uiState.isNewGoogleUser) {
        if (uiState.isLoginSuccessful) {

            // Logic to determine destination
            when (uiState.loggedInUserRole) {
                "admin" -> {
                    onNavigateToAdminHome()
                }

                "customer" -> {
                    onLoginSuccess()
                }

                "guest" -> {
                    onLoginSuccess()
                }

                else -> {
                    onLoginSuccess() // Fallback
                }
            }

            viewModel.onLoginHandled()
        }
        
        if (uiState.isNewGoogleUser) {
            onNavigateToCompleteProfile()
            viewModel.onNewUserHandled()
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_nobg),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp)
            )
        }

        Text(
            text = "Login",
            fontSize = 32.sp,
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

        // Password Field
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

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        )
        {
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

        // Error message
        if (uiState.errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(2f))

        // Login Button
        Button(
            onClick = viewModel::login,
            modifier = Modifier
                .width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFAC7F5E)
            ),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(10.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text(
                    text = "Login",
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        val scope = rememberCoroutineScope()

        // Login with Google Button
        AuthenticationGoogleButton(
            icon =
                {Image(
                    painter = painterResource(R.drawable.google_nobg),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(20.dp)
                )},
            onGoogleClick = {scope.launch {
                signInWithGoogle(
                    context,
                    webClientId = context.getString(R.string.default_web_client_id),
                    onTokenReceived = { token ->
                        firebaseSignInWithGoogle(
                            token,
                            onSuccess = { 
                                // Instead of direct success, we check the user status
                                viewModel.checkGoogleUserStatus() 
                            },
                            onError = { 
                                // Handle error
                            }
                        )
                    }
                )
            }}
        )

        Spacer(modifier = Modifier.weight(1f))
        ServiceIconsRow()

    }
}

@Composable // For viewing Preview
fun LoginScreenContent(
    uiState: LoginUiState,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToAdmin: () -> Unit // Remove after proper navigation to admin is done
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_nobg),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            Text(
                text = "Login",
                fontSize = 32.sp,
                color = Color.Black//MaterialTheme.colorScheme.primary
            )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { onNavigateToAdmin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Login as Admin",
                textDecoration = TextDecoration.Underline
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            )
            {
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

            // Error Message
            if (uiState.errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Login Button
            Button(
                onClick = { },
                modifier = Modifier
                    .width(200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFAC7F5E)
                ),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(10.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = "Login",
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            AuthenticationGoogleButton(
                icon =
                    {Image(
                        painter = painterResource(R.drawable.google_nobg),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(20.dp)
                    )},
                onGoogleClick = {}
            )

            Spacer(modifier = Modifier.weight(1f))
            ServiceIconsRow()

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
        {},
        {}
    )
}
