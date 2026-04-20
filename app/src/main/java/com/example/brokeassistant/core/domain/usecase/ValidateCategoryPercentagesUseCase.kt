package com.example.brokeassistant.core.domain.usecase

import com.example.brokeassistant.core.domain.model.Category

class ValidateCategoryPercentagesUseCase {
    operator fun invoke(categories: List<Category>): Boolean {
        if (categories.isEmpty()) return false
        val totalPercentage = categories.sumOf { it.percentage }
        return totalPercentage == 100
    }
}
