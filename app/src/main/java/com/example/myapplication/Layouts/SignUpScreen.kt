package com.example.myapplication.Layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.ChatViewModel
import com.example.myapplication.CommonnProgressBar
import com.example.myapplication.DestinationScreen
import com.example.myapplication.R
import com.example.myapplication.checkSignedIn
import com.example.myapplication.navigateTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, viewModel: ChatViewModel) {

    checkSignedIn(viewModel = viewModel , navController = navController )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
                .verticalScroll(
                    rememberScrollState()
                ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nameState = remember {
                mutableStateOf(TextFieldValue())
            }
            val numberState = remember {
                mutableStateOf(TextFieldValue())
            }
            val emailState = remember {
                mutableStateOf(TextFieldValue())
            }
            val passwordState = remember {
                mutableStateOf(TextFieldValue())
            }
            val focus = LocalFocusManager.current
            Image(
                painter =
                painterResource(id = R.drawable.login),
                contentDescription = null,
                modifier = Modifier
                    .width(200.dp)
                    .padding(top = 16.dp)
                    .padding(8.dp)
            )
            Text(
                text = "Sign UP",
                fontFamily = FontFamily.SansSerif,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
            OutlinedTextField(
                value = nameState.value,
                onValueChange = {
                    nameState.value = it
                }, label = { Text(text = "Name") },
                modifier = Modifier.padding(5.dp)
            )
            OutlinedTextField(
                value = numberState.value,
                onValueChange = {
                    numberState.value = it
                }, label = { Text(text = "Number") },
                modifier = Modifier.padding(5.dp)
            )
            OutlinedTextField(
                value = emailState.value,
                onValueChange = {
                    emailState.value = it
                }, label = { Text(text = "EMail") },
                modifier = Modifier.padding(5.dp)
            )
            OutlinedTextField(
                value = passwordState.value,
                onValueChange = {
                    passwordState.value = it
                }, label = { Text(text = "Password") },
                modifier = Modifier.padding(5.dp)
            )
            Button(
                onClick = {
                    viewModel.signup(
                        nameState.value.text,
                        numberState.value.text,
                        emailState.value.text,
                        passwordState.value.text
                    )
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "SIGN UP")
            }
            Text(text = "Already I User ? Go To Login ->",
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navigateTo(navController, DestinationScreen.LogIn.route)
                    })
        }
    }
    if (viewModel.inProcess.value) {
        CommonnProgressBar()
    }
}