package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.Layouts.LoginScreen
import com.example.myapplication.Layouts.SignUpScreen
import com.example.myapplication.ui.theme.LiveChatApplicationTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.Layouts.AllChatsScreen
import com.example.myapplication.Layouts.AllStatusScreen
import com.example.myapplication.Layouts.ProfileScreen
import com.example.myapplication.Layouts.SingleChatScreen
import com.example.myapplication.Layouts.SingleStatusScreen
import dagger.hilt.android.AndroidEntryPoint

// navigation classes
sealed class DestinationScreen(var route : String ){
    object SignUp : DestinationScreen("signup")
    object LogIn : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object AllChats : DestinationScreen("allchats")
    object SingleChats : DestinationScreen("singlechats/{chatId}"){
        fun createRoute(id : String ) = "singlechats/$id"
    }
    object AllStatus : DestinationScreen("allstatus")
    object SingleStatus : DestinationScreen("singlestatus/{userId}"){
        fun createRoute(userId : String ) = "singlestatus/$userId"
    }
}
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveChatApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatNavigation()
                }
            }
        }
    }

    @Composable
    fun ChatNavigation(){

        val viewModel = hiltViewModel<ChatViewModel>()
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route ){
            composable(DestinationScreen.SignUp.route){
                SignUpScreen(navController , viewModel)
            }
            composable(DestinationScreen.LogIn.route){
                LoginScreen( viewModel , navController )
            }
            composable(DestinationScreen.AllChats.route){
                AllChatsScreen(navController,viewModel)
            }
            composable(DestinationScreen.AllStatus.route){
                AllStatusScreen(navController,viewModel)
            }
            composable(DestinationScreen.Profile.route){
                ProfileScreen(navController,viewModel)
            }
            composable(DestinationScreen.SingleChats.route){
                val chatId = it.arguments?.getString("chatId")
                chatId?.let {
                    SingleChatScreen(navController,viewModel,chatId)
                }
            }
            composable(DestinationScreen.SingleStatus.route){
                val userId = it.arguments?.getString("userId")
                userId?.let {
                    SingleStatusScreen(navController = navController , viewModel = viewModel , userId = it )

                }
            }

        }
    }
}