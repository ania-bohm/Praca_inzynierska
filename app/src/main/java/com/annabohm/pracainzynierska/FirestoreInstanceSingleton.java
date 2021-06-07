package com.annabohm.pracainzynierska;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreInstanceSingleton {
    private static FirestoreInstanceSingleton INSTANCE;
    private final FirebaseFirestore firebaseFirestoreRef;

    private FirestoreInstanceSingleton() {
        this.firebaseFirestoreRef = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreInstanceSingleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FirestoreInstanceSingleton();
        }

        return INSTANCE;
    }

    public FirebaseFirestore getFirebaseFirestoreRef() {
        return firebaseFirestoreRef;
    }
}

