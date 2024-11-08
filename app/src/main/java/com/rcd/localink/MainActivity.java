package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Import Toast class

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Declare your views here
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signUpButton;
    private Button signup_google;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView terms_and_condition = findViewById(R.id.terms_and_condition);
        terms_and_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TermsAndConditions.class);
                startActivity(intent);
            }
        });

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        signUpButton = findViewById(R.id.sign_up);
        signup_google = findViewById(R.id.signup_google);


        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT);

                    } else {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    }
                    return true;
                }
            }
            return false;
        });

        signup_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize Google Sign-in
                Toast.makeText(MainActivity.this, "Google Sign-in is upcoming!", Toast.LENGTH_SHORT).show();
            }
        });

        // Redirect to dashboard if already logged in
        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String token = sharedPrefs.getString("token", "");

        if (!token.isEmpty()) {
            // go to Dashboard Activity
            Intent intent = new Intent(MainActivity.this, UploadDocuments.class);
            startActivity(intent);
            finish();
        }


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // go to Signup Activity
                Intent intent = new Intent(MainActivity.this, Signup.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text from email and password EditText
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                loginButton.setEnabled(false);
                loginButton.setText("Loading ...");

                // Search a document on Firestore with collection "users" where username and password match
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                    .whereEqualTo("email", email)
                    .whereEqualTo("password", password)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Get the data from the document
                                String userData = document.getData().toString();
                                // Use the userData as needed
                                String firstName = document.getString("firstName");

                                String documentId = document.getId();

                                // Generate a random token
                                String token = UUID.randomUUID().toString();

                                // Update the document with the token
                                document.getReference().update("token", token);



                                // Store the user data in SharedPreferences
                                SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                editor.putString("documentId", documentId);
                                for (Map.Entry<String, Object> entry : document.getData().entrySet()) {
                                    editor.putString(entry.getKey(), String.valueOf(entry.getValue()));
                                    Log.d(TAG, "Key: " + entry.getKey() + ", Value: " + String.valueOf(entry.getValue()));
                                }
                                editor.putString("token", token);
                                editor.commit();



                                // Log the SharedPreference userData
                                SharedPreferences sharedPrefs2 = getSharedPreferences("userAuth", MODE_PRIVATE);
                                Log.d("SharedPreference", sharedPrefs2.getAll().toString());

                                Toast.makeText(MainActivity.this, "Welcome " + firstName + "!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(MainActivity.this, UploadDocuments.class);
                                startActivity(intent);
                                finish();

                                loginButton.setEnabled(true);
                                loginButton.setText("LOGIN");
                            }

                            if(task.getResult().isEmpty()){
                                Toast.makeText(MainActivity.this, "There is no email and password match", Toast.LENGTH_SHORT).show();
                                loginButton.setEnabled(true);
                                loginButton.setText("LOGIN");
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            loginButton.setEnabled(true);
                            loginButton.setText("LOGIN");
                        }
                    });

            }
        });
    }
}