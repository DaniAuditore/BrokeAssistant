package com.example.brokeassistant.core.domain.usecase

import com.example.brokeassistant.core.domain.model.Category
import com.example.brokeassistant.core.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class DistributeIncomeUseCaseTest {

    private lateinit var useCase: DistributeIncomeUseCase
    private val zoneId = ZoneId.systemDefault()
    private val date = LocalDateTime.of(2023, 1, 1, 10, 0)
    private val instant = date.atZone(zoneId).toInstant()
    private val clock = Clock.fixed(instant, zoneId)

    @Before
    fun setUp() {
        useCase = DistributeIncomeUseCase(clock)
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
    fun `invoke with total percentage not 100 returns empty list`() {
        val categories = listOf(
            Category(id = 1, name = "Savings", percentage = 30),
            Category(id = 2, name = "Food", percentage = 30)
            // Total = 60
        )
        val amount = 1000L
        val transactions = useCase(amount, "Paycheck", categories, date)

        assertTrue(transactions.isEmpty())
    }
}
