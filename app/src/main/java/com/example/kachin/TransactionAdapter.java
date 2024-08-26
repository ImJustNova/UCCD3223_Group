package com.example.kachin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<FinancialTransaction> transactionList;

    public TransactionAdapter(List<FinancialTransaction> transactionList) {
        this.transactionList = transactionList;
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
        holder.transactionAmount.setText(String.format("RM %.2f", transaction.getAmount()));
        holder.transactionDescription.setText(transaction.getDescription());
        holder.transactionDate.setText(transaction.getDate());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionCategory, transactionAmount, transactionDescription, transactionDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionCategory = itemView.findViewById(R.id.transactionCategory);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDescription = itemView.findViewById(R.id.transactionDescription);
            transactionDate = itemView.findViewById(R.id.transactionDate);
        }
    }
}