package com.example.pethub.data.remote

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for Firestore database operations
 * Provides generic CRUD operations and specific collection queries
 */
@Singleton
class FirestoreHelper @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // ============================================
    // COLLECTION REFERENCES
    // ============================================

    companion object {
        // Collection names
        const val COLLECTION_CUSTOMER = "customer"
        const val COLLECTION_BRANCH = "branch"
        const val COLLECTION_PET = "pet"
        const val COLLECTION_SERVICE = "service"
        const val COLLECTION_APPOINTMENT = "appointment"
        const val COLLECTION_NOTIFICATION = "notification"


        // Common fields
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"
        const val FIELD_USER_ID = "userId"
        const val FIELD_IS_ACTIVE = "isActive"
    }

    // ============================================
    // GENERIC CRUD OPERATIONS
    // ============================================

    /**
     * Create a new document with auto-generated ID
     */
    suspend fun <T : Any> createDocument(
        collection: String,
        data: T
    ): Result<String> {
        return try {
            val docRef = firestore.collection(collection).document()
            docRef.set(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new document with specific ID
     */
    suspend fun <T : Any> createDocumentWithId(
        collection: String,
        documentId: String,
        data: T
    ): Result<Unit> {
        return try {
            firestore.collection(collection)
                .document(documentId)
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a single document by ID
     */
    suspend fun <T : Any> getDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Result<T?> {
        return try {
            val snapshot = firestore.collection(collection)
                .document(documentId)
                .get()
                .await()

            val data = snapshot.toObject(clazz)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get a specific field from a document
    suspend fun <T : Any> getDocumentField(
        collection: String,
        documentId: String,
        fieldName: String,
        fieldClass: Class<T>
    ): Result<T?> {
        return try {
            val snapshot = firestore.collection(collection)
                .document(documentId)
                .get()
                .await()

            val value = snapshot.get(fieldName, fieldClass)
            Result.success(value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    /**
     * Get document snapshot (includes ID)
     */
    suspend fun getDocumentSnapshot(
        collection: String,
        documentId: String
    ): Result<DocumentSnapshot?> {
        return try {
            val snapshot = firestore.collection(collection)
                .document(documentId)
                .get()
                .await()
            Result.success(snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update a document
     */
    suspend fun updateDocument(
        collection: String,
        documentId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            val updatesWithTimestamp = updates.toMutableMap().apply {
                put(FIELD_UPDATED_AT, FieldValue.serverTimestamp())
            }

            firestore.collection(collection)
                .document(documentId)
                .update(updatesWithTimestamp)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Set/Replace a document (merge option available)
     */
    suspend fun <T : Any> setDocument(
        collection: String,
        documentId: String,
        data: T,
        merge: Boolean = false
    ): Result<Unit> {
        return try {
            val docRef = firestore.collection(collection).document(documentId)

            // If merge is true, use SetOptions.merge()
            // If merge is false, call set(data)
            if (merge) {
                docRef.set(data, SetOptions.merge()).await()
            } else {
                docRef.set(data).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a document
     */
    suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): Result<Unit> {
        return try {
            firestore.collection(collection)
                .document(documentId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if document exists
     */
    suspend fun documentExists(
        collection: String,
        documentId: String
    ): Result<Boolean> {
        return try {
            val snapshot = firestore.collection(collection)
                .document(documentId)
                .get()
                .await()
            Result.success(snapshot.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // QUERY OPERATIONS
    // ============================================

    /**
     * Get all documents from a collection
     */
    suspend fun <T : Any> getAllDocuments(
        collection: String,
        clazz: Class<T>
    ): Result<List<T>> {
        return try {
            val snapshot = firestore.collection(collection)
                .get()
                .await()

            val documents = snapshot.documents.mapNotNull { it.toObject(clazz) }
            Result.success(documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Query documents with where clause
     */
    suspend fun <T : Any> queryDocuments(
        collection: String,
        field: String,
        value: Any,
        clazz: Class<T>
    ): Result<List<T>> {
        return try {
            val snapshot = firestore.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .await()

            val documents = snapshot.documents.mapNotNull { it.toObject(clazz) }
            Result.success(documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Query documents with custom query builder
     */
    suspend fun <T : Any> queryWithBuilder(
        collection: String,
        clazz: Class<T>,
        queryBuilder: (Query) -> Query
    ): Result<List<T>> {
        return try {
            val baseQuery = firestore.collection(collection)
            val finalQuery = queryBuilder(baseQuery)

            val snapshot = finalQuery.get().await()
            val documents = snapshot.documents.mapNotNull { it.toObject(clazz) }
            Result.success(documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get documents ordered by field
     */
    suspend fun <T : Any> getDocumentsOrdered(
        collection: String,
        orderByField: String,
        descending: Boolean = false,
        limit: Long? = null,
        clazz: Class<T>
    ): Result<List<T>> {
        return try {
            var query: Query = firestore.collection(collection)
                .orderBy(orderByField, if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING)

            limit?.let { query = query.limit(it) }

            val snapshot = query.get().await()
            val documents = snapshot.documents.mapNotNull { it.toObject(clazz) }
            Result.success(documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // REALTIME LISTENERS
    // ============================================

    /**
     * Listen to a single document in real-time
     */
    fun <T : Any> listenToDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Flow<T?> = callbackFlow {
        val listenerRegistration = firestore.collection(collection)
            .document(documentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val data = snapshot?.toObject(clazz)
                trySend(data)
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Listen to collection changes in real-time
     */
    fun <T : Any> listenToCollection(
        collection: String,
        clazz: Class<T>,
        queryBuilder: ((Query) -> Query)? = null
    ): Flow<List<T>> = callbackFlow {
        val baseQuery = firestore.collection(collection)
        val finalQuery = queryBuilder?.invoke(baseQuery) ?: baseQuery

        val listenerRegistration = finalQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val documents = snapshot?.documents?.mapNotNull { it.toObject(clazz) } ?: emptyList()
            trySend(documents)
        }

        awaitClose { listenerRegistration.remove() }
    }

    // ============================================
    // BATCH OPERATIONS
    // ============================================

    /**
     * Perform batch write operations
     */
    suspend fun batchWrite(operations: (WriteBatch) -> Unit): Result<Unit> {
        return try {
            val batch = firestore.batch()
            operations(batch)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Batch delete documents
     */
    suspend fun batchDelete(
        collection: String,
        documentIds: List<String>
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()
            documentIds.forEach { id ->
                val docRef = firestore.collection(collection).document(id)
                batch.delete(docRef)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // SUBCOLLECTION OPERATIONS
    // ============================================

    /**
     * Create document in subcollection
     */
    suspend fun <T : Any> createSubcollectionDocument(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        data: T
    ): Result<String> {
        return try {
            val docRef = firestore.collection(parentCollection)
                .document(parentId)
                .collection(subcollection)
                .document()

            docRef.set(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get documents from subcollection
     */
    suspend fun <T : Any> getSubcollectionDocuments(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        clazz: Class<T>
    ): Result<List<T>> {
        return try {
            val snapshot = firestore.collection(parentCollection)
                .document(parentId)
                .collection(subcollection)
                .get()
                .await()

            val documents = snapshot.documents.mapNotNull { it.toObject(clazz) }
            Result.success(documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen to subcollection
     */
    fun <T : Any> listenToSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        clazz: Class<T>
    ): Flow<List<T>> = callbackFlow {
        val listenerRegistration = firestore.collection(parentCollection)
            .document(parentId)
            .collection(subcollection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val documents = snapshot?.documents?.mapNotNull { it.toObject(clazz) } ?: emptyList()
                trySend(documents)
            }

        awaitClose { listenerRegistration.remove() }
    }

    // ============================================
    // HELPER FUNCTIONS
    // ============================================

    /**
     * Get document reference
     */
    fun getDocumentReference(collection: String, documentId: String): DocumentReference {
        return firestore.collection(collection).document(documentId)
    }

    /**
     * Get server timestamp
     */
    fun getServerTimestamp(): Any = FieldValue.serverTimestamp()

    /**
     * Increment field value
     */
    fun incrementValue(value: Long = 1): Any = FieldValue.increment(value)

    /**
     * Decrement field value
     */
    fun decrementValue(value: Long = 1): Any = FieldValue.increment(-value)

    /**
     * Array union (add elements)
     */
    fun arrayUnion(vararg elements: Any): Any = FieldValue.arrayUnion(*elements)

    /**
     * Array remove (remove elements)
     */
    fun arrayRemove(vararg elements: Any): Any = FieldValue.arrayRemove(*elements)

    /**
     * Delete field
     */
    fun deleteField(): Any = FieldValue.delete()

    // ============================================
    // TRANSACTION SUPPORT
    // ============================================

    /**
     * Run a Firestore transaction
     */
    suspend fun <T> runTransaction(transaction: suspend (com.google.firebase.firestore.Transaction) -> T): Result<T> {
        return try {
            val result = firestore.runTransaction { firestoreTransaction ->
                // Note: This is a blocking call, but wrapped in a suspend function
                // The actual transaction logic should be passed as a suspend function
                null as T // Placeholder - actual implementation depends on use case
            }.await()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // PAGINATION SUPPORT
    // ============================================

    /**
     * Get paginated documents
     */
    suspend fun <T : Any> getPaginatedDocuments(
        collection: String,
        orderByField: String,
        pageSize: Long,
        lastDocument: DocumentSnapshot? = null,
        clazz: Class<T>
    ): Result<Pair<List<T>, DocumentSnapshot?>> {
        return try {
            var query: Query = firestore.collection(collection)
                .orderBy(orderByField)
                .limit(pageSize)

            lastDocument?.let {
                query = query.startAfter(it)
            }

            val snapshot = query.get().await()
            val documents = snapshot.documents.mapNotNull { it.toObject(clazz) }
            val lastDoc = snapshot.documents.lastOrNull()

            Result.success(Pair(documents, lastDoc))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // COUNTING
    // ============================================

    /**
     * Count documents in collection (requires aggregation query)
     */
    suspend fun countDocuments(
        collection: String,
        whereField: String? = null,
        whereValue: Any? = null
    ): Result<Long> {
        return try {
            var query: Query = firestore.collection(collection)

            if (whereField != null && whereValue != null) {
                query = query.whereEqualTo(whereField, whereValue)
            }

            val snapshot = query.get().await()
            Result.success(snapshot.size().toLong())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}