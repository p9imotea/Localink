package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

public class TransactionMonitoring extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_monitoring);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout chat_container = findViewById(R.id.chat_container);

        ImageView backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("work_contracts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chat_container.removeAllViews();
                        Toast.makeText(TransactionMonitoring.this, "Number of transactions: " + task.getResult().size(), Toast.LENGTH_SHORT).show();
                        for (DocumentSnapshot document : task.getResult()) {
                            // Process each chat document here
                            // For example, you can log the document data


                            Log.d(TAG, document.getId() + " => " + document.getData());
//                            work_contract_list.add(document.getData());


                            View view = getLayoutInflater().inflate(R.layout.transaction_history, null, false);
                            TextView contractor_name = view.findViewById(R.id.name);
                            TextView job_title = view.findViewById(R.id.type_of_work);
                            TextView status = view.findViewById(R.id.status);
                            ImageView contractor_image = view.findViewById(R.id.image);

                            contractor_name.setText(document.getString("notesToContractor"));
                            job_title.setText("Transaction Id: "+document.getId());
                            status.setText(document.getString("status"));

                            view.setOnClickListener(v -> {
                                Intent intent = new Intent(TransactionMonitoring.this, ContractPreview.class);
                                intent.putExtra("contractId", document.getId());
                                startActivity(intent);
                            });

                            if (status.getText().toString().equals("Completed")) {
                                status.setTextColor(Color.GREEN);
                            } else {
                                status.setTextColor(Color.parseColor("#FFA500"));
                            }

                            chat_container.addView(view);

                        }
                    } else {
                        Log.w("Chats", "Error getting documents.", task.getException());
                    }
                });
    }
}