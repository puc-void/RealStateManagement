package com.example.realstate.data

object MockData {

    var currentUser = User(
        name = "Mehedi Hasan",
        email = "mehedi@example.com",
        profilePicUrl = "https://i.pravatar.cc/150?img=11",
        role = UserRole.USER,
        phone = "+880 1712 345678",
        location = "Dhaka, Bangladesh",
        joinDate = "January 2024",
        id = "950c7e11-4af8-44b3-bd90-38ddfbefb836"
    )

    const val currentWishlistId = "0d1681c0-aba2-4393-969b-293dbb070999"
    var currentAgentId = "508f7879-e762-4666-869f-539c017df80a"

    val users = listOf(
        User("Abdul Hannan", "abdul.hannan@example.com", "https://i.pravatar.cc/150?img=11", UserRole.ADMIN, id = "e5592ed8-103a-407e-960b-28594e85cfff"),
        User("Sarah Jenkins", "sarah@realstate.com", "https://i.pravatar.cc/150?img=9", UserRole.AGENT, id = currentAgentId),
        User("Shaiful Islam", "shaifulislam@gmail.com", "https://i.pravatar.cc/150?img=3", UserRole.AGENT, id = "e09213da-bab4-4f99-9c46-324c329db9f0"),
        User("Nusrat Jahan", "nusrat@example.com", "https://i.pravatar.cc/150?img=5", UserRole.AGENT, id = "3c1d61d6-25e7-4e03-83dc-a61d90e4e38e"),
        User("Mehedi Hasan", "mehedi@example.com", "https://i.pravatar.cc/150?img=11", UserRole.USER, id = "950c7e11-4af8-44b3-bd90-38ddfbefb836"),
        User("Mike Ross", "mike@realstate.com", "https://i.pravatar.cc/150?img=12", UserRole.AGENT, id = "913fc870-beba-44de-9a0d-98b3c79ddb2c"),
    )

    val properties = listOf(
        Property(
            id = "1",
            title = "Modern Glass Villa",
            description = "A beautiful modern villa with fully automated smart home features, nestled in the Hollywood hills. Comes with a heated infinity pool and a magnificent view of the city.",
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
        ),
        Property(
            id = "2",
            title = "Cozy City Apartment",
            description = "Located in the heart of downtown, this cozy apartment is a walker's paradise. Featuring exposed brick and original hardwood floors.",
            price = "$850,000",
            location = "Downtown LA, CA",
            imageUrl = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80",
            category = "Apartment",
            beds = 2,
            baths = 2,
            area = "1,200 sqft",
            amenities = listOf("Gym", "Balcony", "Pet Friendly"),
            agentName = "Mike Ross",
            agentPicUrl = "https://i.pravatar.cc/150?img=11"
        ),
        Property(
            id = "3",
            title = "Suburban Family Home",
            description = "Perfect for a growing family! This home features a large backyard, newly renovated kitchen, and sits in a top-rated school district.",
            price = "$1,150,000",
            location = "Pasadena, CA",
            imageUrl = "https://images.unsplash.com/photo-1583608205776-bfd35f0d9f83?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80",
            category = "House",
            beds = 3,
            baths = 2,
            area = "2,300 sqft",
            amenities = listOf("Backyard", "Garage", "Dishwasher"),
            agentName = "Jessica Pearson",
            agentPicUrl = "https://i.pravatar.cc/150?img=5"
        ),
        Property(
            id = "4",
            title = "Luxury Penthouse",
            description = "Luxurious penthouse overlooking the skyline. Offers 24/7 concierge, private elevator, and an expansive rooftop terrace.",
            price = "$4,200,000",
            location = "Beverly Hills, CA",
            imageUrl = "https://images.unsplash.com/photo-1600607687931-ce8e00262590?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80",
            category = "Penthouse",
            beds = 5,
            baths = 4,
            area = "5,100 sqft",
            amenities = listOf("Pool", "Concierge", "Elevator", "Rooftop"),
            agentName = "Harvey Specter",
            agentPicUrl = "https://i.pravatar.cc/150?img=12"
        ),
        Property(
            id = "5",
            title = "Minimalist Studio",
            description = "A sleek and minimal studio apartment designed for modern living. Excellent natural lighting and space-saving built-ins.",
            price = "$550,000",
            location = "Silver Lake, CA",
            imageUrl = "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80",
            category = "Studio",
            beds = 1,
            baths = 1,
            area = "750 sqft",
            amenities = listOf("Gym", "Laundry"),
            agentName = "Louis Litt",
            agentPicUrl = "https://i.pravatar.cc/150?img=13"
        )
    )

    val orders = listOf(
        Order(
            id = "ORD-1234",
            property = properties[1],
            status = "Completed",
            date = "Oct 12, 2025"
        ),
        Order(
            id = "ORD-5678",
            property = properties[0],
            status = "Pending Approval",
            date = "Nov 02, 2025"
        ),
        Order(
            id = "ORD-9012",
            property = properties[4],
            status = "In Progress",
            date = "Jan 18, 2026"
        )
    )
}
