package nz.ac.myunitec.cs.assignment2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class ExpenseRVAdapter extends RecyclerView.Adapter<ExpenseRVHolder> {
    private List<Expense> expenses;
    private Context context;
    private FirebaseFirestore db;
    final String TAG = "ExpenseRVAdapter";

    // Constructor for ExpenseRVAdapter
    public ExpenseRVAdapter(Context context, List<Expense> expenses) {
        this.context = context;
        this.expenses = expenses;
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    // Create ViewHolder for each expense item
    @NonNull
    @Override
    public ExpenseRVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_expense, parent, false);
        return new ExpenseRVHolder(view);
    }

    // Bind data to each ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ExpenseRVHolder holder, int position) {
        Expense expense = expenses.get(position);
        String documentId = expense.getDocumentId(); // Retrieve document ID

        try {
            // Set the category icon based on the category name
            if (expense.getCategory().equalsIgnoreCase("Food")) {
                holder.img_cat.setImageResource(R.drawable.pic_food);
            } else if (expense.getCategory().equalsIgnoreCase("Commute")) {
                holder.img_cat.setImageResource(R.drawable.pic_car);
            } else if (expense.getCategory().equalsIgnoreCase("Living")) {
                holder.img_cat.setImageResource(R.drawable.pic_house);
            } else if (expense.getCategory().equalsIgnoreCase("Shopping")) {
                holder.img_cat.setImageResource(R.drawable.pic_shop);
            } else {
                holder.img_cat.setImageResource(R.drawable.pic_other);
            }

            // Format and display the expense date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(expense.getDate());
            holder.date.setText(formattedDate);

            // Set the name and amount for the expense
            holder.name.setText(expense.getName());
            holder.amount.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));

            // Handle the edit button click event
            holder.btn_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Edit button clicked for " + expense.getName(), Toast.LENGTH_SHORT).show();
                    // Create an intent to navigate to the Add activity for editing
                    Intent intent = new Intent(context, Add.class);
                    intent.putExtra("documentId", documentId); // Pass the document ID
                    context.startActivity(intent);
                }
            });

            // Handle the delete button click event
            holder.btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Delete button clicked!!", Toast.LENGTH_SHORT).show();
                    // Try to delete the document from Firestore using the document ID
                    try {
                        db.collection("expenses").document(documentId)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });
                    } catch (Exception e) {
                        Log.e(TAG, "Exception while deleting document: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error binding data to ViewHolder: " + e.getMessage());
        }
    }

    // Return the total number of items in the dataset
    @Override
    public int getItemCount() {
        return expenses.size();
    }

    // Method to update the filtered list for search functionality
    public void setFilteredList(List<Expense> filteredList) {
        this.expenses = filteredList;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }
}
