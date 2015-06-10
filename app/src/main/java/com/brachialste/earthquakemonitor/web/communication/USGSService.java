package com.brachialste.earthquakemonitor.web.communication;

import android.util.Log;

import com.brachialste.earthquakemonitor.db.DataSQLite;
import com.brachialste.earthquakemonitor.ApplicationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Servicio Web para SGC Móvil Dir en SGC WEB
 * Created by brachialste on 28/10/13.
 */
public class USGSService {

    // Debug
    private static final String TAG = "USGSService";

    // variable para asignar la URL del Web Service
    private final String urlWebService = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.geojson";

    public static SimpleDateFormat GMTISOTimeFormatter = new SimpleDateFormat(DataSQLite.ISO_8601_FORMAT);
    public static SimpleDateFormat LocaleTimeFormatter = new SimpleDateFormat(DataSQLite.DATE_SQLITE_FORMAT);

    private static USGSService webService;

    public static synchronized USGSService getInstance() {
        if (webService == null) {
            webService = new USGSService();
        }
        return webService;
    }

    /**
     * Constructo de la clase
     */
    private USGSService() {
        // asignamos la hora en GMT
        GMTISOTimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT:00"));
    }

    /**
     * Método encargado de obtener los datos de los puntos de la última hora
     *
     * @return
     * @throws MalformedURLException
     * @throws UnknownHostException
     * @throws IOException
     * @throws JSONException
     * @throws Exception
     */
    public JSONArray procesarPeticion() throws MalformedURLException, UnknownHostException,
            IOException, Exception {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> procesarPeticion()");
        }
        URL url = new URL(urlWebService);
        //the connection to the url is open and we get the inputstream
        InputStream inputStreamWebService = url.openConnection().getInputStream();

        String geoJsonUnformat = convertStreamToString(inputStreamWebService);
        //Tranform the string into a json object
        final JSONObject geoJsonObject = new JSONObject(geoJsonUnformat);

        JSONArray arrayFeatures = geoJsonObject.getJSONArray("features");
        return arrayFeatures;
    }


    /**
     * Convierte un inputstream a un string
     *
     * @param input
     * @return
     */
    private String convertStreamToString(final InputStream input) {
        if (input != null) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            final StringBuilder sBuf = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sBuf.append(line);
                }
            } catch (IOException e) {
                Log.e("Routing Error", e.getMessage());
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e("Routing Error", e.getMessage());
                }
            }
            return sBuf.toString();
        } else {
            return "";
        }
    }
}
