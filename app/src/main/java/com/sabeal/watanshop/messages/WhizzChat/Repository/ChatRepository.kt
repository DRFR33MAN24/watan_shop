package com.sabeal.watanshop.messages.WhizzChat.Repository

class ChatRepository() {



    companion object {
        private var INSTANCE: ChatRepository? = null
        fun getInstance() = INSTANCE
                ?: ChatRepository().also {
                    INSTANCE = it
                }
    }

}