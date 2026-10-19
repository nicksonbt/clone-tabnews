package com.nicksonbt.tarefas;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

/** Fornece as linhas da lista de tarefas exibida no widget. */
public class TaskWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    private static class Factory implements RemoteViewsFactory {
        private final Context context;
        private List<Task> tasks = new ArrayList<>();

        Factory(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            tasks = new TaskStore(context).load();
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position >= tasks.size()) {
                return null;
            }
            Task task = tasks.get(position);
            RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_item);

            if (task.done) {
                SpannableString text = new SpannableString(task.text);
                text.setSpan(new StrikethroughSpan(), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                row.setTextViewText(R.id.widget_item_text, text);
                row.setTextColor(R.id.widget_item_text, context.getColor(R.color.text_dim));
                row.setImageViewResource(R.id.widget_item_icon, R.drawable.ic_check_circle);
            } else {
                row.setTextViewText(R.id.widget_item_text, task.text);
                row.setTextColor(R.id.widget_item_text, context.getColor(R.color.text));
                row.setImageViewResource(R.id.widget_item_icon, R.drawable.ic_circle);
            }

            Intent fillIn = new Intent().putExtra(TaskWidgetProvider.EXTRA_TASK_ID, task.id);
            row.setOnClickFillInIntent(R.id.widget_item, fillIn);
            return row;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position < tasks.size() ? tasks.get(position).id : position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
