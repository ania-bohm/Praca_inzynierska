package com.annabohm.pracainzynierska;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthInstanceSingleton {
    private static FirebaseAuthInstanceSingleton INSTANCE;
    private final FirebaseAuth firebaseAuthRef;

    private FirebaseAuthInstanceSingleton() {
        this.firebaseAuthRef = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseAuthInstanceSingleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FirebaseAuthInstanceSingleton();
        }

        return INSTANCE;
    }

    public FirebaseAuth getFirebaseAuthRef() {
        return firebaseAuthRef;
    }
}

