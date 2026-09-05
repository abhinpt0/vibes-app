package com.vibes.dsrapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vibes.dsrapp.model.DsrEntry
import com.vibes.dsrapp.model.RetailerEntry
import com.vibes.dsrapp.network.ApiClient
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class DsrViewModel : ViewModel() {

    private val gson = Gson()

    private val _dsrList = MutableLiveData<UiState<List<DsrEntry>>>()
    val dsrList: LiveData<UiState<List<DsrEntry>>> = _dsrList

    private val _currentDsr = MutableLiveData<UiState<DsrEntry>>()
    val currentDsr: LiveData<UiState<DsrEntry>> = _currentDsr

    private val _retailers = MutableLiveData<UiState<List<RetailerEntry>>>()
    val retailers: LiveData<UiState<List<RetailerEntry>>> = _retailers

    private val _submitResult = MutableLiveData<UiState<String>>()
    val submitResult: LiveData<UiState<String>> = _submitResult

    // ── Load DSR list ─────────────────────────────────────────────────────────
    fun loadDSRList(limit: Int = 30) {
        _dsrList.value = UiState.Loading
        viewModelScope.launch {
            ApiClient.getDSRList(limit)
                .onSuccess { json ->
                    val type = object : TypeToken<List<DsrEntry>>() {}.type
                    val list: List<DsrEntry> = gson.fromJson(json, type) ?: emptyList()
                    _dsrList.value = UiState.Success(list)
                }
                .onFailure { _dsrList.value = UiState.Error(it.message ?: "Network error") }
        }
    }

    // ── Load single DSR by date ───────────────────────────────────────────────
    fun loadDSRByDate(date: String) {
        _currentDsr.value = UiState.Loading
        viewModelScope.launch {
            ApiClient.getDSRByDate(date)
                .onSuccess { json ->
                    val entry = gson.fromJson(json, DsrEntry::class.java)
                    _currentDsr.value = UiState.Success(entry)
                }
                .onFailure { _currentDsr.value = UiState.Error(it.message ?: "Network error") }
        }
    }

    // ── Load retailers ────────────────────────────────────────────────────────
    fun loadRetailers() {
        _retailers.value = UiState.Loading
        viewModelScope.launch {
            ApiClient.getRetailers()
                .onSuccess { json ->
                    val type = object : TypeToken<List<RetailerEntry>>() {}.type
                    val list: List<RetailerEntry> = gson.fromJson(json, type) ?: emptyList()
                    _retailers.value = UiState.Success(list)
                }
                .onFailure { _retailers.value = UiState.Error(it.message ?: "Network error") }
        }
    }

    // ── Submit DSR ────────────────────────────────────────────────────────────
    fun submitDSR(entry: DsrEntry) {
        _submitResult.value = UiState.Loading
        viewModelScope.launch {
            ApiClient.submitDSR(entry)
                .onSuccess { json -> _submitResult.value = UiState.Success(json) }
                .onFailure { _submitResult.value = UiState.Error(it.message ?: "Submit failed") }
        }
    }
}
