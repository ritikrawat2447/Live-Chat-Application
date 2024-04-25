package com.example.myapplication.Layouts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ChatViewModel
import com.example.myapplication.CommonImage
import com.example.myapplication.commonDivider
import com.example.myapplication.data.Message

@Composable
fun SingleChatScreen(navController: NavController, viewModel: ChatViewModel, chatId: String) {

    var reply by rememberSaveable {
        mutableStateOf("")
    }

    val sendReply = {
        viewModel.onSendReply(chatId, reply)
        reply = ""
    }

    val myUser = viewModel.userData.value
    val currentChat = viewModel.chats.value.first { it.chatId == chatId }
    var chatMessages = viewModel.chatMessages

    val chatuser =
        if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1

    LaunchedEffect(key1 = Unit) {
        viewModel.populateMessages(chatId)
    }
    BackHandler {
        viewModel.dePopulateMessages()
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            chatHeader(name = chatuser.name ?: "", imageUrl = chatuser?.imageUrl ?: "") {
                navController.popBackStack()
                viewModel.dePopulateMessages()
            }
            messageBox(
                modifier = Modifier,
                chatMessages = chatMessages.value,
                currentUserId = myUser?.userId ?: ""
            )
        }
        replyBox(reply = reply, onReplyChange = { reply = it }, onSendReply = sendReply)
    }
}

@Composable
fun replyBox(reply: String, onReplyChange: (String) -> Unit, onSendReply: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        commonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(value = reply, onValueChange = onReplyChange, maxLines = 3,modifier = Modifier.weight(1f))
            Button(onClick = onSendReply, modifier = Modifier.padding(start = 8.dp)) {
                Text(text = "Send")
            }
        }
    }
}

@Composable
fun chatHeader(name: String, imageUrl: String, onBackPressed: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.ArrowBack, contentDescription = null, modifier = Modifier
            .clickable {
                onBackPressed.invoke()
            }
            .padding(8.dp))
        CommonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(text = name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
fun messageBox(modifier: Modifier, chatMessages: List<Message>, currentUserId: String) {
    LazyColumn(modifier = modifier) {
        items(chatMessages) { msg ->
            val alignment = if (msg.sendBy == currentUserId) Alignment.End else Alignment.Start
            val color = if (msg.sendBy == currentUserId) Color(0xFF68cC400) else Color(0xFFC0C0C0)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), horizontalAlignment = alignment
            ) {
                Text(
                    text = msg.message ?: "",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(12.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}