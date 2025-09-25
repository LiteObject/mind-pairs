package com.liteobject.mindpairs.model

data class Card(
    val id: Int,
    val imageRes: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)
