package com.example.pethub.data.repository

import com.example.pethub.data.local.database.dao.BookingDao
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.remote.FirestoreHelper
import com.example.pethub.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

// If BookingRepository is a class:
class BookingRepository @Inject constructor(
    private val firebaseService: FirebaseService,
    private val firestoreHelper: FirestoreHelper,
    private val dao: BookingDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {  }
