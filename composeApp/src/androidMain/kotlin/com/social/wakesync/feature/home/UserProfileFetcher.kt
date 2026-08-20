package com.social.wakesync.feature.home

import com.social.wakesync.FIRESTORE_DATABASE_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

actual fun fetchUserProfile(onResult: (name: String, imageUrl: String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    if (user != null) {
        val db = FirebaseFirestore.getInstance(FIRESTORE_DATABASE_ID)
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("authDisplayName") ?: document.getString("username") ?: "Jake"
                    val imageUrl = document.getString("profilePictureUrl") ?: ""
                    onResult(name, imageUrl)
                } else {
                    onResult("Jake", "")
                }
            }
            .addOnFailureListener {
                onResult("Jake", "")
            }
    } else {
        onResult("Jake", "")
    }
}
