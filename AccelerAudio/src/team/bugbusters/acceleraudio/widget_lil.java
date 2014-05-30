package team.bugbusters.acceleraudio;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class widget_lil extends AppWidgetProvider {
	private static Intent serviceIntent;
	private static boolean serviceRunning =false, b=false,c=false,d=false;
	public static String TOGGLE_WINET = "ToggleWiNetService";
	@Override
	/*-- Handles the first instance of the widget --*/
	public void onEnabled(Context context)
	{
		super.onEnabled(context);
		Toast.makeText(context,"Lanciato", Toast.LENGTH_SHORT).show();
	}
	/*-- Handles the destruction event --*/
	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		super.onDeleted(context, appWidgetIds);
		Toast.makeText(context,"Terminato", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int [] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		/* -- Handles the refreshing of the widgets and/or his components --*/
		for (int i = 0; i < appWidgetIds.length;i++)
		{
			int Id = appWidgetIds[i];
			
			/*-- Remote View Reference --*/
			
			RemoteViews view = new RemoteViews (context.getPackageName(), R.layout.widget_lil_layout);
			

			/*-- Setting REC  Intent --*/
			
			//Intent irec = new Intent(context,DataRecord.class);
			//Intent iirec = new Intent(context,widget_lil.class);
						
			/*-- Pending intents that we want to immediately starts af the first istance --*/
			
			//PendingIntent prec = PendingIntent.getService(context, 0, irec, 0); Actually starts the intent
			
			/*-- Toggling Intent --*/
			
			/*--Intent newIntent = new Intent(context, widget_lil.class);
            newIntent.setAction(TOGGLE_WINET); --*/
			
			/*-- Action Performed --*/
			
            /*-- Starts the intent only with the onclick event--*/
            
			/*--
			 * 
			 *  The only way (that i figure out) to manage the first launch of the application is to instantiate the onReceive calling intents  in the OnClick event
			 * setting it previously with the new keyword such as: Intent ex = new Intent( . . . , . . . ) triggers the onReceive when we don't want to
			 * 
			--*/
			
			Intent start=new Intent (context,widget_lil.class);
			start.setAction("START_REC");
			
			Intent stop=new Intent (context,widget_lil.class);
			stop.setAction("STOP_REC");

			
				view.setOnClickPendingIntent(R.id.stop_lil, PendingIntent.getBroadcast(context, 0,  stop, 0));
				view.setOnClickPendingIntent(R.id.rec_lil, PendingIntent.getBroadcast(context, 0, start, 0));
				
			
			/*-- Update the widget --*/
			
			appWidgetManager.updateAppWidget(Id, view);
			
		}
	}


	@Override
    public void onReceive(Context context, Intent intent)
    {
		String action=intent.getAction();
		
            RemoteViews rw = new RemoteViews(context.getPackageName(), R.layout.widget_lil_layout); 
            serviceIntent = new Intent(context, DataRecord.class);
            
            b=intent.getBooleanExtra("TempoScaduto", false);
 
            if(b)  {
            	b=false;
            	Toast.makeText(context,"Registrazione terminata" , Toast.LENGTH_SHORT).show();
            	rw.setViewVisibility(R.id.rec_lil, View.VISIBLE);
                rw.setViewVisibility(R.id.stop_lil, View.VISIBLE);
            }
            
            
            if(action.equals("START_REC")){
            	context.startService(serviceIntent);
            	rw.setViewVisibility(R.id.stop_lil, View.VISIBLE);
                rw.setViewVisibility(R.id.rec_lil, View.INVISIBLE);
            }
            	
            if(action.equals("STOP_REC")){
            	context.stopService(serviceIntent);
            	rw.setViewVisibility(R.id.rec_lil, View.VISIBLE);
                rw.setViewVisibility(R.id.stop_lil, View.VISIBLE);
            }
            

            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context,widget_lil.class), rw);

        super.onReceive(context, intent);
    }

}

/*--
	public static String TOGGLE_WINET = "ToggleWiNetService";
    private static boolean serviceRunning = false;
    private static Intent serviceIntent;
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        serviceIntent = new Intent(context, WiNetService.class);

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_winet);

            remoteViews.setViewVisibility(R.id.buttonWidgetLoading, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.buttonWidgetStartService, View.VISIBLE);

            Intent newIntent = new Intent(context, WiNetWidget.class);
            newIntent.setAction(TOGGLE_WINET);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.buttonWidgetStartService, pendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.buttonWidgetStopService, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            Log.i(TOGGLE_WINET, "updated");
        }
    }

@Override
public void onReceive(Context context, Intent intent) {
    if(intent.getAction().equals(TOGGLE_WINET)) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_winet);

        // Create a fresh intent 
        Intent serviceIntent = new Intent(context, WiNetService.class);

        if(serviceRunning) {
            context.stopService(serviceIntent);
            remoteViews.setViewVisibility(R.id.buttonWidgetStartService, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.buttonWidgetStopService, View.INVISIBLE);
            Toast.makeText(context, "serviceStopped", Toast.LENGTH_SHORT).show();
        } else {
            context.startService(serviceIntent);
            remoteViews.setViewVisibility(R.id.buttonWidgetStopService, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.buttonWidgetStartService, View.INVISIBLE);
            Toast.makeText(context, "serviceStarted", Toast.LENGTH_SHORT).show();
        }
        serviceRunning=!serviceRunning;
        ComponentName componentName = new ComponentName(context, WiNetWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    }
    super.onReceive(context, intent);
}

--*/
