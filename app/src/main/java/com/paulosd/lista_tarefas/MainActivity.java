package com.paulosd.lista_tarefas;

//Aluno: Paulo Sergio Domingues
//R.A. 20135109-5

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "tasks_prefs";
    private static final String TASKS_KEY = "tasks_key";

    private List<Tarefa> taskList;
    private TarefaAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        checkPermissaoNotificacao();

        EditText editTextTask = findViewById(R.id.editTextTask);
        Button btnAddTarefa = findViewById(R.id.btnAddTarefa);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTasks);
        Button btnInfo = findViewById(R.id.btnInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = carregarTarefas();
        if (taskList == null) {
            taskList = new ArrayList<>();
        }

        taskAdapter = new TarefaAdapter(this, taskList);
        recyclerView.setAdapter(taskAdapter);

        if (taskAdapter != null) {
            taskAdapter.setOnRecurringClickListener(this::showTarefaRecorrente);

            taskAdapter.setOnDeleteClickListener(this::onDeleteClick);

            taskAdapter.setOnReminderClickListener(this::showDefinirLembrete);

            taskAdapter.setOnTaskLongClickListener(this::showEditarTarefa);
        }

        btnAddTarefa.setOnClickListener(v -> {
            String taskName = editTextTask.getText().toString().trim();
            if (!taskName.isEmpty()) {
                Tarefa newTask = new Tarefa(taskName);
                taskList.add(newTask);

                if (taskAdapter != null) {
                    taskAdapter.notifyDataSetChanged();
                }
                editTextTask.setText("");
                salvarTarefas();
            } else {
                Toast.makeText(MainActivity.this, "Digite um nome para a tarefa", Toast.LENGTH_SHORT).show();
            }
        });

        btnInfo.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Informações:", Toast.LENGTH_SHORT).show();
            info();
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminder Channel";
            String description = "Channel for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(
                    "task_reminder_channel",
                    name,
                    importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private List<Tarefa> carregarTarefas() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = sharedPreferences.getString(TASKS_KEY, null);
        Type type = new TypeToken<ArrayList<Tarefa>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    private void info(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("INFORMAÇÕES");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.info, findViewById(android.R.id.content), false);
        builder.setView(viewInflated);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.show();

    }

    private void showEditarTarefa(int position) {
        Tarefa task = taskList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Tarefa");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.tela_editar_tarefa, findViewById(android.R.id.content), false);
        final EditText input = viewInflated.findViewById(R.id.editTextTaskName);

        input.setText(task.getName());

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            dialog.dismiss();
            String newTaskName = input.getText().toString().trim();
            if (!newTaskName.isEmpty()) {
                task.setName(newTaskName);
                taskAdapter.notifyDataSetChanged();
                salvarTarefas();
            } else {
                Toast.makeText(MainActivity.this, "O nome da tarefa não pode estar vazio", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showTarefaRecorrente(int position) {
        Tarefa task = taskList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Configurar Tarefa Recorrente");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.tela_definir_recorrencia, findViewById(android.R.id.content), false);
        final Switch switchRecorrencia = viewInflated.findViewById(R.id.switch_recorrencia);
        final CheckBox checkDom = viewInflated.findViewById(R.id.checkDom);
        final CheckBox checkSeg = viewInflated.findViewById(R.id.checkSeg);
        final CheckBox checkTer = viewInflated.findViewById(R.id.checkTer);
        final CheckBox checkQua = viewInflated.findViewById(R.id.checkQua);
        final CheckBox checkQui = viewInflated.findViewById(R.id.checkQui);
        final CheckBox checkSex = viewInflated.findViewById(R.id.checkSex);
        final CheckBox checkSab = viewInflated.findViewById(R.id.checkSab);

        switchRecorrencia.setChecked(task.isRecurring());
        SharedPreferences sharedPreferences = getSharedPreferences("TASK_PREFS", Context.MODE_PRIVATE);
        carregarDiasRecorrentes(sharedPreferences, task, checkDom, checkSeg, checkTer, checkQua, checkQui, checkSex, checkSab);

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            boolean isRecurring = switchRecorrencia.isChecked();
            task.setRecurring(isRecurring);
            task.setRecurringTime(System.currentTimeMillis());

            if (isRecurring) {
                boolean[] daysOfWeek = {
                        checkDom.isChecked(),
                        checkSeg.isChecked(),
                        checkTer.isChecked(),
                        checkQua.isChecked(),
                        checkQui.isChecked(),
                        checkSex.isChecked(),
                        checkSab.isChecked()
                };

                salvarDiasRecorrentes(sharedPreferences, task, checkDom, checkSeg, checkTer, checkQua, checkQui, checkSex, checkSab);
                task.setRecurringDaysOfWeek(daysOfWeek);

                task.setRecurringInterval(0);
                agendamentoTarefaRecorrente(task, 0);
                Log.d("debugRecorrencia", "Tarefa agendade pela Main");
            } else {
                cancelarTarefaRecorrente(task);
            }

            taskAdapter.notifyItemChanged(position);
            salvarTarefas();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void carregarDiasRecorrentes(SharedPreferences sharedPreferences, Tarefa task, CheckBox... checkBoxes) {
        String[] dayKeys = {"_checkDom", "_checkSeg", "_checkTer", "_checkQua", "_checkQui", "_checkSex", "_checkSab"};
        for (int i = 0; i < dayKeys.length; i++) {
            checkBoxes[i].setChecked(sharedPreferences.getBoolean(task.getName() + dayKeys[i], false));
        }
    }

    private void salvarDiasRecorrentes(SharedPreferences sharedPreferences, Tarefa task, CheckBox... checkBoxes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String[] dayKeys = {"_checkDom", "_checkSeg", "_checkTer", "_checkQua", "_checkQui", "_checkSex", "_checkSab"};
        for (int i = 0; i < dayKeys.length; i++) {
            editor.putBoolean(task.getName() + dayKeys[i], checkBoxes[i].isChecked());
        }
        editor.apply();
    }

    private void salvarTarefas() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString(TASKS_KEY, json);
        editor.apply();
        Log.d("DebugSalvar", "Tarefa salva no SharedPreferences: " + json);
    }

    private void agendamentoTarefaRecorrente(Tarefa task, long interval) {
        Log.d("DebugAgendamento", "Iniciando agendamentoTarefaRecorrente para task: " + task.getName() + " com isRecurring: " + true + " e intervalo: " + interval);

        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e("scheduleWarning", "AlarmManager é nulo");
                throw new Exception("AlarmManager é nulo");
            }

            Intent intent = new Intent(this, TarefaRecorrencia.class);
            intent.putExtra("taskName", task.getName());
            Log.d("DebugAgendamento", "Intent criada para task: " + task.getName());

            boolean[] daysOfWeek = task.getRecurringDaysOfWeek();
            Log.d("DebugAgendamento", "Dias selecionados: " + Arrays.toString(daysOfWeek));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Log.d("DebugAgendamento", "PendingIntent criado");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            long startTime = calendar.getTimeInMillis();
            Log.d("DebugAgendamento", "Data e hora atuais configuradas para: " + calendar.getTime());
            calendar.get(Calendar.HOUR_OF_DAY);
            calendar.get(Calendar.MINUTE);
            calendar.get(Calendar.SECOND);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, interval, pendingIntent);
            Log.d("DebugAgendamento", "Tarefa recorrente agendada para: " + calendar.getTime() + " com intervalo de: " + interval);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao agendar tarefa recorrente: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cancelarTarefaRecorrente(Tarefa task) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {;
                throw new Exception("AlarmManager é nulo");
            }

            Intent intent = new Intent(this, TarefaRecorrencia.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            alarmManager.cancel(pendingIntent);
            Log.d("cancelarTarefaRecorrente", "Tarefa recorrente cancelada para tarefa: " + task.getName());
        } catch (Exception e) {
            Log.e("cancelarTarefaRecorrente", "Erro ao cancelar tarefa recorrente", e);
            Toast.makeText(this, "Erro ao cancelar tarefa recorrente: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    private void lembreteTarefa(Tarefa task, long reminderTimeMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TarefaLembrete.class);
        intent.putExtra("taskName", task.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task.getName().hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
    }

    private void showDefinirLembrete(int position) {
        Tarefa task = taskList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Definir Lembrete");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.tela_definir_lembrete, findViewById(android.R.id.content), false);
        final TimePicker timePicker = viewInflated.findViewById(R.id.timePicker);

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            dialog.dismiss();
            int hour, minute;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
            } else {
                hour = timePicker.getCurrentHour();
                minute = timePicker.getCurrentMinute();
            }

            task.setReminder(hour, minute);

            lembreteTarefa(task, task.getReminderTimeMillis());

            taskAdapter.notifyDataSetChanged();
            salvarTarefas();
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void showCancelarLembrete(Tarefa task) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TarefaLembrete.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task.getName().hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
    }

    private void checkPermissaoNotificacao() {
        // Verifica se o canal de notificação foi criado
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel("task_channel");
            if (channel == null) {
                createNotificationChannel();
            }
        }

        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Toast.makeText(this, "O APP precisa de permissão para exibir notificações", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }, 4000);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        for (Tarefa task : taskList) {
            if (task.isRecurring()) {
                agendamentoTarefaRecorrente(task, task.getRecurringInterval());
            }
        }
    }

    private void onDeleteClick(int position) {
        Tarefa task = taskList.get(position);
        taskList.remove(position);
        taskAdapter.notifyDataSetChanged();
        cancelarTarefaRecorrente(task);
        showCancelarLembrete(task);
        salvarTarefas();
    }
}
