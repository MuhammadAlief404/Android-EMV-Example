package com.example.androidemvtutorial.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

object CardData {
    private var cardData = mutableStateOf(listOf("Please tap your card to the reader."))
    private val listeners = mutableListOf<(List<String>) -> Unit>()

    fun getData(): MutableState<List<String>> {
        return cardData
    }

    fun updateData(newValue: List<String>) {
        cardData.value = newValue
        notifyListeners(newValue)
    }

    private fun notifyListeners(value: List<String>) {
        listeners.forEach { it(value) }
    }
}