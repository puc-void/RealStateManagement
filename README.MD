# RealEstateHub 🏠

**"Discover, Buy & Manage Properties"**

---

## Project Overview

RealEstateHub is a full-stack real estate management platform where users can explore properties, contact agents, and manage listings. Property agents can add and manage properties, while administrators oversee the entire system and manage users and content.

The mobile client will be built using **Kotlin Jetpack (Android)** and the backend will be powered by **TypeScript Express with Prisma ORM**.

---

## Roles & Permissions

| Role             | Description                     | Key Permissions                                                 |
| ---------------- | ------------------------------- | --------------------------------------------------------------- |
| **User (Buyer)** | People searching for properties | Browse properties, view details, save favorites, contact agents |
| **Agent**        | Property sellers or brokers     | Add properties, update listings, manage property status         |
| **Admin**        | Platform manager                | Manage users, properties, reports                               |

> 💡 **Note:** Admin accounts should be seeded in the database.

---

## Tech Stack

### Mobile App

* Kotlin
* Jetpack Compose / Jetpack Components
* Retrofit / Ktor Client
* Coil (Image Loading)

### Backend

* Node.js
* Express.js
* TypeScript
* Prisma ORM
* PostgreSQL (NeonDB)

### Other Tools

* JWT Authentication
* Cloudinary (Image Upload)
* Multer

---

## Features

### Public Features

* Browse all available properties
* Filter properties by location, price, and property type
* View property details
* Search properties

### User (Buyer) Features

* Register and login
* Save properties to wishlist
* Contact with verified agents
* View saved properties
* Manage user profile

### Agent Features

* Register and login as agent
* Add new property listings
* Manage their properties (edit/update)
* Delete properties
* Upload property images
* View their listed properties

### Admin Features

* View all users
* Manage users (activate/suspend)
* Manage all properties
* Delete inappropriate listings
* Manage property categories

---

## Pages & Routes

### Public Routes

| Route             | Page             | Description         |
| ----------------- | ---------------- | ------------------- |
| `/`               | Home             | Featured properties |
| `/properties`     | Properties       | List with filters   |
| `/properties/:id` | Property Details | Full property info  |
| `/agents/:id`     | Agent Profile    | Agent details       |
| `/login`          | Login            | Login form          |
| `/register`       | Register         | Registration form   |

---

### User Routes (Private)

| Route               | Page          | Description      |
| ------------------- | ------------- | ---------------- |
| `/favorites`        | Favorites     | Saved properties |
| `/profile`          | Profile       | User information |
| `/contact/:agentId` | Contact Agent | Send message     |

---

### Agent Routes (Private)

| Route                        | Page          | Description        |
| ---------------------------- | ------------- | ------------------ |
| `/agent/dashboard`           | Dashboard     | Agent stats        |
| `/agent/properties`          | My Properties | Manage listings    |
| `/agent/properties/new`      | Add Property  | Create new listing |
| `/agent/properties/:id/edit` | Edit Property | Update listing     |

---

### Admin Routes (Private)

| Route               | Page       | Description       |
| ------------------- | ---------- | ----------------- |
| `/admin`            | Dashboard  | Platform stats    |
| `/admin/users`      | Users      | Manage users      |
| `/admin/properties` | Properties | Manage properties |
| `/admin/categories` | Categories | Manage categories |

---

## Database Tables

Design your own schema for the following tables:

* **Users** - User authentication and profile data
* **Agents** - Additional information for agents
* **Properties** - Property listings
* **Wishlist** - Saved properties by users
* **WishlistItems** - Individual items in users' wishlists
* **BookedProperty** - Properties booked by users
* **SoldProperty** - Properties sold by users
* **Reviews** - User reviews for properties

> 💡 *Think about relationships between tables when designing your schema.*

---

## API Endpoints

### Authentication

| Method | Endpoint                | Description       |
| ------ | ----------------------- | ----------------- |
| POST   | `/api/v1/auth/register` | Register new user |
| POST   | `/api/v1/auth/login`    | Login user        |
| GET    | `/api/v1/auth/me`       | Get current user  |

---

### Properties (Public)

| Method | Endpoint                     | Description          | Authorization |
| ------ | ---------------------------- | -------------------- | ------------- |
| GET    | `/api/v1/property`           | Get all properties   | Public        |
| GET    | `/api/v1/property/:id`       | Get property details | Public        |
| GET    | `/api/v1/property/agent/:id` | Get all properties   | Agent         |
| POST   | `/api/v1/property`           | Create new property  | Agent         |
| PUT    | `/api/v1/property/:id`       | Update property      | Agent         |
| DELETE | `/api/v1/property/:id`       | Delete property      | Agent / Admin |


---

### Wishlist Items

| Method | Endpoint                                     | Description               | Authorization |
| ------ | -------------------------------------------- | ------------------------- | ------------- |
| GET    | `/api/v1/wishlist-item`                      | Get all wishlists         | Admin         |
| GET    | `/api/v1/wishlist-item/:id`                  | Get wishlist By Id        | User / Admin  |
| GET    | `/api/v1/wishlist-item/wishlist/:wishlistId` | Get user wishlist         | User          |
| POST   | `/api/v1/wishlist-item`                      | Add property to wishlist  | User          |
| DELETE | `/api/v1/wishlist-item/:id`                  | Remove item from wishlist | User          |

---

### Booked Properties

| Method | Endpoint                               | Description                  | Authorization |
| ------ | -------------------------------------- | ---------------------------- | ------------- |
| GET    | `/api/v1/booked-property`              | Get all booked properties    | Admin         |
| GET    | `/api/v1/booked-property/:id`          | Get booked property by ID    | User / Admin  |
| GET    | `/api/v1/booked-property/user/:userId` | Get user's booked properties | User          |
| POST   | `/api/v1/booked-property`              | Add property to booked list  | User          |
| DELETE | `/api/v1/booked-property/:id`          | Remove item from booked list | User / Admin  |

---

### Sold Properties

| Method | Endpoint                             | Description                | Authorization |
| ------ | ------------------------------------ | -------------------------- | ------------- |
| GET    | `/api/v1/sold-property`              | Get all sold properties    | Admin         |
| GET    | `/api/v1/sold-property/:id`          | Get sold property by ID    | User / Admin  |
| GET    | `/api/v1/sold-property/user/:userId` | Get user's sold properties | User          |
| POST   | `/api/v1/sold-property`              | Add property to sold list  | User          |
| DELETE | `/api/v1/sold-property/:id`          | Remove item from sold list | User / Admin  |

---

### Reviews

| Method | Endpoint                  | Description                 | Authorization |
| ------ | ------------------------- | --------------------------- | ------------- |
| GET    | `/api/v1/review`          | Get all reviews             | Admin         |
| GET    | `/api/v1/review/user/:id` | Get reviews by user ID      | Admin         |
| GET    | `/api/v1/review/:id`      | Get review by review ID     | User / Admin  |
| GET    | `/api/v1/property/:id`    | Get property by property ID | User / Admin  |
| POST   | `/api/v1/review`          | Add new review              | User          |
| PUT    | `/api/v1/review/:id`      | Update review               | User          |
| DELETE | `/api/v1/review/:id`      | Remove review               | User / Admin  |

---

## 📚 Additional Resources

- **[API Response](API_RESPONSE.md)** - Detailed endpoint documentation with request/response examples

## Future Improvements

* Property booking system
* In-app messaging
* Map integration
* Push notifications
* Payment integration

---

## License

This project is developed for **University Academic Purposes**.
