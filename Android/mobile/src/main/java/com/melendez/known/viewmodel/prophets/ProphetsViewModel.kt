package com.melendez.known.viewmodel.prophets

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProphetsViewModel(private val generativeModel: GenerativeModel) : ViewModel() {
    private val _uiState: MutableStateFlow<ProphetsUiState> =
        MutableStateFlow(ProphetsUiState.Initial)

    val uiState: StateFlow<ProphetsUiState> = _uiState.asStateFlow()

    fun ask(prompt: String) {
        _uiState.value = ProphetsUiState.Loading

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                Log.i("Known", "ask: prompt: $prompt")
                response.text?.let { outputContent ->
                    _uiState.value = ProphetsUiState.Success(outputContent)
                    Log.i("Known", "ask: returning info: $outputContent")
                }
            } catch (e: Exception) {
                _uiState.value = ProphetsUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}