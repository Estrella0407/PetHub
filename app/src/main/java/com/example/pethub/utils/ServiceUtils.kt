package com.example.pethub.utils

/**
 * Validates if a service is suitable for a specific pet type based on its name.
 */
fun isServiceSuitableForPet(serviceName: String, petType: String): Boolean {
    val lowerServiceName = serviceName.lowercase()
    val lowerPetType = petType.lowercase()

    // Define keywords for known animal types
    val dogKeywords = listOf("dog", "puppy", "canine")
    val catKeywords = listOf("cat", "kitten", "feline")

    // Determine the inferred type of the service
    val isDogService = dogKeywords.any { lowerServiceName.contains(it) }
    val isCatService = catKeywords.any { lowerServiceName.contains(it) }

    // Logic:
    // 1. If it's a "general" service (no specific animal keyword), it's suitable for everyone.
    // 2. If it is a dog service, it's only suitable if the pet is a dog.
    // 3. If it is a cat service, it's only suitable if the pet is a cat.
    
    return when {
        isDogService -> lowerPetType == "dog"
        isCatService -> lowerPetType == "cat"
        else -> true // General services are suitable for all
    }
}
