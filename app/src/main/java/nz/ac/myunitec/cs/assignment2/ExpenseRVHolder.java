package nz.ac.myunitec.cs.assignment2;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ExpenseRVHolder extends RecyclerView.ViewHolder{
    ImageView img_cat,btn_edit,btn_delete;
    TextView name, amount,date;
    public ExpenseRVHolder(@NonNull View itemView) {
        super(itemView);
        img_cat =  itemView.findViewById(R.id.img);
        name = itemView.findViewById(R.id.name);
        amount = itemView.findViewById(R.id.amount);
        date = itemView.findViewById(R.id.date);
        btn_edit =  itemView.findViewById(R.id.img_edit);
        btn_delete =  itemView.findViewById(R.id.img_del);
    }
}
