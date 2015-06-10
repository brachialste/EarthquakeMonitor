package com.brachialste.earthquakemonitor;

import android.app.Application;

import com.brachialste.earthquakemonitor.security.PRNGFixes;
import com.brachialste.earthquakemonitor.view.FontsOverride;

/**
 * Clase encargada de gestionar el comportamiento de la aplicación en general
 * 
 * @author brachialste
 * 
 */
public class ApplicationManager extends Application {
	// Debug
	public static final boolean D = true; // variable global para el debug
//	public static final boolean D = false; // TODO: PRODUCCIÓN

    // variable que indica el valor de la versión
    public static int appVersionCode = 1;

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// aplicamos el parche par ala corrección de la generación de números aleatorios
		PRNGFixes.apply();

		// sobrescribimos los tipos de letra
		FontsOverride.setDefaultFont(this, "DEFAULT", "Roboto_Regular.ttf");
		FontsOverride.setDefaultFont(this, "MONOSPACE", "Roboto_Light.ttf");
		FontsOverride.setDefaultFont(this, "SANS_SERIF", "Roboto_Thin.ttf");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
