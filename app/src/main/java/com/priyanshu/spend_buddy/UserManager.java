package com.priyanshu.spend_buddy;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserManager {

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private static final String uid =
            FirebaseAuth.getInstance().getCurrentUser().getUid();

    private static final DocumentReference userRef =
            db.collection("users").document(uid);

    // ===================== ADD LEAVES =====================

    public static void addLeaves(int amount){

        userRef.get().addOnSuccessListener(document -> {

            Long leaves = document.getLong("leaves");

            if(leaves == null)
                leaves = 0L;

            leaves += amount;

            long level = calculateLevel(leaves);

            Map<String,Object> update = new HashMap<>();

            update.put("leaves", leaves);

            update.put("level", level);

            userRef.update(update);

        });

    }

    // ===================== REMOVE LEAVES =====================

    public static void removeLeaves(int amount){

        userRef.get().addOnSuccessListener(document -> {

            Long leaves = document.getLong("leaves");

            if(leaves == null)
                leaves = 0L;

            leaves -= amount;

            if(leaves < 0)
                leaves = 0L;

            long level = calculateLevel(leaves);

            Map<String,Object> update = new HashMap<>();

            update.put("leaves", leaves);

            update.put("level", level);

            userRef.update(update);

        });

    }

    // ===================== LEVEL =====================

    private static long calculateLevel(long leaves){

        return (leaves / 100) + 1;

    }

}