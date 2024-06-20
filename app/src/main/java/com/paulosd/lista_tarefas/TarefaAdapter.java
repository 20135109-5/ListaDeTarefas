package com.paulosd.lista_tarefas;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.ViewHolder> {

    private final List<Tarefa> tasks;
    private final LayoutInflater inflater;
    private OnRecurringClickListener onRecurringClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private OnReminderClickListener onReminderClickListener;
    private OnTaskLongClickListener onTaskLongClickListener;


    public TarefaAdapter(Context context, List<Tarefa> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
    }

    public void setOnRecurringClickListener(OnRecurringClickListener listener) {
        this.onRecurringClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public void setOnReminderClickListener(OnReminderClickListener listener) {
        this.onReminderClickListener = listener;
    }

    public void setOnTaskLongClickListener(OnTaskLongClickListener listener) {
        this.onTaskLongClickListener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.lista_item_tarefa, parent, false);
        return new ViewHolder(itemView, onTaskLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Tarefa task = tasks.get(position);

        holder.textViewTask.setText(task.getName());
        holder.checkBoxTask.setOnCheckedChangeListener(null);
        holder.checkBoxTask.setChecked(task.isCompleted());

        if (task.isCompleted()) {
            Log.d("CheckDebug", "CheckTarefaCompleta");
            holder.textViewTask.setPaintFlags(holder.textViewTask.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textViewTask.setPaintFlags(holder.textViewTask.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.checkBoxTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            Log.d("CheckDebug", "CheckListnerComplet");

            saveTasks(holder.checkBoxTask.getContext());
            Log.d("CheckDebug", "CheckSave");

            if (isChecked) {
                holder.textViewTask.setPaintFlags(holder.textViewTask.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                Log.d("CheckDebug", "VisualCheck");
            } else {
                holder.textViewTask.setPaintFlags(holder.textViewTask.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            if (task.isRecurring()) {
                holder.textViewRecurring.setVisibility(View.VISIBLE);
                holder.textViewRecurring.setText(getRecurringDaysString(task.getRecurringDaysOfWeek()));
            } else {
                holder.textViewRecurring.setVisibility(View.GONE);
            }

            holder.itemView.post(() -> notifyItemChanged(position));
        });

        holder.buttonRecurringTask.setOnClickListener(v -> {
            if (onRecurringClickListener != null) {
                onRecurringClickListener.onEditClick(position);
            }
        });

        holder.buttonDeleteTask.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position);
                saveTasks(holder.buttonDeleteTask.getContext());
                notifyItemRemoved(position);
            }
        });

        holder.buttonSetReminder.setOnClickListener(v -> {
            if (onReminderClickListener != null) {
                onReminderClickListener.onReminderClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private void saveTasks(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(tasks);
        editor.putString("task_list", json);
        editor.apply();
        Log.d("CheckDebug", "SalvoSahredPref: " + json);
    }

    private String getRecurringDaysString(boolean[] recurringDaysOfWeek) {
        StringBuilder days = new StringBuilder();
        String[] dayNames = {"Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sabádo"};
        for (int i = 0; i < recurringDaysOfWeek.length; i++) {
            if (recurringDaysOfWeek[i]) {
                if (days.length() > 0) {
                    days.append(", ");
                }
                days.append(dayNames[i]);
            }
        }
        return days.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTask;
        CheckBox checkBoxTask;
        TextView textViewRecurring;
        Button buttonRecurringTask;
        Button buttonDeleteTask;
        Button buttonSetReminder;

        public ViewHolder(@NonNull View itemView, final OnTaskLongClickListener longClickListener) {
            super(itemView);
            textViewTask = itemView.findViewById(R.id.textViewTask);
            checkBoxTask = itemView.findViewById(R.id.checkBoxTask);
            textViewRecurring = itemView.findViewById(R.id.textViewRecurring);
            buttonRecurringTask = itemView.findViewById(R.id.buttonRecurringTask);
            buttonDeleteTask = itemView.findViewById(R.id.buttonDeleteTask);
            buttonSetReminder = itemView.findViewById(R.id.buttonSetReminder);

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        longClickListener.onTaskLongClick(position);
                        return true;
                    }
                }
                return false;
            });
        }
    }

    public interface OnRecurringClickListener {
        void onEditClick(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public interface OnReminderClickListener {
        void onReminderClick(int position);
    }

    public interface OnTaskLongClickListener {
        void onTaskLongClick(int position);
    }
}
