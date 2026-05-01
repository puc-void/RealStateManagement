package com.example.realstate.data

import com.example.realstate.data.model.UserDto

object MockData {

    // Global Session State
    var currentUser = User(
        name = "Loading...",
        email = "",
        profilePicUrl = "https://i.pravatar.cc/150",
        role = UserRole.USER,
        phone = "",
        location = "",
        joinDate = "",
        id = "",
        wishlistId = ""
    )

    val wishlistId get() = if (currentUser.wishlistId.isNotEmpty()) currentUser.wishlistId else "0d1681c0-aba2-4393-969b-293dbb070999"
    var currentAgentId = ""

    // Remaining mock data for properties and lists
    val users = mutableListOf<User>() 

    val properties = listOf(
        Property(
            id = "1",
            title = "Modern Glass Villa",
            description = "A beautiful modern villa with fully automated smart home features, nestled in the Hollywood hills.",
            price = "$2,500,000",
            location = "Hollywood Hills, CA",
            imageUrl = "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80",
            category = "Villa",
            beds = 4,
            baths = 3,
            area = "4,500 sqft",
            amenities = listOf("Pool", "Smart Home", "Garage", "Air Conditioning"),
            agentName = "Sarah Jenkins",
            agentPicUrl = "https://i.pravatar.cc/150?img=9"
        )
    )

    val orders = listOf<Order>()
}
