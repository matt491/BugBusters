package team.bugbusters.acceleraudio;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;
import android.widget.Toast;


public class widget_big extends AppWidgetProvider {
	
	/*-- terminated_rec/play are meant to be the control type variables which receives the extras from the services --*/
	private static Intent i_record,i_play, commandIntent;
	private static boolean terminated_rec=false,terminated_play=false, pause=false;
	public static boolean service_running=false;
	private static  long currid = 1;
	
	/*-- record_widget_big and play_widget are global variables for services status knowledge --*/ 
	public static boolean record_widget_big=false,play_widget=true;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int [] appWidgetIds)
	{
		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		/* -- Handles the refreshing of the widgets if there is more that 1 instance --*/

		for (int i = 0; i < appWidgetIds.length;i++)
		{
	
		/*-- setting the view which we are going to update --*/
			
		RemoteViews view = new RemoteViews(context.getPackageName(),R.layout.widget_big_layout);
		
		/*--
		 *  Setting Intents
		 *  --*/
		
		/*--Record Intent --*/
		
		Intent start_rec = new Intent(context,widget_big.class);
		start_rec.setAction("START_STOP_REC");
		
		
		/*-- Play - Pause Intent --*/
		
		Intent start_play = new Intent(context,widget_big.class);
		start_play.setAction("START_STOP_PLAY");
		
		/*-- Previous Intent --*/
		
		Intent start_pre = new Intent (context,widget_big.class);
		start_pre.setAction("START_PRE");
		
		/*-- Forward Intent --*/
		Intent start_for = new Intent (context,widget_big.class);
		start_for.setAction("START_FOR");
		
		
		/*--
		 *  Performing the action 
		 *  --*/
		
		view.setOnClickPendingIntent(R.id.rec_big, PendingIntent.getBroadcast(context, 0, start_rec, 0));
		
		view.setOnClickPendingIntent(R.id.play_big, PendingIntent.getBroadcast(context, 0, start_play, 0));
		
		view.setOnClickPendingIntent(R.id.backward_big, PendingIntent.getBroadcast(context, 0, start_pre, 0));
		
		view.setOnClickPendingIntent(R.id.forward_big, PendingIntent.getBroadcast(context, 0, start_for, 0));
		
		/*-- Updating the widget --*/
		
		appWidgetManager.updateAppWidget(appWidgetIds[i], view);
		}
		
	}


	@Override
	public void onReceive(Context context, Intent intent)
	{

		String action = intent.getAction();
        RemoteViews rw = new RemoteViews(context.getPackageName(), R.layout.widget_big_layout); 
        
        /*-- Recording Code -- */
        
        if((action.equals("START_STOP_REC"))||(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)&&(intent.getBooleanExtra("RS",false))))
        {
        	i_record = new Intent(context, DataRecord.class);
        
        	/*-- catching the signal from DataRecord which notify  that accelerometer is unavailable --*/
        	
        	widget_lil.noaccel = intent.getBooleanExtra("NoAccelerometro", false);
        	/*-- catching the signal from DataRecord which notifies  that the recording time is expired --*/
        	
        	terminated_rec = intent.getBooleanExtra("Terminata", false);
        
        	/*-- Accelerometer unavailable notification --*/
        	
        	if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) &&  widget_lil.noaccel)
        		Toast.makeText(context, R.string.accelUnavailable , Toast.LENGTH_SHORT).show();

        	/*-- Handling the DataRecord call --*/
        
        	if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) && terminated_rec)  {
        	widget_lil.record_running=false;
        	record_widget_big=false;
        	terminated_rec=false;
    		rw.setImageViewResource(R.id.rec_big, android.R.drawable.ic_btn_speak_now);
        	Toast.makeText(context, R.string.registrationEnd , Toast.LENGTH_SHORT).show();
        	}
        
        /*-- Handling the widget start_rec call --*/
        	
        	else
        	{
        		/*-- Checking the mutually exclusive DataRecord service execution --*/
        		if(!widget_lil.record_widget_lil && !record_widget_big) {  	
        			if(widget_lil.record_running==false) {
        				widget_lil.record_running=true;
        				/*-- Widget Big is free to start the Recording Service --*/
        				
        				record_widget_big=true;
        				rw.setImageViewResource(R.id.rec_big, android.R.drawable.stat_notify_call_mute);
        				context.startService(i_record);
        			}
        			else Toast.makeText(context, R.string.alreadyRecording , Toast.LENGTH_SHORT).show();
        		}
        		else if(widget_lil.record_widget_lil)
        			Toast.makeText(context, R.string.alreadyRecording , Toast.LENGTH_SHORT).show();
        				else context.stopService(i_record);
        	}
        }/*-- End of first action filter --*/
        
        
        
        
        /*-- Play code --*/
        
        if(action.equals("START_STOP_PLAY")) {
        	
        	i_play = new Intent(context, PlayRecord.class);
    		commandIntent=new Intent();
    		commandIntent.setAction(UI4.COMMAND_RESPONSE);
    		
    		if(!play_widget)
    			Toast.makeText(context, "Riproduzione già in corso!", Toast.LENGTH_SHORT).show();
    		
        	if(!service_running && play_widget){
        		pause=false;
	    		Toast.makeText(context, "ID" +currid, Toast.LENGTH_SHORT).show();
	        	i_play.putExtra("fromUI4", false);
	        	i_play.putExtra("ID", currid);
	        	context.startService(i_play);
	        	rw.setImageViewResource(R.id.play_big, android.R.drawable.ic_media_pause);
        	}
        	
        	else if(service_running && !pause && play_widget) {
        		pause=true;
        		commandIntent.putExtra("Pausa", true);
        		commandIntent.putExtra("Riprendi", false);
        		commandIntent.putExtra("Stop", false);
        		context.sendBroadcast(commandIntent);
        		rw.setImageViewResource(R.id.play_big, android.R.drawable.ic_media_play);
        	}
        	
        	else if(service_running && pause && play_widget) {
        		pause=false;
        		commandIntent.putExtra("Pausa", false);
        		commandIntent.putExtra("Riprendi", true);
        		commandIntent.putExtra("Stop", false);
        		context.sendBroadcast(commandIntent);
        		rw.setImageViewResource(R.id.play_big, android.R.drawable.ic_media_pause);
        	}
        	

        }
        
        
        
        if(action.equals("START_PRE")) {
        	
        	
        	if(!play_widget)
    			Toast.makeText(context, "Riproduzione già in corso!", Toast.LENGTH_SHORT).show();
        	
        	if(play_widget){
        		i_play = new Intent(context, PlayRecord.class);
        		commandIntent=new Intent();
        		commandIntent.setAction(UI4.COMMAND_RESPONSE);
        		commandIntent.putExtra("Stop", true);
        		commandIntent.putExtra("Pausa", false);
        		commandIntent.putExtra("Riprendi", false);  
        		context.sendBroadcast(commandIntent);
            	context.stopService(i_play);
            	
        		currid=UI4.searchId(new DbAdapter(context), currid, UI4.PREVIOUS, -1);
        		Intent prev=new Intent(context,widget_big.class);
        		prev.setAction("START_STOP_PLAY");
        		context.sendBroadcast(prev);
	        	rw.setImageViewResource(R.id.play_big, android.R.drawable.ic_media_pause);
        	}
        	
        }
        	
            if(action.equals("START_FOR")) {
            	
            	
            	if(!play_widget)
        			Toast.makeText(context, "Riproduzione già in corso!", Toast.LENGTH_SHORT).show();
            	
            	if(play_widget){
            		i_play = new Intent(context, PlayRecord.class);
            		commandIntent=new Intent();
            		commandIntent.setAction(UI4.COMMAND_RESPONSE);
            		commandIntent.putExtra("Stop", true);
            		commandIntent.putExtra("Pausa", false);
            		commandIntent.putExtra("Riprendi", false);  
            		context.sendBroadcast(commandIntent);
                	context.stopService(i_play);	
            		currid=UI4.searchId(new DbAdapter(context), currid, UI4.NEXT, -1);
            		
	            	Intent next=new Intent(context,widget_big.class);
	            	next.setAction("START_STOP_PLAY");
	            	context.sendBroadcast(next);
            		
    	        	rw.setImageViewResource(R.id.play_big, android.R.drawable.ic_media_pause);
            	}
        	
        	
        	
        }
        
        /*-- Handling the Widget start_play call --*/
      /*  if(action.equals("START_STOP_PLAY")){
        	
        	//checking the service is not being already used by the application 
        	if(PlayRecord.play_running==false){
        			//play_running = true;
        			play_widget =true;
	            	rw.setImageViewResource(R.id.play_big, android.R.drawable.ic_media_pause);
	            	
	            	context.startService(i_play);
	        //}else Toast.makeText(context, "Playing from application, access denied" , Toast.LENGTH_SHORT).show();
        }
        else context.stopService(i_play);
        //}// 2nd filter*/
        
        /*-- Updating --*/
        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context,widget_big.class), rw);
			 super.onReceive(context, intent);
	}


	private void sendBroadcast(Intent commandIntent2) {
		// TODO Auto-generated method stub
		
	}
}
	

