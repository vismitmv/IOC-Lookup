package com.example.ioclookup.ui.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ioclookup.data.repository.IocRepository
import com.example.ioclookup.domain.model.LookupResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: IocRepository
) : ViewModel() {

    val bookmarks: StateFlow<List<LookupResult>> = repository.getBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateNote(id: Long, note: String) = viewModelScope.launch {
        repository.updateBookmark(id, true, note)
    }

    fun removeBookmark(id: Long) = viewModelScope.launch {
        repository.updateBookmark(id, false, "")
    }
}
