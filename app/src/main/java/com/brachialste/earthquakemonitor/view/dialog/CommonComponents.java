package com.brachialste.earthquakemonitor.view.dialog;

import com.brachialste.earthquakemonitor.ApplicationManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Clase encargada de recibir un mensaje Broadcast e iniciar la pantalla de
 * diálogo general de la aplicación
 * 
 * @author brachialste
 * 
 */
public class CommonComponents extends BroadcastReceiver {

	// Debug
	private static final String TAG = "CommonComponents";
	public static final String BROADCAST = "com.brachialste.earthquakemonitor.view.dialog.CommonComponents.BROADCAST";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ApplicationManager.D){
			Log.d(TAG, "-> onReceive()");
		}
		if (intent.getAction().equals(CommonComponents.BROADCAST)) {
			String title = intent.getExtras().getString("title");
			String message = intent.getExtras().getString("message");
			String stat = intent.getExtras().getString("status");

			Intent i = new Intent(context, ShowDialogActivity.class);
			i.putExtra("title", title);
			i.putExtra("message", message);
			i.putExtra("status", stat);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}

}
