package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.service.FetchScoresService;

/**
 * Created by Derek on 1/13/2016.
 */
public class ScoreListWidgetProvider extends AppWidgetProvider {
    public final String LOG_TAG = ScoreListWidgetProvider.class.getSimpleName();
    //public static final String TOAST_ACTION = "com.example.android.stackwidget.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.example.android.stackwidget.EXTRA_ITEM";

    // http://docs.huihoo.com/android/3.0/resources/samples/StackWidget/src/com/example/android/stackwidget/index.html
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            // Setup the intent which points to ScoreListWidgetRemoteViewsService
            // That service provides the views for the ListView
            Intent intent = new Intent(context, ScoreListWidgetRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // point to the layout that will be used as the ListView item
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_score_list);
            rv.setRemoteAdapter(R.id.score_listview, intent);

            // Just in case there are no scores at this time, display a friendly message
            rv.setEmptyView(R.id.score_listview, R.id.score_list_empty);

            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            Intent toastIntent = new Intent(context, MainActivity.class);

            PendingIntent toastPendingIntent = PendingIntent.getActivity(context, 0, toastIntent, 0);

            // point to the actual ListView
            rv.setPendingIntentTemplate(R.id.score_listview, toastPendingIntent);

            // also make the title bar of the widget launch the app
            Intent listIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, listIntent, 0);

            // point to the Linear Layout that is the 'Title Bar' of the widget
            rv.setOnClickPendingIntent(R.id.score_list_widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        //Log.d(LOG_TAG, "Call to onReceive - Action: " + intent.getAction());

        if (FetchScoresService.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

            //if (appWidgetIds.length > 0) {
            //    Log.d(LOG_TAG, "******* onReceive - notifyAppWidgetViewDataChanged, appWidgetId: " + String.valueOf(appWidgetIds[0]));
            //}
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.score_listview);
        }
    }
}
