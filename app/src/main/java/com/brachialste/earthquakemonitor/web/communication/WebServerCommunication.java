package com.brachialste.earthquakemonitor.web.communication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.R;
import com.brachialste.earthquakemonitor.view.dialog.CommonComponents;
import com.brachialste.earthquakemonitor.web.utils.ConnectionDetector;

import org.json.JSONArray;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

/**
 * Clase encargada de la ejecución del servicio web de conexión con SGC Web
 * Created by brachialste on 28/10/13.
 */
public class WebServerCommunication {

    // Debug
    private static final String TAG = "WebServerCommunication";

    private static final int METHOD_TRACE = 2;

    // objeto que permite revisar si existe conexión a Internet
    private ConnectionDetector cd;
    // contexto de la aplicación
    private Context context;
    // servicio de conexión
    private USGSService usgsService = null;
    // singleton
    private static WebServerCommunication webServerComm;

    public USGSService getUsgsService() {
        return usgsService;
    }

    public void setUsgsService(USGSService usgsService) {
        this.usgsService = usgsService;
    }

    /**
     * Instancia de la conexión web
     * @param context
     * @return
     */
    public static synchronized WebServerCommunication getInstance(Context context){
        if (webServerComm == null){
            webServerComm = new WebServerCommunication(context);
        }
        return webServerComm;
    }

    /**
     * Constructor de la clase Se intenta conectar desde un unicio con el Web
     * Service
     *
     * @param context
     */
    private WebServerCommunication(Context context) {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> SGCWebDirServerCommunication()");
        }
        this.context = context;

        // creamos un nuevo objeto para detectar la conexión a Internet
        cd = new ConnectionDetector(context);
        // generamos un nuevo servicio de conexión con el Web Service
        usgsService = USGSService.getInstance();
    }

    /**
     * Método encargado de obtener datos del Web Service
     */
    public JSONArray enviarPeticion() {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> enviarPeticion()");
        }
        JSONArray respuesta = null;
        // validamos que exista conexión a Internet y que el servicio Web este
        // activo
        if (cd.isConnectingToInternet() /* && cd.isWebServiceActive(URL) */) {
            try {
                respuesta = usgsService.procesarPeticion();
            } catch (MalformedURLException e) {
                String message = context.getString(R.string.err_web6);
                Log.e(TAG, message);
                // expiro la conexion
                Intent i = new Intent();
                i.putExtra("title",
                        context.getString(R.string.internet_fail_title));
                i.putExtra("message", message);
                i.putExtra("status", "false");
                i.setAction(CommonComponents.BROADCAST);
                context.sendBroadcast(i);
            } catch (UnknownHostException e) {
                String message = "Servidor desconocido";
                Log.e(TAG, message);
                // expiro la conexion
                Intent i = new Intent();
                i.putExtra("title",
                        context.getString(R.string.internet_fail_title));
                i.putExtra("message", message);
                i.putExtra("status", "false");
                i.setAction(CommonComponents.BROADCAST);
                context.sendBroadcast(i);
            } catch (IOException e) {
                String message = context.getString(R.string.err_web4)
                        + " [ "
                        + Thread.currentThread().getStackTrace()[METHOD_TRACE]
                        .getMethodName() + " ]";
                Log.e(TAG, message);
                // expiro la conexion
                Intent i = new Intent();
                i.putExtra("title",
                        context.getString(R.string.internet_fail_title));
                i.putExtra("message", message);
                i.putExtra("status", "false");
                i.setAction(CommonComponents.BROADCAST);
                context.sendBroadcast(i);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Exception: " + e.getMessage());
            }

        }
        return respuesta;
    }

}
