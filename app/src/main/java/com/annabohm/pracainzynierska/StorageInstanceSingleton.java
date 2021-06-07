package com.annabohm.pracainzynierska;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class StorageInstanceSingleton {
    private static StorageInstanceSingleton INSTANCE;
    private final FirebaseStorage firebaseStorageRef;

    private StorageInstanceSingleton() {
        this.firebaseStorageRef = FirebaseStorage.getInstance();
    }

    public static synchronized StorageInstanceSingleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StorageInstanceSingleton();
        }

        return INSTANCE;
    }

    public FirebaseStorage getFirebaseStorageRef() {
        return firebaseStorageRef;
    }
}

