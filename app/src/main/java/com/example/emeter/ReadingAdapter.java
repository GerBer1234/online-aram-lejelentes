package com.example.emeter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A bejelentések listájának megjelenítése.
 */
public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.ReadingViewHolder> {
    private final List<Reading> readingList;
    private final Context adapterContext;
    private String latestReadingId;
    private OnDataChangedListener dataChangedListener;

    public ReadingAdapter(Context context, List<Reading> readingList, String latestReadingId) {
        this.adapterContext = context;
        this.readingList = readingList;
        this.latestReadingId = latestReadingId;
    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading, parent, false);
        return new ReadingViewHolder(view);
    }

    /**
     * Beállítja az aktuális listaelem megjelenését, eseménykezelőkkel együtt.
     */
    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        Reading reading = readingList.get(position);

        String title = (position + 1) + ". " +
                (reading.getComment() == null || reading.getComment().trim().isEmpty()
                        ? "Bejelentés"
                        : reading.getComment());

        holder.titleTextView.setText(title + " – " + reading.getStartDate() + " → " + reading.getEndDate());
        holder.consumptionTextView.setText("Fogyasztás: " + reading.getConsumption() + " kWh");

        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(adapterContext);
            builder.setTitle("Megjegyzés szerkesztése");

            final EditText input = new EditText(adapterContext);
            input.setText(reading.getComment());
            input.setHint("Írj új megjegyzést...");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Mentés", (dialog, which) -> {
                String updatedComment = input.getText().toString().trim();
                if (!updatedComment.isEmpty()) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        db.collection("readings")
                                .document(user.getUid())
                                .collection("user_readings")
                                .document(reading.getId())
                                .update("comment", updatedComment)
                                .addOnSuccessListener(aVoid -> {
                                    reading.setComment(updatedComment);
                                    notifyItemChanged(position);
                                    Toast.makeText(adapterContext, "Megjegyzés frissítve", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(adapterContext, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            });

            builder.setNegativeButton("Mégse", (dialog, which) -> dialog.cancel());

            builder.show();
        });
        holder.itemView.setOnLongClickListener(v -> {
            long now = System.currentTimeMillis();
            if (reading.getCreatedAt() != null &&
                    now - reading.getCreatedAt().getTime() <= TimeUnit.MINUTES.toMillis(5)) {

                new AlertDialog.Builder(adapterContext)
                        .setTitle("Törlés megerősítése")
                        .setMessage("Biztosan törölni szeretnéd ezt a bejelentést?")
                        .setPositiveButton("Igen", (dialog, which) -> {
                            deleteReading(holder.getAdapterPosition(), reading);
                        })
                        .setNegativeButton("Mégse", null)
                        .show();
            } else {
                Toast.makeText(adapterContext, "Ez a bejelentés már nem törölhető.", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    /**
     * Visszaadja a bejelentések számát.
     *
     * @return A bejelentések száma.
     */
    @Override
    public int getItemCount() {
        return readingList.size();
    }

    /**
     * A bejelentés egy sorának megjelenítése.
     */
    public static class ReadingViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, consumptionTextView;

        public ReadingViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.readingTitleTextView);
            consumptionTextView = itemView.findViewById(R.id.readingConsumptionTextView);
        }
    }

    /**
     * Törli az adott bejelentést a Firestore-ból és a listából is.
     *
     * @param position Lista pozíciója
     * @param reading  A törlendő elem
     */
    private void deleteReading(int position, Reading reading) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore.getInstance()
                .collection("readings")
                .document(currentUser.getUid())
                .collection("user_readings")
                .document(reading.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    readingList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(adapterContext, "Törlés sikeres", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(adapterContext, "Törlés sikertelen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        if (dataChangedListener != null) {
            dataChangedListener.onDataChanged();
        }

    }

    /**
     * Egy interfész, amely lehetővé teszi, hogy más osztályok értesüljenek az adatok változásáról.
     */
    public interface OnDataChangedListener {
        void onDataChanged();
    }

    /**
     * Beállítja azt az eseményfigyelőt, amely értesítést kap, ha az adapterben változás történik.
     *
     * @param listener a figyelő, amelyet értesíteni kell változás esetén
     */
    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.dataChangedListener = listener;
    }
}
