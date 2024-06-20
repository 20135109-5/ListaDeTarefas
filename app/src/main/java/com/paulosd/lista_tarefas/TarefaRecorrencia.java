package com.paulosd.lista_tarefas;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TarefaRecorrencia extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra("taskName");
        if (taskName == null) {
            Log.d("debugrecorrencia", "TaskName null");
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConfig.PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(AppConfig.TASKS_KEY, null);
        Log.d("debugrecorrencia", "JSON recuperado do SharedPreferences: " + json);

        Type type = new TypeToken<ArrayList<Tarefa>>() {}.getType();
        List<Tarefa> taskList = gson.fromJson(json, type);

        if (taskList != null) {
            Log.d("debugrecorrencia", "TaskList diferente de null");
            for (Tarefa task : taskList) {
                if (task.getName().equals(taskName)) {
                    Log.d("debugrecorrencia", "Task name igual name");

                    boolean[] diasSemana = task.getRecurringDaysOfWeek();
                    Calendar calendar = Calendar.getInstance();
                    int hoje = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                    Log.d("debugrecorrencia", "Dias de recorrencia: " + Arrays.toString(diasSemana));
                    Toast.makeText(context.getApplicationContext(), "Tarefa recorrente agendada!", Toast.LENGTH_SHORT).show();

                    if (diasSemana[hoje]) {
                        task.setCompleted(false);
                        Log.d("debugrecorrencia", "Tarefa alterada para pendente");
                        Toast.makeText(context.getApplicationContext(), "Tarefa alterada para pendente!", Toast.LENGTH_SHORT).show();

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        json = gson.toJson(taskList);
                        editor.putString(AppConfig.TASKS_KEY, json);
                        editor.apply();
                        Log.d("debugrecorrencia", "Tarefa atualizada no SharedPreferences: " + json);
                    }
                    break;
                }
            }
        }

        agendamentoProximaRecorrencia(context, taskName);
    }

    private void agendamentoProximaRecorrencia(Context context, String taskName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConfig.PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(AppConfig.TASKS_KEY, null);
        Type type = new TypeToken<ArrayList<Tarefa>>() {}.getType();
        List<Tarefa> taskList = gson.fromJson(json, type);

        if (taskList != null) {
            for (Tarefa task : taskList) {
                if (task.getName().equals(taskName)) {
                    boolean[] diasSemana = task.getRecurringDaysOfWeek();
                    Calendar calendar = Calendar.getInstance();
                    int hoje = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                    int proximosDiasRecorrentes = -1;
                    for (int i = 1; i <= 7; i++) {
                        int proximoDia = (hoje + i) % 7;
                        if (diasSemana[proximoDia]) {
                            proximosDiasRecorrentes = i;
                            break;
                        }
                    }

                    if (proximosDiasRecorrentes != -1) {
                        calendar.add(Calendar.DAY_OF_MONTH, proximosDiasRecorrentes);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent newIntent = new Intent(context, TarefaRecorrencia.class);
                        newIntent.putExtra("taskName", taskName);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, taskName.hashCode(), newIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        Log.d("debugrecorrencia", "Tarefa recorrente agendada para: " + calendar.getTime());
                        Toast.makeText(context.getApplicationContext(), "Tarefa recorrente agendada para: " + calendar.getTime(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    }
}
