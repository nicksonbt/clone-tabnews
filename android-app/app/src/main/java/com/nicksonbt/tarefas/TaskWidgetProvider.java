package com.nicksonbt.tarefas;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class TaskWidgetProvider extends AppWidgetProvider {
    static final String ACTION_TOGGLE = "com.nicksonbt.tarefas.TOGGLE";
    static final String EXTRA_TASK_ID = "task_id";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        update(context, manager, widgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_TOGGLE.equals(intent.getAction())) {
            long id = intent.getLongExtra(EXTRA_TASK_ID, -1);
            if (id != -1) {
                new TaskStore(context).toggle(id);
                refreshAll(context);
            }
            return;
        }
        super.onReceive(context, intent);
    }

    /** Atualiza todos os widgets na tela inicial depois de uma mudança nas tarefas. */
    static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TaskWidgetProvider.class));
        if (ids.length > 0) {
            update(context, manager, ids);
        }
    }

    private static void update(Context context, AppWidgetManager manager, int[] widgetIds) {
        String summary = TaskStore.summary(context, new TaskStore(context).load());
        for (int widgetId : widgetIds) {
            manager.updateAppWidget(widgetId, build(context, widgetId, summary));
        }
        manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_list);
    }

    private static RemoteViews build(Context context, int widgetId, String summary) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasks);
        views.setTextViewText(R.id.widget_count, summary);

        Intent service = new Intent(context, TaskWidgetService.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        // Um data único por widget evita que o sistema reaproveite o mesmo adapter.
        service.setData(Uri.parse(service.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widget_list, service);
        views.setEmptyView(R.id.widget_list, R.id.widget_empty);

        Intent toggle = new Intent(context, TaskWidgetProvider.class).setAction(ACTION_TOGGLE);
        views.setPendingIntentTemplate(R.id.widget_list, PendingIntent.getBroadcast(
                context, 0, toggle, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE));

        views.setOnClickPendingIntent(R.id.widget_header, openApp(context, false));
        views.setOnClickPendingIntent(R.id.widget_add, openApp(context, true));
        return views;
    }

    private static PendingIntent openApp(Context context, boolean focusInput) {
        Intent intent = new Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_FOCUS_INPUT, focusInput)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, focusInput ? 2 : 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
