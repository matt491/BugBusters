package team.bugbusters.acceleraudio;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class widget_lil extends AppWidgetProvider {
	private static Intent i_record;
	private static boolean terminated=false;
	public static boolean record_running=false, record_widget=false;
	
	@Override
	/*-- Handles the first instance of the widget --*/
	public void onEnabled(Context context)
	{
		super.onEnabled(context);
		Toast.makeText(context,"Creato", Toast.LENGTH_SHORT).show();
	}
	/*-- Handles the destruction event --*/
	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		super.onDeleted(context, appWidgetIds);
		Toast.makeText(context,"Distrutto", Toast.LENGTH_SHORT).show();
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
			view.setViewVisibility(R.id.stop_lil, View.INVISIBLE);
			
			/*-- Intents for start and stop actions --*/
			
			Intent start = new Intent (context,widget_lil.class);
			start.setAction("START_REC");
	
			Intent stop = new Intent (context,widget_lil.class);
			stop.setAction("STOP_REC");
			
			/*-- OnClick events calling start and stop intents --*/
			view.setOnClickPendingIntent(R.id.stop_lil, PendingIntent.getBroadcast(context, 0,  stop, 0));
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
            
            /*-- catching the signal from DataRecord which notify  that the recording time is expired --*/
            
            terminated = intent.getBooleanExtra("Terminata", false);
 
            /*-- finish recording --*/
            
            if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) && terminated)  {
            	record_running=false;
            	record_widget=false;
            	terminated=false;
            	Toast.makeText(context, R.string.registrationEnd , Toast.LENGTH_SHORT).show();
            	rw.setViewVisibility(R.id.rec_lil, View.VISIBLE);
                rw.setViewVisibility(R.id.stop_lil, View.INVISIBLE);
            }
            
            /*-- Start check --*/
            
            if(action.equals("START_REC")){
            	if(record_running==false) {
	            	record_running=true;
	            	record_widget=true;
	            	context.startService(i_record);
	            	rw.setViewVisibility(R.id.stop_lil, View.VISIBLE);
	                rw.setViewVisibility(R.id.rec_lil, View.INVISIBLE);
            	}
            	else Toast.makeText(context, R.string.alreadyRecording , Toast.LENGTH_SHORT).show();
            }
                
            
            /*-- Stop check --*/
            
            if(action.equals("STOP_REC")){
            	context.stopService(i_record);
            	rw.setViewVisibility(R.id.rec_lil, View.VISIBLE);
                rw.setViewVisibility(R.id.stop_lil, View.INVISIBLE);
            }

            
            
            /*-- Update  --*/
            
            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context,widget_lil.class), rw);

        super.onReceive(context, intent);
    }

}

