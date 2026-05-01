package com.example.realstate.data

enum class UserRole {
    USER, ADMIN, AGENT
}

data class Property(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val location: String,
    val imageUrl: String,
    val category: String,
    val beds: Int,
    val baths: Int,
    val area: String,
    val isVerified: Boolean = true,
    val amenities: List<String> = emptyList(),
    val agentName: String = "John Doe",
    val agentPicUrl: String = "https://i.pravatar.cc/150?img=12",
    val agentId: String = ""
)

data class Order(
    val id: String,
    val property: Property,
    val status: String,
    val date: String
)

data class User(
    val name: String,
    val email: String,
    val profilePicUrl: String = "",
    val role: UserRole = UserRole.USER,
    val phone: String = "+1 234 567 890",
    val location: String = "New York, USA",
    val joinDate: String = "January 2024",
    val status: String = "ACTIVE",
    val id: String = "user-123",
    val wishlistId: String = ""
)
