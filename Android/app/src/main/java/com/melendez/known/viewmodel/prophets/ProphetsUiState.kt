package com.melendez.known.viewmodel.prophets


/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface ProphetsUiState {

    /**
     * Empty state when the screen is first shown
     */
    data object Initial : ProphetsUiState

    /**
     * Still loading
     */
    data object Loading : ProphetsUiState

    /**
     * Text has been generated
     */
    data class Success(
        val outputText: String,
    ) : ProphetsUiState

    /**
     * There was an error generating text
     */
    data class Error(
        val errorMessage: String,
    ) : ProphetsUiState
}