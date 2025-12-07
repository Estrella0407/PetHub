package com.example.pethubself.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pethub.R


@Composable
fun AuthenticationButton (
    text: String,
    onClick: ()->Unit,
    color: ButtonColors,
    modifier: Modifier = Modifier
){
    Button(
        onClick = onClick,
        colors = color,
        shape = RoundedCornerShape(5.dp),
        modifier = modifier
    ){
        Text(text = text,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AuthenticationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
){
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {Text(label)},
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        leadingIcon = leadingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color(0xFFEDE3D1),
            unfocusedContainerColor = Color(0xFFEDE3D1)),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        )
    )
}

@Composable
fun AuthenticationSwitch(
    placeholder: String = "",
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                uncheckedTrackColor = Color(0xFFEDE3D1),
                checkedTrackColor = Color(0xFFA28970),
                uncheckedThumbColor = Color(0xFFA28970),
                checkedThumbColor = Color.White
            ),
            modifier = Modifier
                .scale(0.7f)
        )

        Spacer(modifier = Modifier.width(3.dp))

        Text(
            text = placeholder,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun AuthenticationImagesFooter(
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Image(
            painter = painterResource(R.drawable.daycare_nobg),
            contentDescription = "Daycare",
            modifier = Modifier.size(50.dp)
        )
        Image(
            painter = painterResource(R.drawable.boarding_nobg),
            contentDescription = "Boarding",
            modifier = Modifier.size(50.dp)
        )
        Image(
            painter = painterResource(R.drawable.grooming_nobg),
            contentDescription = "Grooming",
            modifier = Modifier.size(50.dp)
        )
        Image(
            painter = painterResource(R.drawable.training_nobg),
            contentDescription = "Training",
            modifier = Modifier.size(50.dp)
        )
        Image(
            painter = painterResource(R.drawable.walking_nobg),
            contentDescription = "Walking",
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
fun AuthenticationGoogleButton(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
)
{
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ){
        Spacer(modifier = Modifier.weight(1f)) // Takes up all remaining space
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Sign In with Google",
            fontSize = 19.sp
        )
        Spacer(modifier = Modifier.weight(1f)) // Takes up all remaining space

    }
}