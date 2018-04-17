package com.squad.stopby;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RegistrationActivity extends AppCompatActivity {

    private EditText register_username;
    private EditText register_email;
    private EditText register_password;
    private EditText register_userInfo;
    private Button registrationBtn;

    private FirebaseAuth mAuth;
    private Database db;
    private DatabaseReference profileDatabase;
    private Profile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        register_username = (EditText) findViewById(R.id.register_username);
        register_email = (EditText) findViewById(R.id.register_email);
        register_password = (EditText) findViewById(R.id.register_password);
        register_userInfo = (EditText) findViewById(R.id.register_userInfo);
        registrationBtn = (Button) findViewById(R.id.register_registerBtn);

        mAuth = FirebaseAuth.getInstance();
        db = new Database();
        profileDatabase = db.getDatabaseReference().child("user profile");

        registrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String username = register_username.getText().toString();
                    String email = register_email.getText().toString();
                    String rawPassword = register_password.getText().toString();
                    String password = encrypt(register_password.getText().toString());
                    String userInfo = register_userInfo.getText().toString();
                    userProfile = new Profile(username, email, password, userInfo);
                    registerUsers(email, rawPassword);

                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void registerUsers(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            profileDatabase.child(currentUser.getUid()).setValue(userProfile);
                            //update UI
                            Intent intent = new Intent(RegistrationActivity.this, MenuActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(RegistrationActivity.this, "Registration is successful.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegistrationActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Encrypt the password
    public String encrypt(String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] bytesArray = cipher.doFinal(password.getBytes());
        String encryptedVal = Base64.encodeToString(bytesArray, Base64.DEFAULT);
        return encryptedVal;
    }

    private SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }
}
