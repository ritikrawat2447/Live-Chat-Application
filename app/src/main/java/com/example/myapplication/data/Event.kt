package com.example.myapplication.data
open class Event<out T>(val content : T ){
    var hasHandled = false
    fun getContentOrNull(): T?{
        return if(hasHandled) null
        else{
            hasHandled = true
            content
        }
    }
}
