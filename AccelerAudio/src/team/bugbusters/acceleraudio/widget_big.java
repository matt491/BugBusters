package team.bugbusters.acceleraudio;

import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

public class widget_big extends AppWidgetProvider {
	
	/*-- Declaring intents/services we are going to use --*/
	private static boolean timeout=false;
	private static Intent i_record;
	private static String status;
	private static boolean recording = false;
	private static boolean terminated=false;
	public static boolean record_running=false, record_widget=false;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int [] appWidgetIds)
	{
		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		/* -- Handles the refreshing of the widgets and/or his components --*/

		
		for (int i = 0; i < appWidgetIds.length;i++)
		{
	
		/*-- setting the view which we are going to update --*/
			
		RemoteViews view = new RemoteViews(context.getPackageName(),R.layout.widget_big_layout);
		
		/*-- Intents --*/
		
		Intent start_rec = new Intent(context,widget_big.class);
		start_rec.setAction("START_STOP_REC");
		
		/*-- Pending intents that we want to immediately starts af the first istance --*/
		
		/*-- Performing the action --*/
		
		view.setOnClickPendingIntent(R.id.rec_big, PendingIntent.getBroadcast(context, 0, start_rec, 0));
		//view.setOnClickPendingIntent(R.id.rec_big, PendingIntent.getBroadcast(context, 0, irec, 0));
		
		/*-- Updating the widget --*/
		appWidgetManager.updateAppWidget(appWidgetIds[i], view);
		}
		
	}


	@Override
	public void onReceive(Context context, Intent intent)
	{
		
		String action = intent.getAction();
	
        RemoteViews rw = new RemoteViews(context.getPackageName(), R.layout.widget_big_layout); 
        
        /*-- calling the DataRecord.class -- */
        
        i_record = new Intent(context, DataRecord.class);
        
        
        /*-- catching the signal from DataRecord which notifies  that the recording time is expired --*/
        
        terminated = intent.getBooleanExtra("Terminata", false);

        /*-- finish recording --*/
        
        if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) && terminated)  {
        	record_running=false;
        	record_widget=false;
        	terminated=false;
    		rw.setImageViewResource(R.id.rec_big, android.R.drawable.ic_btn_speak_now);
        	Toast.makeText(context, R.string.registrationEnd , Toast.LENGTH_SHORT).show();
        }
        
        /*-- Start and Stop check --*/
        
        if(action.equals("START_STOP_REC")){
        	if(!record_widget) {  	
            	if(record_running==false) {
	            	record_running=true;
	            	record_widget=true;
	            	rw.setImageViewResource(R.id.rec_big, android.R.drawable.ic_media_pause);
	            	context.startService(i_record);
            	}
            	else Toast.makeText(context, R.string.alreadyRecording , Toast.LENGTH_SHORT).show();
        	}
        	else {	
        		context.stopService(i_record);
        	}
        }

			 AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context,widget_big.class), rw);
			 super.onReceive(context, intent);
	}
}
