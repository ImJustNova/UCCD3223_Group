package com.example.kachin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<FinancialTransaction> transactionList;

    // Constructor for TransactionAdapter
    public TransactionAdapter(List<FinancialTransaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each transaction item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        FinancialTransaction transaction = transactionList.get(position);

        // Set the transaction details
        holder.transactionCategory.setText(transaction.getCategory());
        holder.transactionAmount.setText(String.format("RM %.2f", transaction.getAmount()));
        holder.transactionDescription.setText(transaction.getDescription());
        holder.transactionDate.setText(transaction.getDate());

        // Set the icon based on the category
        switch (transaction.getCategory()) {
            case "Food and Drinks":
                holder.transactionCategoryIcon.setImageResource(R.drawable.food_and_drinks_icon);
                break;
            case "Transport":
                holder.transactionCategoryIcon.setImageResource(R.drawable.transport_icon);
                break;
            case "Groceries":
                holder.transactionCategoryIcon.setImageResource(R.drawable.groceries_icon);
                break;
            case "Entertainment":
                holder.transactionCategoryIcon.setImageResource(R.drawable.entertainment_icon);
                break;
            case "Rent":
                holder.transactionCategoryIcon.setImageResource(R.drawable.rent_icon);
                break;
            case "Others":
                holder.transactionCategoryIcon.setImageResource(R.drawable.others_icon);
                break;
            default:
                holder.transactionCategoryIcon.setImageResource(R.drawable.profile); // Use a default icon for unknown categories
                break;
        }
    }

    @Override
    public int getItemCount() {
        // Return the size of the transaction list
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionCategory, transactionAmount, transactionDescription, transactionDate;
        ImageView transactionCategoryIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind the views with the respective IDs in XML
            transactionCategory = itemView.findViewById(R.id.transactionCategory);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDescription = itemView.findViewById(R.id.transactionDescription);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionCategoryIcon = itemView.findViewById(R.id.transactionCategoryIcon);
        }
    }
}
