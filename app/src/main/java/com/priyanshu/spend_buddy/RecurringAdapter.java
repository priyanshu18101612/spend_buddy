package com.priyanshu.spend_buddy;

import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RecurringAdapter extends RecyclerView.Adapter<RecurringAdapter.ViewHolder> {

    List<RecurringModel> list;
    FirebaseFirestore db;
    String userId;

    public RecurringAdapter(List<RecurringModel> list) {
        this.list = list;
        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recurring, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        RecurringModel item = list.get(position);

        holder.tvAmount.setText("₹" + item.amount);
        holder.tvDay.setText("Day " + item.day);

        // 🗑️ DELETE (USER-WISE FIX)
        holder.btnDelete.setOnClickListener(v -> {

            if (userId == null) return;

            db.collection("users")
                    .document(userId)
                    .collection("recurring")
                    .document(item.id)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        list.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(v.getContext(), "Deleted 🗑️", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvAmount, tvDay;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDay = itemView.findViewById(R.id.tvDay);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}