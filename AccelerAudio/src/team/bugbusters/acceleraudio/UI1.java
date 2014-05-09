package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

public class UI1 extends Activity {

	
	
	
	
	/*
	 * Alla pressione del tasto back viene notificato all'utente che l'applicazione sta per chiudersi
	 * 
	 */
	@Override
	public void onBackPressed() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UI1.this);
		
		
		alertDialogBuilder.setTitle(R.string.alertTitle);
		alertDialogBuilder.setMessage(R.string.alertMessage);
		
		alertDialogBuilder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				UI1.this.finish();
			}
		});
		
		alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
}
