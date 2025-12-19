package com.example.pethub.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToAdminHome: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onReturnClick: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Handle navigation logic
    LaunchedEffect(uiState.isSuccess, uiState.isLoginSuccessful, uiState.loggedInUserRole, uiState.isNewGoogleUser) {
        if (uiState.isSuccess) {
            onNavigateToCompleteProfile()
        }

        if (uiState.isLoginSuccessful) {
            when (uiState.loggedInUserRole) {
                "admin" -> onNavigateToAdminHome()
                else -> onLoginSuccess()
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
            text = "Register",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // Email Field
        AuthenticationTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            label ="Email",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.profile_icon),
                    contentDescription = "Email Icon",
                    modifier = Modifier.size(40.dp)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Field
        AuthenticationTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.password_key_icon),
                    contentDescription = "Password Icon",
                    modifier = Modifier.size(40.dp)
                )
            },            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (uiState.isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm Password Field
        AuthenticationTextField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = "Confirm Password",
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.password_key_icon),
                    contentDescription = "Password Icon",
                    modifier = Modifier.size(40.dp)
                )
            },
            visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.isConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (uiState.isConfirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { 
                    focusManager.clearFocus()
                    viewModel.register()
                }
            )
        )

        // Error Message
        if (!uiState.errorMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Already have an account?
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Already have an account? ", color = Color.Gray)
            Text(
                text = "Login",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Register Button
        Button(
            onClick = viewModel::register,
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFAC7F5E)),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(10.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = "Register", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenContent(
    uiState: RegisterUiState,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current

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
            text = "Register",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // Email Field
        AuthenticationTextField(
            value = uiState.email,
            onValueChange = {},//Do},
            label = "Email",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.profile_icon),
                    contentDescription = "Email Icon",
                    modifier = Modifier.size(40.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Field
        AuthenticationTextField(
            value = uiState.password,
            onValueChange = {},// DO
            label = "Password",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.password_key_icon),
                    contentDescription = "Password Icon",
                    modifier = Modifier.size(40.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
//            visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//            trailingIcon = {
//                val image = if (uiState.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
//                IconButton(onClick = viewModel::togglePasswordVisibility) {
//                    Icon(imageVector = image, contentDescription = if (uiState.passwordVisible) "Hide password" else "Show password")
//                }
//            }, DO
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm Password Field
        AuthenticationTextField(
            value = uiState.confirmPassword,
            onValueChange = {},// DO
            label = "Confirm Password",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.password_key_icon),
                    contentDescription = "Password Icon",
                    modifier = Modifier.size(40.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
//            visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//            trailingIcon = {
//                val image = if (uiState.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
//                IconButton(onClick = viewModel::togglePasswordVisibility) {
//                    Icon(imageVector = image, contentDescription = if (uiState.passwordVisible) "Hide password" else "Show password")
//                }
//            }, DO
            modifier = Modifier.fillMaxWidth()
        )

        // Error Message
        if (!uiState.errorMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Login Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Already have an account? ",
                color = Color.Gray,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onNavigateToLogin() }
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        // Register Button
        Button(
            onClick = {},
            modifier = Modifier
                .width(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFAC7F5E)
            ),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(10.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Register",
                    fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Login with Google Button
        AuthenticationGoogleButton(
            icon =
                {Image(
                    painter = painterResource(R.drawable.google_nobg),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(20.dp)
                )},
            onGoogleClick = {}//Login}
        )

        Spacer(modifier = Modifier.weight(1f))
        ServiceIconsRow()

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
fun RegisterScreenPreview(){
    RegisterScreenContent(
        RegisterUiState(
     email= "",
     password = "",
     confirmPassword = "",
     isPasswordVisible= false,
     isConfirmPasswordVisible= false,
     isLoading= false,
     isSuccess= false,
     errorMessage= null,
     successMessage= null
    ),
        {},
        {})
}
