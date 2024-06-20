package com.paulosd.lista_tarefas;

import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;

public class Tarefa {
    private String name;
    private boolean isCompleted;
    private boolean isRecurring;
    private long recurringInterval;
    private long recurringTime;
    private boolean[] recurringDaysOfWeek;
    private long reminderTime;
    private long lastCompletionTime;

    public Tarefa(String name) {
        this.name = name;
        this.isCompleted = false;
        this.isRecurring = false;
        this.recurringInterval = 0;
        this.reminderTime = 0;
        this.lastCompletionTime = 0;
        this.recurringTime = 0;
        this.recurringDaysOfWeek = new boolean[7];
    }
    public long getLastCompletionTime() {
        return lastCompletionTime;
    }

    public void setLastCompletionTime(long lastCompletionTime) {
        this.lastCompletionTime = lastCompletionTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
    public void setCompleted(boolean completed) {
        isCompleted = completed;
        if (completed) {
            setLastCompletionTime(System.currentTimeMillis());
        }
    }

    public long getRecurringTime() {
        return recurringTime;
    }

    public void setRecurringTime(long recurringTime) {
        this.recurringTime = recurringTime;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public long getRecurringInterval() {
        return recurringInterval;
    }

    public void setRecurringInterval(long recurringInterval) {
        this.recurringInterval = recurringInterval;
    }

    public long getReminderTimeMillis() {
        return reminderTime;
    }

    public void setReminderTimeMillis(long reminderTimeMillis) {
        this.reminderTime = reminderTimeMillis;
    }

    public boolean isReminderSet() {
        return reminderTime != 0;
    }

    public boolean isReminderTimeExpired() {
        return isReminderSet() && System.currentTimeMillis() > reminderTime;
    }

    public void setReminder(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, hourOfDay, minute);
        setReminderTimeMillis(calendar.getTimeInMillis());
    }
    public void setReminder(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        setReminderTimeMillis(calendar.getTimeInMillis());
    }
    public boolean[] getRecurringDaysOfWeek() {
        return recurringDaysOfWeek;
    }

    public void setRecurringDaysOfWeek(boolean[] recurringDaysOfWeek) {
        if (recurringDaysOfWeek.length == 7) {
            this.recurringDaysOfWeek = recurringDaysOfWeek;
            Log.d("debugrecorrencia", "definidosDias" + Arrays.toString(recurringDaysOfWeek));
        } else {
            throw new IllegalArgumentException("Array deve ter exatamente 7 elementos.");
        }
    }
    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", isCompleted=" + isCompleted +
                ", isRecurring=" + isRecurring +
                ", recurringInterval=" + recurringInterval +
                ", reminderTime=" + reminderTime +
                ", lastCompletionTime=" + lastCompletionTime +
                ", recurringTime=" + recurringTime +
                ", recurringDaysOfWeek=" + Arrays.toString(recurringDaysOfWeek) +
                '}';
    }
}
