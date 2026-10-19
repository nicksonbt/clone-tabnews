package com.nicksonbt.tarefas;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda as tarefas em SharedPreferences como JSON. É compartilhado entre o
 * app e o widget, por isso grava com commit() (síncrono).
 */
final class TaskStore {
    private static final String PREFS = "tarefas";
    private static final String KEY_TASKS = "tasks";
    private static final String KEY_NEXT_ID = "next_id";

    private final SharedPreferences prefs;

    TaskStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Tarefas pendentes primeiro (mais novas no topo), depois as concluídas. */
    List<Task> load() {
        List<Task> pending = new ArrayList<>();
        List<Task> done = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(KEY_TASKS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                Task task = new Task(o.getLong("id"), o.getString("text"), o.optBoolean("done"));
                (task.done ? done : pending).add(task);
            }
        } catch (JSONException e) {
            return new ArrayList<>();
        }
        pending.addAll(done);
        return pending;
    }

    void add(String text) {
        List<Task> tasks = load();
        long id = prefs.getLong(KEY_NEXT_ID, 1);
        tasks.add(0, new Task(id, text, false));
        save(tasks, id + 1);
    }

    void toggle(long id) {
        List<Task> tasks = load();
        for (Task task : tasks) {
            if (task.id == id) {
                task.done = !task.done;
            }
        }
        save(tasks, -1);
    }

    void delete(long id) {
        List<Task> tasks = load();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            if (tasks.get(i).id == id) {
                tasks.remove(i);
            }
        }
        save(tasks, -1);
    }

    void clearDone() {
        List<Task> tasks = load();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            if (tasks.get(i).done) {
                tasks.remove(i);
            }
        }
        save(tasks, -1);
    }

    static int countPending(List<Task> tasks) {
        int count = 0;
        for (Task task : tasks) {
            if (!task.done) {
                count++;
            }
        }
        return count;
    }

    static String summary(Context context, List<Task> tasks) {
        if (tasks.isEmpty()) {
            return context.getString(R.string.summary_none);
        }
        int pending = countPending(tasks);
        if (pending == 0) {
            return context.getString(R.string.summary_all_done);
        }
        return context.getResources().getQuantityString(R.plurals.summary_pending, pending, pending);
    }

    private void save(List<Task> tasks, long nextId) {
        JSONArray array = new JSONArray();
        try {
            for (Task task : tasks) {
                array.put(new JSONObject()
                        .put("id", task.id)
                        .put("text", task.text)
                        .put("done", task.done));
            }
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
        SharedPreferences.Editor editor = prefs.edit().putString(KEY_TASKS, array.toString());
        if (nextId > 0) {
            editor.putLong(KEY_NEXT_ID, nextId);
        }
        editor.commit();
    }
}
