package com.example.pethub.utils

import com.example.pethub.R

/**
 * Service Icons
 */
fun getServiceIcon(category: String): Int {
    return when (category.lowercase()) {
        "grooming" -> R.drawable.grooming_nobg
        "boarding" -> R.drawable.boarding_nobg
        "training" -> R.drawable.training_nobg
        "walking" -> R.drawable.walking_nobg
        "daycare" -> R.drawable.daycare_nobg
        else -> R.drawable.grooming_nobg
    }
}