package com.example.myapplication.data

data class UserData(
    var userId : String?="",
    var name : String?="",
    var number : String?="",
    var imageUrl : String?=""
){
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "number" to number,
        "imageUrl" to imageUrl
    )
}

data class ChatData(
    val chatId : String ?= "",
    var user1 : ChatUser = ChatUser(),
    var user2 : ChatUser = ChatUser()
)

data class ChatUser(
    val userId : String?="",
    val name : String?="",
    val imageUrl : String?="",
    val number : String?=""
)

data class Message(
    var sendBy : String?="",
    var message : String?="",
    var timeStamp : String?=""
)

data class Status(
    val user : ChatUser = ChatUser(),
    val imageUrl: String?="",
    val timeStamp: Long?=null
)