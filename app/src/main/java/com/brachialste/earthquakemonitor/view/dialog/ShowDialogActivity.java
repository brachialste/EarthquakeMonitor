package com.brachialste.earthquakemonitor.view.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.brachialste.earthquakemonitor.ApplicationManager;

/**
 * Clase encargada de mostrar un diálogo sin interferir con el hilo principal de
 * la aplicación
 * 
 * @author brachialste
 * 
 */
public class ShowDialogActivity extends Activity {

	// Debug
	private static final String TAG = "ShowDialogActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ApplicationManager.D) {
			Log.d(TAG, "-> onCreate()");
		}

		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Make us non-modal, so that others can receive touch events.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

		// ...but notify us that it happened.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		String title = getIntent().getExtras().getString("title");
		String message = getIntent().getExtras().getString("message");
		String stat = getIntent().getExtras().getString("status");

		// creamos el fragment del dialogo de alerta
		DialogFragment alertDialogFragment = AlertDialogFragment.newInstance(title, message, stat);
		if(alertDialogFragment != null) {
			alertDialogFragment.show(getFragmentManager(), "alert_dialog");
		}

	}

	@Override
	public void onBackPressed() {
		// do nothing.
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		ShowDialogActivity.this.finish();
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(ApplicationManager.D){
            Log.d(TAG, "-> onConfigurationChanged()");
        }
        // recarga la Actividad
    }

}
