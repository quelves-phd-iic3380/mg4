package edu.puc.iic3380.mg4.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static edu.puc.iic3380.mg4.util.Constantes.FIREBASE_KEY_USERS;

/**
 * Created by quelves on 09/11/2016.
 */

public class UserDAO {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference usersRef;

    private static UserDAO instance;

    private UserDAO() {


    }

    public static UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }

        return instance;
    }
}
