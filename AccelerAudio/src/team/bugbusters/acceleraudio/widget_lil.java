package team.bugbusters.acceleraudio;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

public class widget_lil extends AppWidgetProvider {
	
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
			RemoteViews view = new RemoteViews (context.getPackageName(), R.layout.widget_lil_layout);
			
			/*-- Setting  Intent --*/
			Intent irec = new Intent(context,DataRecord.class);
			
			/*-- Pending Intents --*/
			
			PendingIntent prec = PendingIntent.getService(context, 0, irec, 0);
			
			/*-- Action Performed --*/
			
			view.setOnClickPendingIntent(R.id.rec_lil, prec);
			
			/*-- Update the widget --*/
			
			appWidgetManager.updateAppWidget(Id, view); 
			
		}
		
	}

}
