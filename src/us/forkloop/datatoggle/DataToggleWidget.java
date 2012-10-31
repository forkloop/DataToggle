package us.forkloop.datatoggle;

import java.lang.reflect.Method;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

public class DataToggleWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Method dataConnSwitchMethod, getITelephonyMethod;
        Class<? extends Object> telephonyManagerClass;
        Class<? extends Object> ITelephonyClass;
        Object ITelephonyStub;
        RemoteViews rv;
        AppWidgetManager wm;
        ComponentName widgetPro;
        boolean isEnabled;

        rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        if (intent.getAction().equals("us.forkloop.datatoggle.TOGGLE")) {
            /**
             * @link http://stackoverflow.com/questions/3644144/how-to-disable-mobile-data-on-android
             */
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
                isEnabled = true;
                Log.d(getClass().getSimpleName(), "DATA_CONNECTED");
            } else {
                isEnabled = false;
                Log.d(getClass().getSimpleName(), "DATA_DISCONNECTED or");
            }
            try {
                telephonyManagerClass = TelephonyManager.class;
                getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());
                if (isEnabled) {
                    dataConnSwitchMethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
                    Log.d(getClass().getSimpleName(), "user try to disable connection...");
                    rv.setImageViewResource(R.id.image_button, R.drawable.princess);
                } else {
                    dataConnSwitchMethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");
                    Log.d(getClass().getSimpleName(), "user try to enable connection...");
                    rv.setImageViewResource(R.id.image_button, R.drawable.emerald);
                }
                dataConnSwitchMethod.setAccessible(true);
                dataConnSwitchMethod.invoke(ITelephonyStub);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(getClass().getSimpleName(), e.toString());
            }
        } else if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
                rv.setImageViewResource(R.id.image_button, R.drawable.emerald);
                Log.d(getClass().getSimpleName(), "Connection enabled.");
            } else {
                rv.setImageViewResource(R.id.image_button, R.drawable.princess);
                Log.i(getClass().getSimpleName(), "Connection disabled.");
            }
        }
        wm = AppWidgetManager.getInstance(context);
        widgetPro = new ComponentName(context, DataToggleWidget.class);
        int[] allWidgetIds = wm.getAppWidgetIds(widgetPro);
        for (int i = 0; i < allWidgetIds.length; i++)
            wm.updateAppWidget(allWidgetIds[i], rv);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent toggleIntent = new Intent("us.forkloop.datatoggle.TOGGLE");
            PendingIntent pendingToggle = PendingIntent.getBroadcast(context, 0, toggleIntent, 0);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            rv.setOnClickPendingIntent(R.id.image_button, pendingToggle);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
    }
}