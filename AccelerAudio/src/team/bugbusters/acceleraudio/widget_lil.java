package team.bugbusters.acceleraudio;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

public class widget_lil extends AppWidgetProvider {
	private static Intent i_record, i_pref;
	private static boolean terminated=false;
	public static boolean record_running=false, record_widget_lil=false, noaccel=false;
	
	
	@Override
	/*-- Handles the first instance of the widget --*/
	public void onEnabled(Context context)
	{
		super.onEnabled(context);

	}
	/*-- Handles the destruction event --*/
	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		super.onDeleted(context, appWidgetIds);
		if(record_running && record_widget_lil)
			context.stopService(new Intent(context, DataRecord.class));

	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int [] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		/* 
		 * -- Handles the refreshing of widget's instances, not necessary if there always is only 1 instance of the widget at time 
		 * --*/
		for (int i = 0; i < appWidgetIds.length;i++)
		{
			int Id = appWidgetIds[i];
			
			/*-- Remote View Reference --*/
			
			RemoteViews view = new RemoteViews (context.getPackageName(), R.layout.widget_lil_layout);
			
			/*-- Intents for start and stop actions --*/
			
			Intent start = new Intent (context, widget_lil.class);
			start.setAction("START_STOP_REC");
	
			Intent pref = new Intent (context, widget_lil.class);
			pref.setAction("PREF");
			
			/*-- OnClick events calling start and stop intents --*/
	     	view.setOnClickPendingIntent(R.id.pref_lil, PendingIntent.getBroadcast(context, 0,  pref, 0));
			view.setOnClickPendingIntent(R.id.rec_lil, PendingIntent.getBroadcast(context, 0, start, 0));
				
			/*-- Update the widget --*/
			
			appWidgetManager.updateAppWidget(Id, view);
			
		}
	}


	@Override
    public void onReceive(Context context, Intent intent)
    {
			String action = intent.getAction();
		
            RemoteViews rw = new RemoteViews(context.getPackageName(), R.layout.widget_lil_layout); 
            
            /*-- calling the DataRecord.class -- */
            
            i_record = new Intent(context, DataRecord.class);
            i_record.putExtra("fromLIL", true);
            
            /*-- calling the UI5.class (Preferences) -- */
            
            i_pref = new Intent(context, UI5.class);
            i_pref.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i_pref.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i_pref.putExtra("prefFromWidget", true);
            
            /*-- Catching the signal from DataRecord which notify that the recording time is expired --*/
            
            terminated = intent.getBooleanExtra("Terminata", false);
            
            /*-- Catching the signal from DataRecord which notify that accelerometer is unavailable --*/
            
            noaccel = intent.getBooleanExtra("NoAccelerometro", false);
            
            /*-- Accelerometer unavailable notification --*/
            
            if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) && noaccel)
            	Toast.makeText(context, R.string.accelUnavailable , Toast.LENGTH_SHORT).show();
 
            /*-- Finish recording --*/
            
            if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) && terminated)  {
            	record_running=false;
            	record_widget_lil=false;
            	terminated=false;
        		rw.setImageViewResource(R.id.rec_lil, R.drawable.rec);
            	Toast.makeText(context, R.string.registrationEnd , Toast.LENGTH_SHORT).show();
            }
            
            /*-- Start and Stop check --*/
            
            if(action.equals("START_STOP_REC")){
            	if(!record_widget_lil && !widget_big.record_widget_big) {  	
	            	if(record_running==false) {
		            	record_running=true;
		            	record_widget_lil=true;
		            	rw.setImageViewResource(R.id.rec_lil, R.drawable.stop);
		            	context.startService(i_record);
	            	}
	            	else Toast.makeText(context, R.string.alreadyRecording , Toast.LENGTH_SHORT).show();
            	}
            	else if (widget_big.record_widget_big)
            		Toast.makeText(context, R.string.alreadyRecording , Toast.LENGTH_SHORT).show();
            	
            	else context.stopService(i_record);
            }
                
            
            /*-- Preferences check --*/
            
            if(action.equals("PREF")) 
            	context.startActivity(i_pref);

     
            /*-- Update  --*/
            
            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context,widget_lil.class), rw);

        super.onReceive(context, intent);
    }

}

