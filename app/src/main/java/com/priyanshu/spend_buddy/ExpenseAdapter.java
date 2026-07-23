package com.priyanshu.spend_buddy;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    List<Expenses> list;

    public ExpenseAdapter(List<Expenses> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvAmount, tvDate, tvIcon, tvCategory;

        public ViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    public void updateList(List<Expenses> newList) {
        if (newList == null) return;
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        Expenses e = list.get(pos);
        if (e == null) return;

        // 🔥 CATEGORY ICON
        String emoji;
        String cat = (e.category == null) ? "other" : e.category.toLowerCase().trim();

        switch (cat) {
            case "food": emoji = "🍔"; break;
            case "tea": emoji = "☕"; break;
            case "shopping": emoji = "🛍️"; break;
            case "travel": emoji = "✈️"; break;
            case "water": emoji = "💧"; break;
            case "milk": emoji = "🥛"; break;
            default: emoji = "💸";
        }

        holder.tvIcon.setText(emoji);
        holder.tvTitle.setText(e.title);
        holder.tvAmount.setText("₹" + e.amount);
        holder.tvCategory.setText(e.category);

        // 🔥 DATE
        if (e.date != 0) {
            holder.tvDate.setText(
                    new SimpleDateFormat("dd MMM", Locale.getDefault())
                            .format(new Date(e.date))
            );
        } else {
            holder.tvDate.setText("Recent");
        }

        // 🔥 COLOR
        if ("food".equals(cat)) {
            holder.tvAmount.setTextColor(Color.RED);
        } else if ("travel".equals(cat)) {
            holder.tvAmount.setTextColor(Color.BLUE);
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#00E676"));
        }

        // 🔥 DELETE FIXED
        holder.itemView.setOnLongClickListener(v -> {

            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return true;

            Expenses expense = list.get(currentPos);

            // 🔴 NULL CHECKS (CRASH FIX)
            if (expense.id == null) {
                Toast.makeText(v.getContext(), "Error: ID missing", Toast.LENGTH_SHORT).show();
                return true;
            }

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(v.getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return true;
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("expenses")
                    .document(expense.id)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        list.remove(currentPos);
                        notifyItemRemoved(currentPos);
                        Toast.makeText(v.getContext(), "Deleted 🗑️", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e1 -> {
                        Toast.makeText(v.getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    });

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return (list == null) ? 0 : list.size();
    }
}