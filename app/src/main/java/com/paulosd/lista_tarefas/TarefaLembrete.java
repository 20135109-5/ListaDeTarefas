package com.paulosd.lista_tarefas;

import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import android.os.Build;

public class TarefaLembrete extends BroadcastReceiver {

    private static final String CHANNEL_ID = "task_reminder_channel";
    private static final int NOTIFICATION_ID = 123;

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra("taskName");

        if (taskName == null) {
            return;
        }

        criarNotificacao(context, taskName);
    }

    private void criarNotificacao(Context context, String taskName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminder Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Lembrete de Tarefa")
                .setContentText("Não se esqueça de realizar a tarefa: " + taskName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true); // Fecha a notificação ao clicar nela

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
