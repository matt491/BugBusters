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
	
	Intent iplay,irec,iforward,iback;
	
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
		Toast.makeText(context,"Lunghezza" + appWidgetIds.length, Toast.LENGTH_SHORT).show();
	
		/*-- setting the view which we are going to update --*/
		
		RemoteViews view = new RemoteViews(context.getPackageName(),R.layout.widget_big_layout);

		/*-- Intents --*/
		
		iplay = new Intent (context,PlayRecord.class);
		irec = new Intent(context,DataRecord.class);
		
		/*-- Pending intents that we want to immediately starts af the first istance --*/
		
		/*-- Performing the action --*/
		
		view.setOnClickPendingIntent(R.id.play_big, PendingIntent.getService(context, 0, iplay, 0));
		view.setOnClickPendingIntent(R.id.rec_big, PendingIntent.getService(context, 0, irec, 0));
		
		/*-- Updating the widget --*/
		appWidgetManager.updateAppWidget(appWidgetIds[0], view);
		}
		
	}

}
