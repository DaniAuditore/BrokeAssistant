package com.example.brokeassistant.core.domain.usecase

import com.example.brokeassistant.core.domain.model.Category
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateCategoryPercentagesUseCaseTest {

    private lateinit var useCase: ValidateCategoryPercentagesUseCase

    @Before
    fun setUp() {
        useCase = ValidateCategoryPercentagesUseCase()
    }

    @Test
    fun `invoke with empty list returns false`() {
        val result = useCase(emptyList())
        assertFalse(result)
    }

    @Test
    fun `invoke with exactly 100 percent returns true`() {
        val categories = listOf(
            Category(name = "Savings", percentage = 50),
            Category(name = "Food", percentage = 30),
            Category(name = "Fun", percentage = 20)
        )
        val result = useCase(categories)
        assertTrue(result)
    }

    @Test
    fun `invoke with less than 100 percent returns false`() {
        val categories = listOf(
            Category(name = "Savings", percentage = 50),
            Category(name = "Food", percentage = 30)
        )
        val result = useCase(categories)
        assertFalse(result)
    }

    @Test
    fun `invoke with more than 100 percent returns false`() {
        val categories = listOf(
            Category(name = "Savings", percentage = 50),
            Category(name = "Food", percentage = 30),
            Category(name = "Fun", percentage = 30)
        )
        val result = useCase(categories)
        assertFalse(result)
    }
}
