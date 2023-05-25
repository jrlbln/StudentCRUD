package com.application.student;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private EditText addStudentNameEditText;
    private EditText addStudentYASEditText;
    private EditText addStudentNumberEditText;
    private Button saveButton;

    private EditText searchStudentIdEditText;
    private Button searchButton;

    private TextView updateStudentIdEditText;
    private EditText updateStudentNameEditText;
    private EditText updateStudentYASEditText;
    private EditText updateStudentNumberEditText;
    private Button updateButton;

    private EditText deleteStudentIdEditText;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        addStudentNameEditText = findViewById(R.id.addStudentName);
        addStudentYASEditText = findViewById(R.id.addStudentYAS);
        addStudentNumberEditText = findViewById(R.id.addStudentNumber);
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> addStudent());

        searchStudentIdEditText = findViewById(R.id.searchStudentId);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view -> searchStudent());

        updateStudentIdEditText = findViewById(R.id.updateStudentId);
        updateStudentNameEditText = findViewById(R.id.updateStudentName);
        updateStudentYASEditText = findViewById(R.id.updateStudentYAS);
        updateStudentNumberEditText = findViewById(R.id.updateStudentNumber);
        updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(view -> updateStudent());

        deleteStudentIdEditText = findViewById(R.id.deleteStudentId);
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(view -> deleteStudent());
    }

    private void addStudent() {
        String name = addStudentNameEditText.getText().toString();
        String yas = addStudentYASEditText.getText().toString();
        String studentNumber = addStudentNumberEditText.getText().toString();

        // Query Firestore to find the occupied document slots
        db.collection("students")
                .orderBy("ID", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int newDocumentSlot = 1;

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        int documentId = document.getLong("ID").intValue();
                        if (documentId == newDocumentSlot) {
                            // The current slot is occupied, move to the next slot
                            newDocumentSlot++;
                        } else if (documentId > newDocumentSlot) {
                            // Found an available slot before the current document, break the loop
                            break;
                        }
                    }

                    Map<String, Object> studentData = new HashMap<>();
                    studentData.put("ID", newDocumentSlot); // Set the ID field
                    studentData.put("Name", name);
                    studentData.put("Year and Section", yas);
                    studentData.put("Student Number", studentNumber);

                    String documentName = String.valueOf(newDocumentSlot); // Use the new document slot as the document name

                    db.collection("students")
                            .document(documentName)
                            .set(studentData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Student added with ID: " + documentName);
                                addStudentNameEditText.setText(""); // Clear the input field
                                addStudentYASEditText.setText(""); // Clear the input field
                                addStudentNumberEditText.setText(""); // Clear the input field
                                showMessage("Added Successfully"); // Display success message
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error adding student", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying documents", e);
                });
    }

    private void searchStudent() {
        String studentId = searchStudentIdEditText.getText().toString();

        // Convert the studentId to an integer
        int id = Integer.parseInt(studentId);

        db.collection("students")
                .whereEqualTo("ID", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Student document found");
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String documentId = documentSnapshot.getId();
                            String name = documentSnapshot.getString("Name");
                            String yearAndSection = documentSnapshot.getString("Year and Section");
                            String studentNumber = documentSnapshot.getString("Student Number");

                            // Set the retrieved data in the update fields
                            updateStudentIdEditText.setText("ID: " + id);
                            updateStudentNameEditText.setText(name);
                            updateStudentYASEditText.setText(yearAndSection);
                            updateStudentNumberEditText.setText(studentNumber);
                        }
                    } else {
                        Log.d(TAG, "Student document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student document", e);
                });
    }

    private void updateStudent() {
        String studentId = updateStudentIdEditText.getText().toString();

        // Extract the numeric part of the ID by removing the "ID: " prefix
        String numericId = studentId.replace("ID: ", "");

        // Convert the numericId to an integer
        int id = Integer.parseInt(numericId);

        String updatedName = updateStudentNameEditText.getText().toString();
        String updatedYAS = updateStudentYASEditText.getText().toString();
        String updatedStudentNumber = updateStudentNumberEditText.getText().toString();

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("ID", id); // Update the ID field
        updatedData.put("Name", updatedName);
        updatedData.put("Year and Section", updatedYAS);
        updatedData.put("Student Number", updatedStudentNumber);

        db.collection("students")
                .whereEqualTo("ID", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String documentId = documentSnapshot.getId();

                            db.collection("students")
                                    .document(documentId)
                                    .set(updatedData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Student document updated successfully");
                                        showMessage("Updated Successfully"); // Display success message

                                        // Clear the input fields
                                        updateStudentIdEditText.setText("");
                                        updateStudentNameEditText.setText("");
                                        updateStudentYASEditText.setText("");
                                        updateStudentNumberEditText.setText("");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error updating student document", e);
                                    });
                        }
                    } else {
                        Log.d(TAG, "No student document found with ID: " + id);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student document", e);
                });
    }

    private void deleteStudent() {
        String studentId = deleteStudentIdEditText.getText().toString();

        db.collection("students")
                .whereEqualTo("ID", Integer.parseInt(studentId))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String documentId = documentSnapshot.getId();

                            db.collection("students")
                                    .document(documentId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Student document deleted successfully");
                                        showMessage("Deleted Successfully"); // Display success message
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error deleting student document", e);
                                    });
                        }
                    } else {
                        Log.d(TAG, "No student document found with ID: " + studentId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving student document", e);
                });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
