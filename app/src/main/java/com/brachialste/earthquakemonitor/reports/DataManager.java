package com.brachialste.earthquakemonitor.reports;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.R;
import com.brachialste.earthquakemonitor.db.DataBean;
import com.brachialste.earthquakemonitor.db.DataBeanHelper;
import com.brachialste.earthquakemonitor.db.DataSQLite;
import com.brachialste.earthquakemonitor.web.communication.WebServerCommunication;
import com.brachialste.earthquakemonitor.web.utils.ConnectionDetector;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brachialste on 2/03/15.
 */
public class DataManager {

    // Debug
    private static final String TAG = "ReporteManager";

    // objeto para conectarse al WebService
    private WebServerCommunication web_comm;
    // objeto para validar la comunicación
    private ConnectionDetector conn_det;
    // objeto para almacenar los datos de los temblores
    private DataBeanHelper db_dataBean;
    // referencia a la instancia actual del ReporteManager
    private static DataManager dataManager;
    // contexto de la aplicación
    private Context context;
    // handler recibido
    private Handler mHandler;

    // valores del tipo de respuesta
    public static final int DATA_RECEIVED = 0x35;

    /**
     * Método encargado de obtener la instancia actual del DataManager
     *
     * @param context
     * @return
     */
    public static synchronized DataManager getInstance(Context context) {
        if (dataManager == null) {
            dataManager = new DataManager(context);
        }

        return dataManager;
    }

    /**
     * Constructor de la clase
     *
     * @param context
     */
    public DataManager(Context context) {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> DataManager()");
        }
        this.context = context;
        // instanciamos el Web Service
        web_comm = WebServerCommunication.getInstance(context);
        // instanciamos el objeto para la tabla de ReporteRi505
        db_dataBean = new DataBeanHelper(context);
        // obtenemos memoria para el detector de conexión
        conn_det = ConnectionDetector.getInstance(context);

    }

    /**
     * Método encargado para consultar los datos sobre demanda
     *
     * @param handler
     */
    public void consultData(Handler handler) {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> consultData()");
        }
        this.mHandler = handler;
        SendConsultDataRequest sendConsultReportRequest = new SendConsultDataRequest();
        sendConsultReportRequest.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Método encargado de guardar un reporte en la base de datos
     *
     * @param dataBean
     * @return
     */
    public boolean guardarDataBean(DataBean dataBean) {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> guardarReporte()");
        }
        boolean res = false;
        if (db_dataBean.validaDataBean(dataBean)){
            // guardamos el reporte en la base de datos
            db_dataBean.guardarDataBean(dataBean);
            if (ApplicationManager.D) {
                Log.i(TAG, "dataBean guardado correctamente");
            }
            res = true;
        } else {
            if (ApplicationManager.D) {
                Log.e(TAG, "dataBean duplicado");
            }
        }

        return res;
    }

    /**
     * Método encargado de obtener los reportes de la base de datos
     *
     * @return
     */
    public List<DataBean> obtenerDataBeans() {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> obtenerDataBeans()");
        }
        return db_dataBean.listaDataBeans();
    }

    /**
     * Método encargado de borrar un reporte en la base de datos
     *
     * @param dataBean
     * @return
     */
    public boolean borrarDataBean(DataBean dataBean) {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> borrarDataBean()");
        }
        boolean res = false;
        // eliminamos el reporte en la base de datos
        db_dataBean.borrarDataBean(dataBean);
        if (ApplicationManager.D) {
            Log.i(TAG, "Reporte borrado correctamente");
        }
        res = true;
        return res;
    }

    /**
     * Método encargado de obtener los datos del listado
     *
     * @return
     */
    public List<DataBeanInfo> obtenerDatos() {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> obtenerDatos()");
        }
        // reportes de regreso
        List<DataBeanInfo> dataBeanListFilter = new ArrayList<DataBeanInfo>();

        List<DataBean> dataBeanList = obtenerDataBeans();
        // obtenemos los elementos del arreglo ordenados
        if (dataBeanList.size() > 0) {
            for (DataBean dataBean : dataBeanList) {
                // obtenemos el color del componente
                int dataColor = R.color.master_ok;
                if (dataBean.getMagnitude() > 0.9) {
                    dataColor = R.color.dark_red;
                }

                // obtenemos la fecha de generación del dataBean
                DateTime fechaTemblor = new DateTime(dataBean.getTime());

                // creamos un nuevo objeto DataBeanInfo
                DataBeanInfo dataBeanInfo = new DataBeanInfo(Integer.toString(dataBean.getId()),
                        "" + dataBean.getMagnitude(),
                        "" + dataBean.getPlace(),
                        "[" + dataBean.getLatLng().latitude + ", " +
                                dataBean.getLatLng().longitude + "]",
                        new SimpleDateFormat(DataSQLite.DATE_SQLITE_FORMAT).format(fechaTemblor.toDate()),
                        "(" + dataBean.getDepth() + "km.)",
                        dataColor);

                // agregamos el elemento al listado
                dataBeanListFilter.add(dataBeanInfo);
            }
        }
        return dataBeanListFilter;
    }

    /**
     * Método encargado de obtener el reporte de acuerdo al folio
     *
     * @param id id del DataBean
     * @return
     */
    public DataBean obtieneDataBeanId(int id) {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> obtieneDataBeanId()");
        }
        DataBean dataBean = db_dataBean.obtieneDataBean(id);
        return dataBean;
    }

    /**
     * Método encargado de enviar la petición sobre demanda de un reporte
     * @return
     */
    public boolean sendConsultRequest() {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> sendConsultRequest()");
        }
        // respuesta
        boolean res = false;
        // número de intentos de conexión
        int connect_tries = 3;
        if (connect_tries > 0) {
            boolean connected = false;
            do {
                // revisamos si hay conexión a internet
                connected = conn_det.hasInternetAccess();
                // restamos 1 al contador de intentos
                connect_tries--;
            } while (!connected && connect_tries > 0);
            if (connected) {
                JSONArray arrayFeatures = web_comm.enviarPeticion();
                try {
                    if (arrayFeatures != null) {
                        ArrayList<DataBean> datos = new ArrayList<DataBean>();
                        for (int i = 0; i < arrayFeatures.length(); i++) {
                            JSONObject feature = arrayFeatures.getJSONObject(i);
                            JSONObject geometry = feature.getJSONObject("geometry");
                            JSONObject properties = feature.getJSONObject("properties");
                            JSONArray puntos = geometry.getJSONArray("coordinates");
                            // creamos los beans de datos
                            LatLng position = new LatLng(puntos.getDouble(1), puntos.getDouble(0));
                            DataBean dataBean = new DataBean(0,
                                    properties.getDouble("mag"),
                                    properties.getString("place"),
                                    position,
                                    properties.getLong("time"),
                                    puntos.getDouble(2));
                            datos.add(dataBean);
                        }
                        if(datos.size() > 0){
                            for(DataBean dataBean : datos){
                                guardarDataBean(dataBean);
                            }
                            res = true;
                        }
                    }
                }catch (JSONException e){
                    Log.e(TAG, "Error en los datos");
                }
            }
        }

        return res;
    }

    /**
     * Clase encargada de enviar una petición de registro de dispositivo
     *
     * @author brachialste
     *
     */
    private class SendConsultDataRequest extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            if (ApplicationManager.D) {
                Log.d(TAG, "SendConsultReportRequest -> onPreExecute()");
            }
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (ApplicationManager.D) {
                Log.d(TAG, "SendConsultReportRequest -> doInBackground()");
            }
            Boolean result = Boolean.valueOf(false);
            synchronized (this) {
                result = Boolean
                        .valueOf(sendConsultRequest());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (ApplicationManager.D) {
                Log.d(TAG, "SendConsultReportRequest -> onPostExecute()");
            }
            if (result.booleanValue()) {
                // envia la confirmación de dispositivo registrado al fragment
                Message msg = mHandler.obtainMessage(DATA_RECEIVED);
                Bundle bundle = new Bundle();
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }else{
                mHandler.sendEmptyMessage(0);
            }
            super.onPostExecute(result);
        }
    }

}
