package com.brachialste.earthquakemonitor.web.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.R;
import com.brachialste.earthquakemonitor.utils.Utilities;
import com.brachialste.earthquakemonitor.view.dialog.CommonComponents;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Clase encargada de verificar la conectividad con Internet y con el Web
 * Service
 *
 * @author brachialste
 *
 */
public class ConnectionDetector {

    // Debug
    private static final String TAG = "ConnectionDetector";

    private boolean connection = true;

    private Context _context;

    // referencia a la instancia actual del ServiciosManager
    private static ConnectionDetector con_det;
    /**
     * Método encargado de obteener la instancia actual del ConnectionDetector
     * @param context
     * @return
     */
    public static synchronized ConnectionDetector getInstance(Context context) {
        if (con_det == null) {
            con_det = new ConnectionDetector(context);
        }

        return con_det;
    }

    /**
     * Constructor de la clase
     *
     * @param context
     *            Contexto de la aplicacion
     */
    public ConnectionDetector(Context context) {
        this._context = context;
    }

    /**
     * Método que verifica si existe conexión a Internet
     *
     * @return <b>true</b> si existe conexión, <b>false</b> si no hay conexión
     */
    public boolean isConnectingToInternet() {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> isConnectingToInternet()");
        }
        // obtenemos el servicio de conectividad
        ConnectivityManager connectivity = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            // obtenemos toda la inforamción de la red
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {// si
                        // esta
                        // conectada
                        // entonces
                        // regresa
                        // true
                        if (ApplicationManager.D) {
                            Log.d(TAG, "Connected: true");
                        }
                        connection = true;
                        return true;
                    }
                }
            }

        }
        if (ApplicationManager.D) {
            Log.d(TAG, "Connected: false");
        }
        if (connection == true) {
            connection = false;
            // no hay Internet
            Intent i = new Intent();
            i.putExtra("title",
                    _context.getString(R.string.internet_fail_title));
            i.putExtra("message",
                    _context.getString(R.string.internet_fail_message));
            i.putExtra("status", "false");
            i.setAction(CommonComponents.BROADCAST);
            _context.sendBroadcast(i);
        }

        return false;
    }

    /**
     * Método para saber si el dispositivo se encuentra conectado a la red o no
     * @return
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Método que valida si se tiene una conexión de internet activa [HACK]
     * @return
     */
    public boolean hasInternetAccess() {
        if (isOnline()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(TAG, "No network available!");
        }
        return false;
    }

}