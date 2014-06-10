package team.bugbusters.acceleraudio;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;


public class widget_big extends AppWidgetProvider {
	
	/*-- terminated_rec/play are meant to be the control type variables which receives the extras from the services --*/
	private static Intent i_record,i_play, commandIntent;
	private static boolean terminated_rec=false;
	public static boolean service_running=false, pause=true;
	public static  int currid = 1;
	private static Cursor c;
	private static DbAdapter db;
//	private static int lastposition=-1;
	private static boolean PRIMA_VOLTA=true;
	public static boolean delete=false;
	private static long starttime=System.currentTimeMillis();
	private SharedPreferences prefs;
	
	/*-- record_widget_big and play_widget are global variables for services status knowledge --*/ 
	public static boolean record_widget_big=false,play_widget=true;
	
	/*-- Handles the destruction event --*/
	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		super.onDeleted(context, appWidgetIds);
		PRIMA_VOLTA=true;
		if(play_widget && service_running) {
			commandIntent=new Intent();
    		commandIntent.setAction(UI4.COMMAND_RESPONSE);
			commandIntent.putExtra("Pausa", false);
    		commandIntent.putExtra("Riprendi", false);
    		commandIntent.putExtra("Stop", true);
    		context.sendBroadcast(commandIntent);
		}
		Toast.makeText(context,"Distrutto", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onEnabled(Context context)
	
	{
		super.onEnabled(context);
		PRIMA_VOLTA=true;
	}
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
		//db.close();
		/*-- Updating the widget --*/
		
		appWidgetManager.updateAppWidget(appWidgetIds[i], view);
		}
		
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{

		String action = intent.getAction();
        RemoteViews rw = new RemoteViews(context.getPackageName(), R.layout.widget_big_layout); 
        
/*------------------------------------------------------------------------------------------------------------------------------------------------*/
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
        }
/*------------------------------------------------------------------------------------------------------------------------------------------------*/
        else
        {
        /*-- PlayRecord Code --*/
        	/*-- DB Emptiness check --*/
        	
        	db = new DbAdapter(context);
        	db.open();
        	c = db.fetchAllRecord();
        	if(c.getCount()==0)
        	{
        		Toast.makeText(context, "DataBase Empty from Receiver", Toast.LENGTH_SHORT).show();
        		rw.setTextViewText(R.id.title_w_big, "Title");
        		rw.setTextViewText(R.id.duration_big, "0,00 s");
        		rw.setTextViewText(R.id.modify_big, "Last Modified");
        		rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_launcher1);
        		rw.setInt(R.id.image_big, "setBackgroundColor", Color.BLACK);
        		/*-- First instance of the widget after deleting all records --*/
        		PRIMA_VOLTA=true;
        		
        	}
        	else
        	{
        		
        		switch(currentSorting(context)) {
        		case UI4.BY_NAME:
        			c = db.fetchAllRecordSortedByName(); //lastposition=-1;
        			break;
        			case UI4.BY_DATE:
        			c = db.fetchAllRecordSortedByDate();// lastposition=-1;
        			break;
        			case UI4.BY_DURATION:
        			c = db.fetchAllRecordSortedByDuration();// lastposition=-1;
        			break;
        			case UI4.BY_INSERTION:  //case BY_INSERTION
        			c = db.fetchAllRecord();// lastposition=-1;
        			break;
        		}
        	
    		/*-- First Call--*/
        		
        		if(PRIMA_VOLTA)
        		{	
        			PRIMA_VOLTA=false;
        			Toast.makeText(context, "Porcodio!!!", Toast.LENGTH_SHORT).show();
        			c.moveToFirst();
        			currid=(int)c.getLong(c.getColumnIndex(DbAdapter.KEY_RECORDID));
        			rw.setTextViewText(R.id.title_w_big,c.getString(c.getColumnIndex(DbAdapter.KEY_NAME)));
        			rw.setTextViewText(R.id.modify_big,c.getString(c.getColumnIndex(DbAdapter.KEY_LAST)));
        			float dur=(float) (c.getInt(c.getColumnIndex(DbAdapter.KEY_DURATION)))/1000;
        			rw.setTextViewText(R.id.duration_big, String.format("%.2f s", dur));
        			String thumb=c.getString(c.getColumnIndex(DbAdapter.KEY_IMM));
        			int alpha = Integer.parseInt(thumb.substring(0, 3));
        			int red = Integer.parseInt(thumb.substring(3, 6));
        			int green = Integer.parseInt(thumb.substring(6, 9));
        			int blue = Integer.parseInt(thumb.substring(9, 12));
        			rw.setInt(R.id.image_big, "setBackgroundColor", Color.argb(alpha, red, green, blue));
        			switch(Integer.parseInt(thumb.substring(11))) {
	        			case 0:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_0);
		    				break;
		    			case 1:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_1);
		    				break;
		    			case 2:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_2);
		    				break;
		    			case 3:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_3);
		    				break;
		    			case 4:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_4);
		    				break;
		    			case 5: 
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_5);
		    				break;
		    			case 6:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_6);
		    				break;
		    			case 7:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_7);
		    				break;
		    			case 8:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_8);
		    				break;
		    			case 9:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_9);
		    				break;
	    			}
        		}
    		
        /*-- Play code --*/
        
        if(action.equals("START_STOP_PLAY")) {
        	if(System.currentTimeMillis()-starttime>300) {
        		starttime=System.currentTimeMillis();
	        	i_play = new Intent(context, PlayRecord.class);
	    		commandIntent=new Intent();
	    		commandIntent.setAction(UI4.COMMAND_RESPONSE);
	    		
	    		if(!play_widget)
	    			Toast.makeText(context, "Riproduzione gi� in corso!", Toast.LENGTH_SHORT).show();
	    		
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
        	
        	}/*--Start Stop play end --*/
        
        if(action.equals("START_PRE")) {
        	if(System.currentTimeMillis()-starttime>300) {
        		starttime=System.currentTimeMillis();
	        	if(!play_widget)
	    			Toast.makeText(context, "Riproduzione gi� in corso!", Toast.LENGTH_SHORT).show();
	        	
	        	if(play_widget)
	        	{
	        		i_play = new Intent(context, PlayRecord.class);
	        		commandIntent=new Intent();
	        		commandIntent.setAction(UI4.COMMAND_RESPONSE);
	        		commandIntent.putExtra("Stop", true);
	        		commandIntent.putExtra("Pausa", false);
	        		commandIntent.putExtra("Riprendi", false);  
	        		context.sendBroadcast(commandIntent);
	            	context.stopService(i_play);
	            	service_running=false;
	            	pause=true;
	            	rw.setImageViewResource(R.id.play_big, android.R.drawable.ic_media_play);
	        		currid=UI4.searchId(new DbAdapter(context), currid, UI4.PREVIOUS, currentSorting(context)/*intent.getIntExtra("WAY", -1)*/);
	        		c=db.fetchRecordById(currid);
	        		c.moveToNext();
	            	rw.setTextViewText(R.id.title_w_big,c.getString(c.getColumnIndex(DbAdapter.KEY_NAME)));
	        		rw.setTextViewText(R.id.modify_big,c.getString(c.getColumnIndex(DbAdapter.KEY_LAST)));
	        		float dur=(float) (c.getInt(c.getColumnIndex(DbAdapter.KEY_DURATION)))/1000;
	    			rw.setTextViewText(R.id.duration_big, String.format("%.2f s", dur));
	        		String thumb=c.getString(c.getColumnIndex(DbAdapter.KEY_IMM));
	    			int alpha = Integer.parseInt(thumb.substring(0, 3));
	    	        int red = Integer.parseInt(thumb.substring(3, 6));
	    	        int green = Integer.parseInt(thumb.substring(6, 9));
	    	        int blue = Integer.parseInt(thumb.substring(9, 12));
	    	        rw.setInt(R.id.image_big, "setBackgroundColor", Color.argb(alpha, red, green, blue));
	    	        switch(Integer.parseInt(thumb.substring(11))) {
		    			case 0:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_0);
		    				break;
		    			case 1:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_1);
		    				break;
		    			case 2:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_2);
		    				break;
		    			case 3:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_3);
		    				break;
		    			case 4:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_4);
		    				break;
		    			case 5: 
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_5);
		    				break;
		    			case 6:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_6);
		    				break;
		    			case 7:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_7);
		    				break;
		    			case 8:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_8);
		    				break;
		    			case 9:
		    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_9);
		    				break;
		    		}
	        		}
        		}
        	}/*--  End Start Pre --*/
        	
            if(action.equals("START_FOR")|| (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) && delete)) {
            	delete=false;
            	if(System.currentTimeMillis()-starttime>300) {
            		starttime=System.currentTimeMillis();
            	
	            	if(!play_widget)
	        			Toast.makeText(context, "Riproduzione gi� in corso!", Toast.LENGTH_SHORT).show();
	            	
	            	if(play_widget){
	            		i_play = new Intent(context, PlayRecord.class);
	            		
	            		commandIntent=new Intent();
	            		commandIntent.setAction(UI4.COMMAND_RESPONSE);
	            		commandIntent.putExtra("Stop", true);
	            		commandIntent.putExtra("Pausa", false);
	            		commandIntent.putExtra("Riprendi", false);  
	            		context.sendBroadcast(commandIntent);
	                	context.stopService(i_play);
	                	service_running=false;
	                	pause=true;
	                	rw.setImageViewResource(R.id.play_big, android.R.drawable.ic_media_play);
	            		currid=UI4.searchId(new DbAdapter(context), currid, UI4.NEXT,  currentSorting(context)/*intent.getIntExtra("WAY", -1)*/);
	                	c=db.fetchRecordById(currid);
	            		
	            		c.moveToNext();

		            	rw.setTextViewText(R.id.title_w_big,c.getString(c.getColumnIndex(DbAdapter.KEY_NAME)));
		        		rw.setTextViewText(R.id.modify_big,c.getString(c.getColumnIndex(DbAdapter.KEY_LAST)));
		        		float dur=(float) (c.getInt(c.getColumnIndex(DbAdapter.KEY_DURATION)))/1000;
		    			rw.setTextViewText(R.id.duration_big, String.format("%.2f s", dur));
		        		String thumb=c.getString(c.getColumnIndex(DbAdapter.KEY_IMM));
		    			int alpha = Integer.parseInt(thumb.substring(0, 3));
		    	        int red = Integer.parseInt(thumb.substring(3, 6));
		    	        int green = Integer.parseInt(thumb.substring(6, 9));
		    	        int blue = Integer.parseInt(thumb.substring(9, 12));
		    	        rw.setInt(R.id.image_big, "setBackgroundColor", Color.argb(alpha, red, green, blue));
		    	        switch(Integer.parseInt(thumb.substring(11))) {
			    			case 0:
			    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_0);
			    				break;
			    			case 1:
			    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_1);
			    				break;
			    			case 2:
			    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_2);
			    				break;
			    			case 3:
			    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_3);
			    				break;
			    			case 4:
			    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_4);
			    				break;
			    			case 5: 
			    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_5);
			    				break;
			    			case 6:
			    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_6);
			    				break;
			    			case 7:
			    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_7);
			    				break;
			    			case 8:
			    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_8);
			    				break;
			    			case 9:
			    				rw.setInt(R.id.image_big, "setImageResource", R.drawable.ic_music_9);
			    				break;
			    			}
	            		}
	            	}
            	}/*--  End Start For --*/
           
           db.close();
    		}
    	}
        /*-- Updating --*/
        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context,widget_big.class), rw);
			 super.onReceive(context, intent);
			 
				
			 
	}

	private int currentSorting(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int currentSorting = UI4.BY_INSERTION; //Default
		
		if(prefs.getBoolean("sortedByName", false)) {
			currentSorting = UI4.BY_NAME;
		}
		else if(prefs.getBoolean("sortedByDate", false)) {
			currentSorting = UI4.BY_DATE;
		}
		else if(prefs.getBoolean("sortedByDuration", false)) {
			currentSorting = UI4.BY_DURATION;
		}
		
		return currentSorting;
	}

	
}
	

