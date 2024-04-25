package com.example.myapplication.Layouts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ChatViewModel
import com.example.myapplication.CommonRow
import com.example.myapplication.CommonnProgressBar
import com.example.myapplication.DestinationScreen
import com.example.myapplication.TitleText
import com.example.myapplication.commonDivider
import com.example.myapplication.navigateTo

@Composable
fun AllStatusScreen(navController: NavController, viewModel: ChatViewModel) {
    val inProcess = viewModel.inProgressStatus.value
    if (inProcess) {
        CommonnProgressBar()
    } else {
        val statutes = viewModel.status.value
        val userData = viewModel.userData.value

        val myStatuses = statutes.filter {
            it.user.userId == userData?.userId
        }

        val otherStatuses = statutes.filter {
            it.user.userId != userData?.userId
        }

        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent() ){
            uri ->
            uri?.let{
                viewModel.uploadStatus(uri)
            }
        }

        Scaffold(
            floatingActionButton = {
                FAB {
                    launcher.launch("image/*")
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(it)
                ) {
                    TitleText(text = "Status")
                    if (statutes.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Status Available")
                        }
                    } else {
                        if (myStatuses.isNotEmpty()) {
                            CommonRow(
                                imageUrl = myStatuses[0].user.imageUrl,
                                name = myStatuses[0].user.name
                            ) {
                                navigateTo(
                                    navController = navController,
                                    DestinationScreen.SingleStatus.createRoute(myStatuses[0].user.userId!!)
                                )
                            }
                            commonDivider()
                            val uniquieUsers = otherStatuses.map { it.user }.toSet().toList()
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(uniquieUsers) { user ->
                                    CommonRow(imageUrl = user.imageUrl, name = user.name) {
                                        navigateTo(
                                            navController = navController,
                                            DestinationScreen.SingleStatus.createRoute(user.userId!!)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    BottomNavigationMenu(
                        selectedItem = BottomNavigationItem.STATUSLIST,
                        navController = navController
                    )
                }

            }
        )
    }
}

@Composable
fun FAB(
    onFabClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Add Status",
            tint = Color.White
        )
    }
}