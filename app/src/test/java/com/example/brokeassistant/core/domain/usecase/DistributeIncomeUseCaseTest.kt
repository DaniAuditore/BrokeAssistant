package com.example.brokeassistant.core.domain.usecase

import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class DistributeIncomeUseCaseTest {

    private lateinit var useCase: DistributeIncomeUseCase
    private val date = LocalDateTime.of(2023, 1, 1, 10, 0)

    @Before
    fun setUp() {
        useCase = DistributeIncomeUseCase()
    }

    @Test
    fun `invoke with exact distribution returns exact amounts`() {
        val categories = listOf(
            Category(id = 1, name = "Savings", percentage = 50),
            Category(id = 2, name = "Food", percentage = 30),
            Category(id = 3, name = "Fun", percentage = 20)
        )
        val amount = 1000L // 10.00
        val transactions = useCase(amount, "Paycheck", categories, date)

        assertEquals(3, transactions.size)
        
        val savings = transactions.find { it.categoryId == 1L }!!
        assertEquals(500L, savings.amountInCents)
        
        val food = transactions.find { it.categoryId == 2L }!!
        assertEquals(300L, food.amountInCents)
        
        val funCategory = transactions.find { it.categoryId == 3L }!!
        assertEquals(200L, funCategory.amountInCents)
    }

    @Test
    fun `invoke with rounding remainder adds cents to default free category`() {
        val categories = listOf(
            Category(id = 1, name = "Savings", percentage = 33),
            Category(id = 2, name = "Food", percentage = 33),
            Category(id = 3, name = "Fun", percentage = 34, isDefaultFree = true)
        )
        val amount = 1000L // 10.00
        // 33% of 1000 = 330
        // 34% of 1000 = 340
        // Total = 1000. 
        // Wait, what if amount = 1001?
        // 1001 * 33 / 100 = 330
        // 1001 * 34 / 100 = 340
        // 330 + 330 + 340 = 1000.
        // Remainder = 1.
        
        val testAmount = 1001L 
        val transactions = useCase(testAmount, "Paycheck", categories, date)

        val savings = transactions.find { it.categoryId == 1L }!!
        assertEquals(330L, savings.amountInCents)
        
        val food = transactions.find { it.categoryId == 2L }!!
        assertEquals(330L, food.amountInCents)
        
        val funCategory = transactions.find { it.categoryId == 3L }!!
        // 340 + 1 remainder
        assertEquals(341L, funCategory.amountInCents)
    }

    @Test
    fun `invoke with rounding remainder without default free adds cents to highest percentage`() {
        val categories = listOf(
            Category(id = 1, name = "Savings", percentage = 33), // max
            Category(id = 2, name = "Food", percentage = 33),
            Category(id = 3, name = "Fun", percentage = 34) // max is 34
        )
        val testAmount = 1001L 
        val transactions = useCase(testAmount, "Paycheck", categories, date)

        val savings = transactions.find { it.categoryId == 1L }!!
        assertEquals(330L, savings.amountInCents)
        
        val food = transactions.find { it.categoryId == 2L }!!
        assertEquals(330L, food.amountInCents)
        
        val funCategory = transactions.find { it.categoryId == 3L }!!
        // 340 + 1 remainder
        assertEquals(341L, funCategory.amountInCents)
    }

    @Test
    fun `invoke creates transaction models correctly`() {
        val categories = listOf(
            Category(id = 1, name = "Savings", percentage = 100)
        )
        val amount = 500L
        val desc = "Paycheck"
        val transactions = useCase(amount, desc, categories, date)

        assertEquals(1, transactions.size)
        val tx = transactions.first()
        assertEquals(1L, tx.categoryId)
        assertEquals(TransactionType.INCOME, tx.type)
        assertEquals(500L, tx.amountInCents)
        assertEquals("$desc - Savings", tx.description)
        assertEquals(date, tx.date)
    }
}
