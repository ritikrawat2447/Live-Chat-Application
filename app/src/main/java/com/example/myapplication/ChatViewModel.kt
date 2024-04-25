package com.example.myapplication

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.myapplication.data.CHATS
import com.example.myapplication.data.ChatData
import com.example.myapplication.data.ChatUser
import com.example.myapplication.data.Event
import com.example.myapplication.data.MESSAGE
import com.example.myapplication.data.Message
import com.example.myapplication.data.STATUS
import com.example.myapplication.data.Status
import com.example.myapplication.data.USER
import com.example.myapplication.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    var inProcess = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    val eventutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration? = null
    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun signup(name: String, number: String, email: String, password: String) {
        inProcess.value = true
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please Fill all Fields")
        }
        db.collection(USER).whereEqualTo("number", number).get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                signIn.value = true
                                createOrUpdateProfile(name, number)
                            } else {
                                handleException(it.exception, "Sign Up Failed")
                            }
                        }
                } else {
                    handleException(customMessage = "Number Already Registered")
                    inProcess.value = false
                }
            }
    }

    fun logIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            handleException(customMessage = "Please Fill All Details ")
        } else {
            inProcess.value = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        signIn.value = true
                        inProcess.value = false
                        auth.currentUser?.uid?.let {
                            getUserData(it)
                        }
                    } else {
                        handleException(exception = it.exception, customMessage = " Login Failed ")
                    }
                }
        }
    }

    public fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageUrl: String? = null
    ) {
        var uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            number = number ?: userData.value?.number,
            imageUrl = imageUrl ?: userData.value?.imageUrl
        )
        uid?.let {
            inProcess.value = true
            db.collection(USER).document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        db.collection(USER).document(uid).set(userData)
                        inProcess.value = false
                        getUserData(uid)
                    } else {
                        db.collection(USER).document(uid).set(userData)
                        inProcess.value = false
                        getUserData(uid)
                    }
                }
                .addOnFailureListener {
                    handleException(it, "Cannot Retrieve User")
                }
        }

    }

    private fun getUserData(uid: String) {
        inProcess.value = true;
        db.collection(USER).document(uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error, "Cannot Not Retrieve User")
                }
                if (value != null) {
                    val user = value.toObject<UserData>()
                    userData.value = user
                    inProcess.value = false
                    populateChats()
                    populateStatuses()
                }
            }
    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("LiveChatApp", "Chat Exception : ", exception)
        exception?.printStackTrace()
        val errorMessage = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNullOrBlank()) errorMessage else customMessage

        eventutableState.value = Event(message)
        inProcess.value = false
    }

    fun uploadFileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProcess.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val upload = imageRef.putFile(uri)
        upload
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
                inProcess.value = false
            }
            .addOnFailureListener {
                handleException(it)
            }
    }

    fun logOut() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        dePopulateMessages()
        currentChatMessageListener = null
        eventutableState.value = Event("Logged Out")
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() || !number.isDigitsOnly()) {
            handleException(customMessage = "Number must contains Digits !!! ")
        } else {
            db.collection(CHATS).where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.number", number),
                        Filter.equalTo("user2.number", userData.value?.number)
                    ),
                    Filter.and(
                        Filter.equalTo("user1.number", userData.value?.number),
                        Filter.equalTo("user2.number", number)
                    ),

                    )
            ).get().addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection(USER).whereEqualTo("number", number).get().addOnSuccessListener {
                        if (it.isEmpty) {
                            handleException(customMessage = "Number not found !!!")
                        } else {
                            val chatPartner = it.toObjects<UserData>()[0]
                            val id = db.collection(CHATS).document().id
                            val chat = ChatData(
                                chatId = id,
                                ChatUser(
                                    userData.value?.userId,
                                    userData.value?.name,
                                    userData.value?.imageUrl,
                                    userData.value?.number
                                ),
                                ChatUser(
                                    chatPartner.userId,
                                    chatPartner.name,
                                    chatPartner.imageUrl,
                                    chatPartner.number
                                )
                            )
                            db.collection(CHATS).document(id).set(chat)
                        }
                    }
                        .addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(customMessage = "Chat Already Exists ")
                }
            }
        }
    }

    fun populateChats() {
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()

                }
                inProcessChats.value = false
            }
        }
    }

    fun populateMessages(chatId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener =
            db.collection(CHATS).document(chatId).collection(MESSAGE).addSnapshotListener { value , error ->
                if ( error!= null ){
                    handleException(error)
                }
                if ( value != null ){
                    chatMessages.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timeStamp }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun dePopulateMessages(){
        chatMessages.value = listOf()
        currentChatMessageListener = null
    }

    fun onSendReply(chatId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.userId, message, time)
        db.collection(CHATS).document(chatId).collection(MESSAGE).document().set(msg)
    }

    fun uploadStatus(uri: Uri) {
        uploadImage(uri){
            createStatus(it.toString())
        }
    }

    fun createStatus(imageUrl: String?){
        val newStatus = Status(
            ChatUser(
                userData.value?.userId,
                userData.value?.name,
                userData.value?.imageUrl,
                userData.value?.number
            ),
            imageUrl,
            System.currentTimeMillis()
        )
        db.collection(STATUS).document().set(newStatus)
    }

    fun populateStatuses(){
        val timeDelta = 24L * 60 * 60 * 1000
        val cutOff = System.currentTimeMillis() - timeDelta
        inProgressStatus.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId",userData.value?.userId),
                Filter.equalTo("user2.userId",userData.value?.userId)
            )
        )
            .addSnapshotListener{ value , error ->
                if ( error != null ){
                    handleException(error)
                }
                if ( value != null ){
                    val currentConnections = arrayListOf(userData.value?.userId)
                    val chats = value.toObjects<ChatData>()
                    chats.forEach{
                        chat ->
                        if ( chat.user1.userId == userData.value?.userId){
                            currentConnections.add(chat.user2.userId)
                        }else{
                            currentConnections.add(chat.user1.userId)
                        }
                    }
                    db.collection(STATUS).whereGreaterThan("timeStamp",cutOff).whereIn("user.userId",currentConnections).addSnapshotListener{ value , error ->
                        if ( error != null ){
                            handleException(error)
                        }
                        if ( value != null ){
                            status.value = value.toObjects()
                            inProgressStatus.value = false
                        }

                    }
                }

            }
    }

}