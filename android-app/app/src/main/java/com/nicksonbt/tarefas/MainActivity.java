package com.nicksonbt.tarefas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    static final String EXTRA_FOCUS_INPUT = "focus_input";

    private TaskStore store;
    private List<Task> tasks = new ArrayList<>();
    private TaskAdapter adapter;
    private TextView summary;
    private EditText input;
    private Button clearDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        store = new TaskStore(this);
        summary = findViewById(R.id.summary);
        input = findViewById(R.id.input);
        clearDone = findViewById(R.id.clear_done);

        ListView list = findViewById(R.id.list);
        adapter = new TaskAdapter();
        list.setAdapter(adapter);
        list.setEmptyView(findViewById(R.id.empty));
        list.setOnItemClickListener((parent, view, position, id) -> {
            store.toggle(id);
            refresh();
        });

        findViewById(R.id.add).setOnClickListener(v -> addTask());
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTask();
                return true;
            }
            return false;
        });
        clearDone.setOnClickListener(v -> {
            store.clearDone();
            refresh();
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // O widget pode ter alterado as tarefas enquanto o app estava fechado.
        refresh();
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_FOCUS_INPUT, false)) {
            input.requestFocus();
            input.post(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            });
        }
    }

    private void addTask() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        store.add(text);
        input.setText("");
        refresh();
    }

    private void refresh() {
        tasks = store.load();
        adapter.notifyDataSetChanged();
        summary.setText(TaskStore.summary(this, tasks));
        clearDone.setVisibility(TaskStore.countPending(tasks) < tasks.size() ? View.VISIBLE : View.GONE);
        TaskWidgetProvider.refreshAll(this);
    }

    private class TaskAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public Task getItem(int position) {
            return tasks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return tasks.get(position).id;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null
                    ? convertView
                    : LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            Task task = getItem(position);

            CheckBox check = view.findViewById(R.id.check);
            TextView text = view.findViewById(R.id.text);
            check.setChecked(task.done);
            text.setText(task.text);
            if (task.done) {
                text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                text.setAlpha(0.5f);
            } else {
                text.setPaintFlags(text.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                text.setAlpha(1f);
            }

            view.findViewById(R.id.delete).setOnClickListener(v -> {
                store.delete(task.id);
                refresh();
            });
            return view;
        }
    }
}
