package com.sabeal.watanshop.messages.WhizzChat.Models.ChatScreenModels

import com.google.gson.annotations.SerializedName
import com.sabeal.watanshop.messages.WhizzChat.Models.Extra

data class ChatModel(
        @SerializedName("data")
        val `data`: Data,
        @SerializedName("message")
        val message: String,
        @SerializedName("success")
        val success: Boolean,
        @SerializedName("extra")
        val extra: Extra
)