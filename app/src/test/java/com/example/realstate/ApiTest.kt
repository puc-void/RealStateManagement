package com.example.realstate

import com.example.realstate.data.network.RetrofitClient
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ApiTest {
    @Test
    fun testDashboard() = runBlocking {
        println("Fetching booked properties...")
        val booked = RetrofitClient.bookedPropertyApi.getAllBookedProperties()
        println("Booked Properties Success: ${booked.success}, size: ${booked.data.size}")
    }
}
