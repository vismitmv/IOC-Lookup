package com.example.ioclookup.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ioclookup.data.repository.IocRepository
import com.example.ioclookup.domain.model.IocType
import com.example.ioclookup.domain.model.LookupResult
import com.example.ioclookup.domain.model.Verdict
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryFilter(
    val query: String = "",
    val typeFilter: String = "",   // empty = all
    val verdictFilter: String = "" // empty = all
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: IocRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter())
    val filter: StateFlow<HistoryFilter> = _filter.asStateFlow()

    val lookups: StateFlow<List<LookupResult>> = _filter
        .flatMapLatest { f ->
            repository.searchLookups(f.query, f.typeFilter, f.verdictFilter)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChanged(q: String) = _filter.update { it.copy(query = q) }
    fun onTypeFilterChanged(type: String) = _filter.update { it.copy(typeFilter = type) }
    fun onVerdictFilterChanged(verdict: String) = _filter.update { it.copy(verdictFilter = verdict) }

    fun deleteItem(id: Long) = viewModelScope.launch { repository.deleteById(id) }
    fun clearAll() = viewModelScope.launch { repository.clearAll() }
}
