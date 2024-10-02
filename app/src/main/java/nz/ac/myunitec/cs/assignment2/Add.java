package nz.ac.myunitec.cs.assignment2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Add extends AppCompatActivity {
    ImageView btnPrevious, btnNext;
    EditText name, amount, category;
    LinearLayout catShop, catCommute, catFood, catLiving, catOther;
    TextView btnAdd, btnCancel, tvDate;
    FirebaseFirestore db;
    final String TAG = "AddExpense";
    String documentId;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);
        db = FirebaseFirestore.getInstance();

        // Adjust layout for system bars (top, bottom, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        name = findViewById(R.id.name);
        amount = findViewById(R.id.amount);
        category = findViewById(R.id.cat);
        catShop = findViewById(R.id.cat_shop);
        catCommute = findViewById(R.id.cat_commute);
        catFood = findViewById(R.id.cat_food);
        catLiving = findViewById(R.id.cat_living);
        catOther = findViewById(R.id.cat_other);
        btnAdd = findViewById(R.id.btn_add);
        btnCancel = findViewById(R.id.btn_cnecel);
        tvDate = findViewById(R.id.tv_date);

        // Get document ID from Intent to either update or add new record
        documentId = getIntent().getStringExtra("documentId");

        // Load existing data if document ID exists, otherwise set the current date
        if (documentId != null && !documentId.isEmpty()) {
            btnAdd.setText("Update");
            loadExpenseData();
        } else {
            setDate(null);
        }

        // Navigate to previous day
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Add.this, "Previous clicked", Toast.LENGTH_SHORT).show();
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                setDate(calendar.getTime());  // Update date display
            }
        });

        // Navigate to next day
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Add.this, "Next clicked", Toast.LENGTH_SHORT).show();
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                setDate(calendar.getTime());  // Update date display
            }
        });

        // Cancel and return to the main screen
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Add.this, "Form cleared", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Add.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Category selection logic
        catShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetOtherCategories();
                catShop.setBackgroundResource(R.drawable.background_selected_item);
                category.setText("Shopping");
                Toast.makeText(Add.this, "Category: Shopping", Toast.LENGTH_SHORT).show();
            }
        });

        catCommute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetOtherCategories();
                catCommute.setBackgroundResource(R.drawable.background_selected_item);
                category.setText("Commute");
                Toast.makeText(Add.this, "Category: Commute", Toast.LENGTH_SHORT).show();
            }
        });

        catFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetOtherCategories();
                catFood.setBackgroundResource(R.drawable.background_selected_item);
                category.setText("Food");
                Toast.makeText(Add.this, "Category: Food", Toast.LENGTH_SHORT).show();
            }
        });

        catLiving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetOtherCategories();
                catLiving.setBackgroundResource(R.drawable.background_selected_item);
                category.setText("Living");
                Toast.makeText(Add.this, "Category: Living", Toast.LENGTH_SHORT).show();
            }
        });

        catOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetOtherCategories();
                catOther.setBackgroundResource(R.drawable.background_selected_item);
                category.setText("Other");
                Toast.makeText(Add.this, "Category: Other", Toast.LENGTH_SHORT).show();
            }
        });

        // OnClick method for adding or updating expense
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Validate name input
                    String enteredName = name.getText().toString();
                    if (enteredName == null || enteredName.trim().isEmpty()) {
                        Toast.makeText(Add.this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validate amount input
                    String amountText = amount.getText().toString();
                    Double enteredAmount;
                    try {
                        enteredAmount = Double.parseDouble(amountText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(Add.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validate category input
                    String enteredCategory = category.getText().toString();
                    if (enteredCategory == null || enteredCategory.trim().isEmpty()) {
                        Toast.makeText(Add.this, "Please select a category", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Date currentDate = calendar.getTime();

                    // Create expense data map
                    Map<String, Object> expense = new HashMap<>();
                    expense.put("name", enteredName);
                    expense.put("amount", enteredAmount);
                    expense.put("category", enteredCategory);
                    expense.put("date", currentDate);

                    // Check if updating an existing document or adding a new one
                    if (documentId != null && !documentId.isEmpty()) {
                        // Update existing document
                        db.collection("expenses").document(documentId)
                                .update(expense)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    Toast.makeText(Add.this, "Expense Updated Successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Add.this, MainActivity.class);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
                    } else {
                        // Add new document
                        db.collection("expenses")
                                .add(expense)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    Toast.makeText(Add.this, "Expense Added Successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Add.this, MainActivity.class);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> Log.d(TAG, "Error adding document: " + e.getMessage()));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error occurred while processing Firestore operation", e);
                }
            }
        });
    }

    // Method to set the date on the TextView
    private void setDate(Date date) {
        try {
            if (date == null) {
                calendar = Calendar.getInstance();  // Use current date if no date is provided
            } else {
                calendar = Calendar.getInstance();
                calendar.setTime(date);  // Set the provided date
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);  // Month is 0-based, so add 1 for display
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            tvDate.setText(day + "/" + (month + 1) + "/" + year);  // Update TextView with formatted date
        } catch (Exception e) {
            Log.e(TAG, "Error setting date", e);
        }
    }

    // Method to load expense data for updating
    private void loadExpenseData() {
        try {
            db.collection("expenses").document(documentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Expense expense = documentSnapshot.toObject(Expense.class);
                            if (expense != null) {
                                name.setText(expense.getName());
                                amount.setText(String.valueOf(expense.getAmount()));
                                category.setText(expense.getCategory());
                                selectCategory(expense.getCategory());
                                setDate(expense.getDate());
                            }
                        } else {
                            Toast.makeText(Add.this, "No such document", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error loading document", e));
        } catch (Exception e) {
            Log.e(TAG, "Error loading expense data", e);
        }
    }

    // Reset background for all category selections
    private void resetOtherCategories() {
        try {
            catShop.setBackgroundResource(android.R.color.transparent);
            catCommute.setBackgroundResource(android.R.color.transparent);
            catFood.setBackgroundResource(android.R.color.transparent);
            catLiving.setBackgroundResource(android.R.color.transparent);
            catOther.setBackgroundResource(android.R.color.transparent);
        } catch (Exception e) {
            Log.e(TAG, "Error resetting category backgrounds", e);
        }
    }

    // Highlight selected category
    private void selectCategory(String category) {
        resetOtherCategories();
        switch (category) {
            case "Food":
                catFood.setBackgroundResource(R.drawable.background_selected_item);
                break;
            case "Commute":
                catCommute.setBackgroundResource(R.drawable.background_selected_item);
                break;
            case "Living":
                catLiving.setBackgroundResource(R.drawable.background_selected_item);
                break;
            case "Shopping":
                catShop.setBackgroundResource(R.drawable.background_selected_item);
                break;
            default:
                catOther.setBackgroundResource(R.drawable.background_selected_item);
                break;
        }
    }
}
