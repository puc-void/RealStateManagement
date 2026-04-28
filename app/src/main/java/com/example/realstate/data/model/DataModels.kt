package com.example.realstate.data.model

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class PropertyDto(
    val id: Int,
    val agentId: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val location: String,
    val priceRange: String,
    val propertyType: String,
    val isVerified: Boolean? = null,
    val isAdvertised: Boolean? = null,
    val isBought: Boolean? = null,
    val generatedAt: String? = null,
    val updatedAt: String? = null,
    val agent: AgentDto? = null,
    val user: UserDto? = null
)

data class AgentDto(
    val id: String,
    val userId: String,
    val isVerified: Boolean? = null,
    val isFraud: Boolean? = null
)

data class AgentDetailDto(
    val id: String,
    val userId: String,
    val isVerified: Boolean? = null,
    val isFraud: Boolean? = null,
    val user: UserDto? = null
)

data class UserDto(
    val id: String? = null,
    val name: String,
    val email: String? = null,
    val image: String? = null,
    val role: String? = null,
    val status: String? = null,
    val contactNumber: String? = null,
    val address: String? = null,
    val password: String? = null,
    val emailVerified: Boolean? = null,
    val generatedAt: String? = null,
    val updatedAt: String? = null
)

data class ReviewDto(
    val id: String,
    val propertyId: Int,
    val userId: String,
    val rating: Int,
    val description: String,
    val generatedAt: String? = null,
    val updatedAt: String? = null,
    val property: PropertySmallDto? = null,
    val user: UserDto? = null
)

data class PropertySmallDto(
    val title: String
)

data class WishlistItemDto(
    val id: String,
    val wishlistId: String,
    val propertyId: Int,
    val agentId: String,
    val addedAt: String? = null,
    val property: PropertyDto? = null,
    val agent: AgentDto? = null
)

data class BookedPropertyDto(
    val id: Int,
    val propertyId: Int,
    val userId: String,
    val agentId: String,
    val proposedAmount: String,
    val isPropAmountAccepted: Boolean,
    val isSold: Boolean,
    val bookedAt: String? = null,
    val updatedAt: String? = null,
    val property: PropertyDto? = null,
    val agent: AgentDto? = null,
    val user: UserDto? = null
)

data class SoldPropertyDto(
    val id: Int,
    val bookedPropertyId: Int,
    val propertyId: Int,
    val userId: String,
    val agentId: String,
    val amount: String,
    val soldAt: String? = null,
    val updatedAt: String? = null,
    val property: PropertyDto? = null,
    val agent: AgentDto? = null,
    val user: UserDto? = null
)
