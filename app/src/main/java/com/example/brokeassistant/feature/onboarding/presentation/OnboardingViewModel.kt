package com.example.brokeassistant.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brokeassistant.core.datastore.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val selectedCurrency: String = "USD"
)

sealed class OnboardingIntent {
    data class SelectCurrency(val currency: String) : OnboardingIntent()
    object SaveCurrency : OnboardingIntent()
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.SelectCurrency -> {
                _state.update { it.copy(selectedCurrency = intent.currency) }
            }
            OnboardingIntent.SaveCurrency -> {
                viewModelScope.launch {
                    preferencesDataStore.saveCurrency(_state.value.selectedCurrency)
                }
            }
        }
    }
}
