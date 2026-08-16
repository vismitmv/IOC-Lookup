package com.example.ioclookup.ui.lookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ioclookup.data.export.ExportService
import com.example.ioclookup.data.repository.IocRepository
import com.example.ioclookup.domain.model.IocType
import com.example.ioclookup.domain.model.LookupResult
import com.example.ioclookup.domain.util.IocDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class LookupUiState(
    val inputText: String = "",
    val detectedType: IocType = IocType.UNKNOWN,
    val isLooking: Boolean = false,
    val result: LookupResult? = null,
    val error: String? = null,
    val exportedFile: File? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class LookupViewModel @Inject constructor(
    private val repository: IocRepository,
    private val exportService: ExportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LookupUiState())
    val uiState: StateFlow<LookupUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) {
        val type = if (text.isBlank()) IocType.UNKNOWN else IocDetector.detect(text)
        _uiState.update { it.copy(inputText = text, detectedType = type, error = null) }
    }

    fun performLookup(forceRefresh: Boolean = false) {
        val state = _uiState.value
        val ioc = state.inputText.trim()
        if (ioc.isBlank()) {
            _uiState.update { it.copy(error = "Please enter an IOC to look up") }
            return
        }
        val type = state.detectedType
        if (type == IocType.UNKNOWN) {
            _uiState.update { it.copy(error = "Could not detect IOC type. Please check your input.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLooking = true, error = null, result = null) }
            try {
                val result = repository.lookup(ioc, type, forceRefresh)
                _uiState.update { it.copy(isLooking = false, result = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLooking = false, error = "Lookup failed: ${e.message}") }
            }
        }
    }

    fun toggleBookmark() {
        val result = _uiState.value.result ?: return
        viewModelScope.launch {
            val newBookmarked = !result.isBookmarked
            repository.updateBookmark(result.id, newBookmarked, result.bookmarkNote)
            _uiState.update {
                it.copy(
                    result = result.copy(isBookmarked = newBookmarked),
                    snackbarMessage = if (newBookmarked) "Bookmarked!" else "Bookmark removed"
                )
            }
        }
    }

    fun exportAsText(): String? = _uiState.value.result?.let { exportService.asPlainText(it) }

    fun exportAsPdf(): File? {
        return try {
            _uiState.value.result?.let { exportService.asPdf(it) }
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = "PDF export failed: ${e.message}") }
            null
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
