package com.example.realstate.data.network

import com.example.realstate.data.model.*
import retrofit2.http.*

interface PropertyApiService {
    @GET("property")
    suspend fun getAllProperties(): BaseResponse<List<PropertyDto>>
    
    @GET("property/{id}")
    suspend fun getPropertyDetails(@Path("id") id: String): BaseResponse<PropertyDto>
    
    @GET("property/agent/{id}")
    suspend fun getPropertiesByAgent(@Path("id") agentId: String): BaseResponse<List<PropertyDto>>
    
    @POST("property")
    suspend fun addProperty(@Body property: Map<String, @JvmSuppressWildcards Any>): BaseResponse<PropertyDto>
    
    @PUT("property/{id}")
    suspend fun updateProperty(@Path("id") id: String, @Body property: Map<String, @JvmSuppressWildcards Any>): BaseResponse<PropertyDto>
    
    @DELETE("property/{id}")
    suspend fun deleteProperty(@Path("id") id: String): BaseResponse<PropertyDto>
}

interface ReviewApiService {
    @GET("review")
    suspend fun getAllReviews(): BaseResponse<List<ReviewDto>>
    
    @GET("review/property/{id}")
    suspend fun getReviewsByProperty(@Path("id") propertyId: String): BaseResponse<List<ReviewDto>>
    
    @GET("review/{id}")
    suspend fun getReviewDetails(@Path("id") id: String): BaseResponse<ReviewDto>
    
    @POST("review")
    suspend fun addReview(@Body review: Map<String, @JvmSuppressWildcards Any>): BaseResponse<ReviewDto>
    
    @PUT("review/{id}")
    suspend fun updateReview(@Path("id") id: String, @Body review: Map<String, @JvmSuppressWildcards Any>): BaseResponse<ReviewDto>
    
    @DELETE("review/{id}")
    suspend fun deleteReview(@Path("id") id: String): BaseResponse<ReviewDto>
    
    @GET("review/user/{id}")
    suspend fun getReviewsByUser(@Path("id") userId: String): BaseResponse<List<ReviewDto>>
}

interface WishlistApiService {
    @GET("wishlist-item")
    suspend fun getAllWishlistItems(): BaseResponse<List<WishlistItemDto>>

    @GET("wishlist-item/{id}")
    suspend fun getWishlistItemDetails(@Path("id") id: String): BaseResponse<WishlistItemDto>

    @GET("wishlist-item/wishlist/{wishlistId}")
    suspend fun getWishlistItems(@Path("wishlistId") wishlistId: String): BaseResponse<List<WishlistItemDto>>
    
    @POST("wishlist-item")
    suspend fun addWishlistItem(@Body item: Map<String, @JvmSuppressWildcards Any>): BaseResponse<WishlistItemDto>
    
    @DELETE("wishlist-item/{id}")
    suspend fun deleteWishlistItem(@Path("id") id: String): BaseResponse<WishlistItemDto>
}

interface BookedPropertyApiService {
    @GET("booked-property")
    suspend fun getAllBookedProperties(): BaseResponse<List<BookedPropertyDto>>
    
    @GET("booked-property/user/{userId}")
    suspend fun getBookedPropertiesByUser(@Path("userId") userId: String): BaseResponse<List<BookedPropertyDto>>
    
    @POST("booked-property")
    suspend fun bookProperty(@Body booking: Map<String, @JvmSuppressWildcards Any>): BaseResponse<BookedPropertyDto>
    
    @PUT("booked-property/{id}")
    suspend fun updateBookedPropertyStatus(@Path("id") id: String, @Body update: Map<String, @JvmSuppressWildcards Any>): BaseResponse<BookedPropertyDto>
    
    @DELETE("booked-property/{id}")
    suspend fun deleteBookedProperty(@Path("id") id: String): BaseResponse<BookedPropertyDto>
}

interface UserApiService {
    @GET("auth")
    suspend fun getAllUsers(): BaseResponse<List<UserDto>>
    
    @GET("auth/{id}")
    suspend fun getUserDetails(@Path("id") id: String): BaseResponse<UserDto>
    
    @PUT("auth/{id}")
    suspend fun updateUserStatus(@Path("id") id: String, @Body update: Map<String, String>): BaseResponse<UserDto>

    @PUT("auth/update-profile/{id}")
    suspend fun updateProfile(@Path("id") id: String, @Body update: Map<String, @JvmSuppressWildcards Any>): BaseResponse<UserDto>

    @DELETE("auth/{id}")
    suspend fun deleteUser(@Path("id") id: String): BaseResponse<UserDto>
}
interface AgentApiService {
    @GET("agent")
    suspend fun getAllAgents(): BaseResponse<List<AgentDetailDto>>
    
    @GET("agent/{id}")
    suspend fun getAgentDetails(@Path("id") id: String): BaseResponse<AgentDetailDto>
    
    @PUT("agent/verify-agent/{id}")
    suspend fun verifyAgent(@Path("id") id: String, @Body body: Map<String, Boolean>): BaseResponse<AgentDetailDto>
    
    @PUT("agent/fraud-agent/{id}")
    suspend fun markAgentFraud(@Path("id") id: String, @Body body: Map<String, Boolean>): BaseResponse<AgentDetailDto>
    
    @DELETE("agent/{id}")
    suspend fun deleteAgent(@Path("id") id: String): BaseResponse<AgentDetailDto>
}

interface SoldPropertyApiService {
    @GET("sold-property")
    suspend fun getAllSoldProperties(): BaseResponse<List<SoldPropertyDto>>
    
    @GET("sold-property/{id}")
    suspend fun getSoldPropertyDetails(@Path("id") id: String): BaseResponse<SoldPropertyDto>
    
    @GET("sold-property/user/{userId}")
    suspend fun getSoldPropertiesByUser(@Path("userId") userId: String): BaseResponse<List<SoldPropertyDto>>
    
    @POST("sold-property")
    suspend fun addSoldProperty(@Body body: Map<String, @JvmSuppressWildcards Any>): BaseResponse<SoldPropertyDto>
}
