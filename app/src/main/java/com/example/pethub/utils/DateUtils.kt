package com.example.pethub.util

import java.util.Calendar
import java.util.Date

fun calculateAge(dateOfBirth: Long?): Int? {
    if (dateOfBirth == null) return null

    val dob = Calendar.getInstance()
    dob.timeInMillis = dateOfBirth

    val today = Calendar.getInstance()

    var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

    // If the birth day of the year has not occurred yet this year, subtract one year.
    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
        age--
    }

    return age
}
