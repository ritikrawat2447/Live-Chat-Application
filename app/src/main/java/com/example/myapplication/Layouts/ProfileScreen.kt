package com.example.myapplication.Layouts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ChatViewModel
import com.example.myapplication.CommonImage
import com.example.myapplication.CommonnProgressBar
import com.example.myapplication.DestinationScreen
import com.example.myapplication.commonDivider
import com.example.myapplication.navigateTo

@Composable
fun ProfileScreen(navController: NavController, viewModel: ChatViewModel) {

    val inPorcess = viewModel.inProcess.value
    if (inPorcess) {
        CommonnProgressBar()
    } else {

        val userData = viewModel.userData.value
        var name by rememberSaveable {
            mutableStateOf(userData?.name ?: "")
        }
        var number by rememberSaveable {
            mutableStateOf(userData?.number ?: "")
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            profileContent(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                viewModel = viewModel,
                name = name,
                number = number,
                onNameChange = { name = it },
                onNumberChange = { number = it },
                onSave = {
                    viewModel.createOrUpdateProfile(
                        name = name, number = number
                    )
                },
                onBack = {
                    navigateTo(
                        navController = navController,
                        route = DestinationScreen.AllChats.route
                    )
                },
                onLogOut = {
                    viewModel.logOut()
                    navigateTo(
                        navController = navController,
                        route = DestinationScreen.LogIn.route
                    )
                }
            )
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.PROFILE,
                navController = navController
            )

        }
    }

}

@Composable
fun profileContent(
    modifier: Modifier,
    onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: ChatViewModel,
    name: String,
    number: String,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onLogOut: () -> Unit
) {

    val imageUrl = viewModel.userData.value?.imageUrl
    Column( modifier = modifier ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back", Modifier.clickable {
                onBack.invoke()
            })
            Text(text = "Save", Modifier.clickable {
                onSave.invoke()
            })
        }

        commonDivider()
        profileImage(imageUrl = imageUrl, viewModel = viewModel)

        commonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Name", modifier = Modifier.width(100.dp))
            TextField(
                value = name, onValueChange = onNameChange,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Number", modifier = Modifier.width(100.dp))
            TextField(
                value = number, onValueChange = onNumberChange,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
        }

        commonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "LogOut", modifier = Modifier.clickable { onLogOut.invoke() })
        }
    }
}

@Composable
fun profileImage(imageUrl: String?, viewModel: ChatViewModel) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.uploadFileImage(uri)
        }
    }
    Box(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min))
    {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    launcher.launch("image/*")
                }, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                CommonImage(data = imageUrl)
            }
            Text("Change Profile Picture")
        }
        if (viewModel.inProcess.value) {
            CommonnProgressBar()
        }
    }

}