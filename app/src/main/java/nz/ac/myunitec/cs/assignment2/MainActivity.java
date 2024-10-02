package nz.ac.myunitec.cs.assignment2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    PieChart pieChart;
    private LinearLayout addForm;
    private ImageView addButton;
    FirebaseFirestore db;
    final String TAG = "MainPage";
    RecyclerView rv;
    ExpenseRVAdapter adapter;
    ArrayList<Expense> expenseArrayList;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle system insets for better layout padding (top and bottom)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the expense list and Firestore instance
        expenseArrayList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and its adapter
        rv = findViewById(R.id.r_view_expense);
        pieChart = findViewById(R.id.pieChart);
        addButton = findViewById(R.id.add);

        // Initialize adapter and set to RecyclerView
        adapter = new ExpenseRVAdapter(MainActivity.this, expenseArrayList);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // Read data from Firestore and populate RecyclerView
        readDoc();



        // Set up SearchView to filter expenses based on input
        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterList(s);  // Filter list as user types
                return true;
            }
        });

        // Set up Add button click listener to open Add Activity
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Add.class);
                startActivity(intent);
            }
        });
    }

    // Method to filter expense list based on search query
    private void filterList(String text) {
        try {
            List<Expense> filteredList = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

            for (Expense expense_ : expenseArrayList) {
                // Format the date for comparison
                String formattedDate = dateFormat.format(expense_.getDate());

                // Check if the expense name, category, or formatted date contains the search text
                if (expense_.getName().toLowerCase().contains(text.toLowerCase()) ||
                        expense_.getCategory().toLowerCase().contains(text.toLowerCase()) ||
                        formattedDate.contains(text)) {
                    filteredList.add(expense_);
                }
            }

            // Update RecyclerView with the filtered list
            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No expenses found", Toast.LENGTH_SHORT).show();
            } else {
                adapter.setFilteredList(filteredList);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering the expense list", e);
        }
    }

    // Method to set up and display the PieChart based on expenses
    private void setupPieChart(ArrayList<Expense> expenseArrayList) {
        try {
            // Disable description for the PieChart
            pieChart.getDescription().setEnabled(false);

            // Check if there are any expenses to display
            if (expenseArrayList.isEmpty()) {
                pieChart.setCenterText("No data available");
                pieChart.setData(null);  // Clear chart data
                pieChart.invalidate();   // Refresh the chart
                return;
            }

            // Calculate total amount for each category
            HashMap<String, Float> categoryTotals = new HashMap<>();
            for (Expense expense : expenseArrayList) {
                String category = expense.getCategory();
                float amount = (float) expense.getAmount();

                // Sum the expenses for each category
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
            }

            // Prepare data for PieChart
            ArrayList<PieEntry> pieEntries = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            // Create a color mapping for each category
            HashMap<String, Integer> categoryColors = new HashMap<>();
            categoryColors.put("Shopping", Color.parseColor("#FFC6E094"));
            categoryColors.put("Commute", Color.parseColor("#FF94E0BB"));
            categoryColors.put("Food", Color.parseColor("#FF94BDE0"));
            categoryColors.put("Living", Color.parseColor("#FFE094B7"));
            categoryColors.put("Other", Color.parseColor("#FFF3B25D"));

            // Add entries and corresponding colors to the PieChart
            for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
                pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
                Integer color = categoryColors.get(entry.getKey());
                colors.add(color != null ? color : Color.GRAY);  // Default to gray if no color found
            }

            // Set up PieDataSet and apply custom colors
            PieDataSet dataSet = new PieDataSet(pieEntries, "Expense Categories");
            dataSet.setColors(colors);

            // Create PieData and apply to PieChart
            PieData data = new PieData(dataSet);
            pieChart.setData(data);
            pieChart.invalidate();  // Refresh the chart

            // Add animation
            pieChart.animateXY(1000, 1000);
            pieChart.setCenterText("Expense Percentage");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up PieChart", e);
        }
    }

    // Method to read data from Firestore and populate the RecyclerView
    public void readDoc() {
        try {
            db.collection("expenses")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                expenseArrayList.clear();  // Clear existing data to avoid duplicates
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    try {
                                        Expense expense = document.toObject(Expense.class);
                                        expense.setDocumentId(document.getId());  // Set documentId
                                        expenseArrayList.add(expense);  // Add data to list

                                    } catch (Exception e) {
                                        Log.e(TAG, "Error converting document to Expense", e);  // Catch conversion errors
                                    }
                                }
                                adapter.notifyDataSetChanged();  // Notify adapter that data has changed
                                setupPieChart(expenseArrayList);
                                Log.d(TAG, "Documents successfully read.");
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error reading documents from Firestore", e);
        }
    }
}
