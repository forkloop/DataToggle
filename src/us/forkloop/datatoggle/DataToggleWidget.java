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
        Method dataConnSwitchmethod, getITelephonyMethod;
        Class telephonyManagerClass, ITelephonyClass;
        Object ITelephonyStub;
        RemoteViews rv;
        AppWidgetManager wm;
        ComponentName widgetPro;
        boolean isEnabled;

        rv = new RemoteViews(context.getPackageName(), R.layout.widget);

        if (intent.getAction().equals("us.forkloop.datatoggle.TOGGLE")) {
            // http://stackoverflow.com/questions/3644144/how-to-disable-mobile-data-on-android
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
                isEnabled = true;
                Log.i("log", "enabled");
            } else {
                isEnabled = false;
                Log.d("log", "disabled");
            }
            try {
                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                // Method getITelephonyMethod;
                getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());
                if (isEnabled) {
                    dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
                    Log.i("log", "try to disable...");
                    rv.setImageViewResource(R.id.imgButton, R.drawable.princess);
                } else {
                    dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");
                    Log.i("log", "try to enable...");
                    rv.setImageViewResource(R.id.imgButton, R.drawable.emerald);
                }
                dataConnSwitchmethod.setAccessible(true);
                dataConnSwitchmethod.invoke(ITelephonyStub);
            } catch (Exception e) {
                Log.i("log", e.toString());
            }
        } else if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            Log.i("log", "connection changed");
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
                isEnabled = true;
                Log.i("log", "enabled");
            } else {
                isEnabled = false;
                Log.i("log", "disabled");
            }
            try {
                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                // Method getITelephonyMethod;
                getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());
                if (isEnabled) {
                    dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");
                    Log.i("log", "system try to enable...");
                    rv.setImageViewResource(R.id.imgButton, R.drawable.emerald);
                } else {
                    dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
                    Log.i("log", "system try to disable...");
                    rv.setImageViewResource(R.id.imgButton, R.drawable.princess);
                }
                dataConnSwitchmethod.setAccessible(true);
                dataConnSwitchmethod.invoke(ITelephonyStub);
            } catch (Exception e) {
                Log.i("log", e.toString());
            }
        }
        wm = AppWidgetManager.getInstance(context);
        widgetPro = new ComponentName(context, DataToggleWidget.class);
        int[] allWidgetIds = wm.getAppWidgetIds(widgetPro);
        for (int i = 0; i < allWidgetIds.length; i++)
            wm.updateAppWidget(allWidgetIds[i], rv);
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent toggleIntent = new Intent("us.forkloop.datatoggle.TOGGLE");
            PendingIntent pendingToggle = PendingIntent.getBroadcast(context, 0, toggleIntent, 0);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            rv.setOnClickPendingIntent(R.id.imgButton, pendingToggle);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
    }
}