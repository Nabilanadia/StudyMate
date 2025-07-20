package com.example.studymate.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;
import com.example.studymate.models.Reminder;

import java.util.ArrayList;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private ArrayList<Reminder> reminders;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Reminder reminder);
        void onItemLongClick(Reminder reminder);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ReminderAdapter(ArrayList<Reminder> reminders, Context context) {
        this.reminders = reminders;
        this.context = context;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Reminder reminder = reminders.get(position);
        holder.title.setText(reminder.getTitle());
        holder.datetime.setText(reminder.getDateTime());
        holder.tag.setText(reminder.getTag());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(reminder);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(reminder);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView title, datetime, tag;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reminderTitle);
            datetime = itemView.findViewById(R.id.reminderDate);
            tag = itemView.findViewById(R.id.reminderTag);
        }
    }
}
