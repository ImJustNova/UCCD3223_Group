package com.example.kachin;

import android.content.Context;
import android.content.SharedPreferences;
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
    private String currencyUnit;

    public TransactionAdapter(Context context, List<FinancialTransaction> transactionList) {
        this.transactionList = transactionList;

        SharedPreferences currencyPref = context.getSharedPreferences("CurrencyPrefs", Context.MODE_PRIVATE);
        String selectedCurrency = currencyPref.getString("selectedCurrency", "MYR");
        String[] currencyUnits = context.getResources().getStringArray(R.array.currency_units);
        this.currencyUnit = getCurrencyUnit(selectedCurrency, currencyUnits);
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        FinancialTransaction transaction = transactionList.get(position);

        holder.transactionCategory.setText(transaction.getCategory());
        holder.transactionAmount.setText(String.format(currencyUnit + " %.2f", transaction.getAmount()));
        holder.transactionDescription.setText(transaction.getDescription());
        holder.transactionDate.setText(transaction.getDate());

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
        return Math.min(transactionList.size(), 3); // Limit to 3 items
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionCategory, transactionAmount, transactionDescription, transactionDate;
        ImageView transactionCategoryIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionCategory = itemView.findViewById(R.id.transactionCategory);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDescription = itemView.findViewById(R.id.transactionDescription);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionCategoryIcon = itemView.findViewById(R.id.transactionCategoryIcon);
        }
    }

    private String getCurrencyUnit(String selectedCurrency, String[] currencyUnits) {
        for (String unit : currencyUnits) {
            if (unit.startsWith(selectedCurrency)) {
                return unit.split(" - ")[1];
            }
        }
        return "RM"; // Default to RM if not found
    }
}