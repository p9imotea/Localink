package com.rcd.localink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddGigWork extends AppCompatActivity {

    ImageView backButton;
    ImageView profileImage;
    TextView jobPostDetailsText;
    EditText jobPostTitle;
    EditText jobPostDescription;
    EditText jobPostLocation;
    EditText jobPostRate;
    EditText jobPostRequiredHours;
    CheckBox activeCheckBox;
    Button publishButton;
    Spinner job_type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_gig_work);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize all views with @+id/ identifiers
        backButton = findViewById(R.id.back_button);
        profileImage = findViewById(R.id.profile_image);
        jobPostTitle = findViewById(R.id.job_post_title);
        jobPostDescription = findViewById(R.id.job_post_description);
        jobPostLocation = findViewById(R.id.job_post_location);
        jobPostRate = findViewById(R.id.job_post_rate);
        jobPostRequiredHours = findViewById(R.id.job_post_required_hours);
        activeCheckBox = findViewById(R.id.active);
        publishButton = findViewById(R.id.publish);
        job_type = findViewById(R.id.job_type);

        publishButton.setOnClickListener(v -> {
            saveJobPost();
        });

        ImageView backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(view -> {
            finish();
        });
    }

    // Function to save job post data
    private void saveJobPost() {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Define collection reference
        CollectionReference jobPostingsRef = db.collection("job_postings");

        // Retrieve input values
        String title = jobPostTitle.getText().toString().trim();
        String description = jobPostDescription.getText().toString().trim();
        String location = jobPostLocation.getText().toString().trim();
        double rate = Double.parseDouble(jobPostRate.getText().toString().trim());
        int requiredHours = Integer.parseInt(jobPostRequiredHours.getText().toString().trim());
        boolean isActive = activeCheckBox.isChecked();
        String job_type_value = job_type.getSelectedItem().toString();

        // Add additional fields
        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String employerId = sharedPrefs.getString("documentId", "");
        String employerName = sharedPrefs.getString("firstName", "") + " " +
                sharedPrefs.getString("middleName", "") + " " +
                sharedPrefs.getString("lastName", "");
        String employerProfilePic = sharedPrefs.getString("profile_picture", "");
        FieldValue dateAdded = FieldValue.serverTimestamp();

        // Create a map to store job post data
        Map<String, Object> jobPost = new HashMap<>();
        jobPost.put("title", title);
        jobPost.put("description", description);
        jobPost.put("location", location);
        jobPost.put("rate", rate);
        jobPost.put("required_hours", requiredHours);
        jobPost.put("is_active", isActive);
        jobPost.put("employer_id", employerId);
        jobPost.put("employer_name", employerName);
        jobPost.put("job_type", job_type_value);
        jobPost.put("employer_profile_pic", employerProfilePic);
        jobPost.put("date_added", dateAdded);
        jobPost.put("ratings", new ArrayList<>()); // Empty ratings array


        // Check if job post with employer_id already exists
        jobPostingsRef.whereEqualTo("employer_id", employerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && querySnapshot.size() > 0) {
                            Toast.makeText(this, "You can only have one active job posting!", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        } else {
                            // Add job post to Firestore
                            jobPostingsRef.add(jobPost)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, "Job post saved successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(this, GigWork.class);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error saving job post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                });


    }
}