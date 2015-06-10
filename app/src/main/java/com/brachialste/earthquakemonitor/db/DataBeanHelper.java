package com.brachialste.earthquakemonitor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.utils.AndroidIdentifier;
import com.brachialste.earthquakemonitor.utils.Utilities;
import com.google.android.gms.maps.model.LatLng;

import net.sqlcipher.database.SQLiteConstraintException;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by brachialste on 9/06/15.
 */
public class DataBeanHelper {

    // Debug
    private static final String TAG = "DataBeanHelper";

    private DataSQLite helper;
    private String secret;

    public DataBeanHelper(Context context) {
        // creamos la base de datos general si no esta creada
        helper = DataSQLite.getInstance(context);
        // creamos el password de la BD
        secret = Utilities.Md5Hash(new AndroidIdentifier(context).generateCombinationID());
        SQLiteDatabase.loadLibs(context);
    }

    /**
     * Método encargado de almacenar un DataBean en la BD
     *
     * @param dataBean
     */
    public void guardarDataBean(DataBean dataBean) {
        if (ApplicationManager.D) {
            Log.d(TAG, "->guardarDataBean()");
        }
        final SQLiteDatabase writableDatabase = helper.getWritableDatabase(secret);
        // asignamos los valores a las columnas
        final ContentValues newReportBase = new ContentValues();
        newReportBase.put("magnitude", dataBean.getMagnitude());
        newReportBase.put("place", dataBean.getPlace());
        newReportBase.put("latitude", dataBean.getLatLng().latitude);
        newReportBase.put("longitude", dataBean.getLatLng().longitude);
        newReportBase.put("time", dataBean.getTime());
        newReportBase.put("depth", dataBean.getDepth());
        newReportBase.put("date_entered", new SimpleDateFormat(
                DataSQLite.DATE_SQLITE_FORMAT)
                .format(Calendar.getInstance().getTime())); // colocamos la hora de creacion
        newReportBase.put("date_modified", new SimpleDateFormat(
                DataSQLite.DATE_SQLITE_FORMAT)
                .format(Calendar.getInstance().getTime())); // colocamos la hora de modificacion
        newReportBase.put("deleted", DataSQLite.ACTIVE);

        try {
            writableDatabase.insertOrThrow("dataBean", null, newReportBase);
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        } finally {
            // writableDatabase.close();
        }
    }

    /**
     * Método que permite actualizar un DataBean en la BD
     *
     * @param dataBean
     */
    public void actualizarDataBean(DataBean dataBean) {
        if (ApplicationManager.D) {
            Log.d(TAG, "->actualizarDataBean()");
        }
        final SQLiteDatabase writableDatabase = helper.getWritableDatabase(secret);
        try {
            // asignamos los nuevos valores a las columnas
            final ContentValues updateDataBean = new ContentValues();
            updateDataBean.put("magnitude", dataBean.getMagnitude());
            updateDataBean.put("place", dataBean.getPlace());
            updateDataBean.put("latitude", dataBean.getLatLng().latitude);
            updateDataBean.put("longitude", dataBean.getLatLng().longitude);
            updateDataBean.put("time", dataBean.getTime());
            updateDataBean.put("depth", dataBean.getDepth());
            updateDataBean.put("date_modified", new SimpleDateFormat(
                    DataSQLite.DATE_SQLITE_FORMAT).format(Calendar.getInstance()
                    .getTime())); // colocamos la hora de modificacion
            writableDatabase.update("dataBean", updateDataBean, "_id="
                    + dataBean.getId(), null);

        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        } finally {
            // writableDatabase.close();
        }
    }

    /**
     * Método que permite eliminar de la BD un DataBean por medio de su
     * identificador
     *
     * @param dataBean
     */
    public void borrarDataBean(DataBean dataBean) {
        if (ApplicationManager.D) {
            Log.d(TAG, "->borrarDataBean()");
        }
        final SQLiteDatabase writableDatabase = helper.getWritableDatabase(secret);
        try {
            writableDatabase
                    .delete("dataBean", "_id=?", new String[] { Integer
                            .toString(dataBean.getId()) });
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            // writableDatabase.close();
        }
    }

    /**
     * Método que regresa la lista de los Solicituds almacenados en la BD
     *
     * @return
     */
    public List<DataBean> listaDataBeans() {
        if (ApplicationManager.D) {
            Log.d(TAG, "->listaDataBeans()");
        }
        String[] campos = { "_id", "magnitude", "place", "latitude", "longitude", "time", "depth",
                "date_entered", "date_modified", "deleted"};
        List<DataBean> resultado = new ArrayList<DataBean>();
        final SQLiteDatabase readableDatabase = helper.getReadableDatabase(secret);
        final Cursor cursor = readableDatabase.query("dataBean", campos,
                "deleted=?",
                new String[] { Integer.toString(DataSQLite.ACTIVE) }, null,
                null, "_id DESC");
        try {
            while (cursor.moveToNext()) {
                DataBean solicitud;
//				try {
                solicitud = new DataBean(
                        cursor.getInt(0),   // _id
                        cursor.getDouble(1),   // magnitude
                        cursor.getString(2),   // place
                        new LatLng(cursor.getFloat(3),  // latitude
                                cursor.getFloat(4)),   // longitude
                        cursor.getLong(5),   // time
                        cursor.getDouble(6) // depth
                );
                solicitud.setDate_entered(cursor.getString(7));
                solicitud.setDeleted(cursor.getInt(9));
                resultado.add(solicitud);
//				} catch (IOException e) {
//					Log.e(TAG, e.getMessage());
//				}
            }
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            // readableDatabase.close();
        }
        return resultado;
    }

    /**
     * Método encargado de obtener los registros caducados
     *
     * @return
     */
    public List<DataBean> listaDataBeansCaducos() {
        if (ApplicationManager.D) {
            Log.d(TAG, "->listaDataBeansCaducos()");
        }
        DateTime fecha_actual = new DateTime(); // fecha actual
        String[] campos = { "_id", "magnitude", "place", "latitude", "longitude", "time", "depth",
                "date_entered", "date_modified", "deleted"};
        List<DataBean> resultado = new ArrayList<DataBean>();
        final SQLiteDatabase readableDatabase = helper.getReadableDatabase(secret);
        final Cursor cursor = readableDatabase.query("dataBean", campos, null,
                null, null, null, "_id");
        try {
            while (cursor.moveToNext()) {
                try {
                    if (cursor.getString(8) != null) {
                        // obtenemos la fecha de modificacion
                        DateTime fecha_mod = new DateTime(
                                (Date) new SimpleDateFormat(
                                        DataSQLite.DATE_SQLITE_FORMAT).parse(cursor
                                        .getString(8).trim()));
                        // comparamos si entre la fecha modificada y la fecha actual
                        // ha pasado mas de 3 días (72 horas)
                        if (Days.daysBetween(fecha_mod, fecha_actual).getDays() >= 3) {
                            DataBean dataBean = new DataBean(
                                    cursor.getInt(0),   // _id
                                    cursor.getDouble(1),   // magnitude
                                    cursor.getString(2),   // place
                                    new LatLng(cursor.getFloat(3),  // latitude
                                            cursor.getFloat(4)),   // longitude
                                    cursor.getLong(5),   // time
                                    cursor.getDouble(6) // depth
                            );
                            dataBean.setDate_entered(cursor.getString(7));
                            dataBean.setDeleted(cursor.getInt(9));
                            resultado.add(dataBean);
                        }
                    }
//				} catch (IOException e) {
//					Log.e(TAG, e.getMessage());
                } catch (ParseException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            // readableDatabase.close();
        }
        return resultado;
    }

    /**
     * Método que obtiene una DataBean por el folio
     * @param id
     *          Id del dataBean
     * @return
     */
    public DataBean obtieneDataBean(int id){
        if (ApplicationManager.D) {
            Log.d(TAG, "->obtieneDataBean()");
        }

        DataBean solicitud = null;
        String[] campos = { "_id", "magnitude", "place", "latitude", "longitude", "time", "depth",
                "date_entered", "date_modified", "deleted"};
        final SQLiteDatabase readableDatabase = helper.getReadableDatabase(secret);
        final Cursor cursor = readableDatabase
                .query("dataBean", campos, "_id=?", new String[]{
                        Integer.toString(id)}, null, null, null);
        try {
            if (cursor != null && cursor.moveToNext()) {
                solicitud = new DataBean(
                        cursor.getInt(0),   // _id
                        cursor.getDouble(1),   // magnitude
                        cursor.getString(2),   // place
                        new LatLng(cursor.getFloat(3),  // latitude
                                cursor.getFloat(4)),   // longitude
                        cursor.getLong(5),   // time
                        cursor.getDouble(6) // depth
                );
                solicitud.setDate_entered(cursor.getString(7));
                solicitud.setDeleted(cursor.getInt(9));
            }
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            // readableDatabase.close();
        }
        return solicitud;
    }

    /**
     * Método que valida el DataBean previo a la insersión
     *
     * @return
     */
    public boolean validaDataBean(DataBean dataBean) {
        if (ApplicationManager.D) {
            Log.d(TAG, "->validaReporte()");
        }
        final SQLiteDatabase readableDatabase = helper.getReadableDatabase(secret);
        String[] campos = { "_id", "magnitude", "place", "latitude", "longitude", "time", "depth",
                "date_entered", "date_modified", "deleted"};
        final Cursor cursor = readableDatabase
                .query("dataBean", campos, "time=?", new String[]{
                        Long.toString(dataBean.getTime())}, null, null, null);
        try {
            if (cursor != null && cursor.getCount() > 0) {
                return false;
            }else{
                return true;
            }
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            // readableDatabase.close();
        }
        return true;
    }

    /**
     * Metodo que permite realizar un borrado lógico de los DataBeans
     *
     * @param dataBean
     */
    public void borrarDataBeanLogico(DataBean dataBean) {
        if (ApplicationManager.D) {
            Log.d(TAG, "->borrarDataBeanLogico()");
        }
        final SQLiteDatabase writableDatabase = helper.getWritableDatabase(secret);
        try {
            // asignamos los nuevos valores a las columnas
            final ContentValues deleteDataBean = new ContentValues();
            deleteDataBean.put("date_modified", new SimpleDateFormat(
                    DataSQLite.DATE_SQLITE_FORMAT).format(Calendar.getInstance()
                    .getTime())); // colocamos la hora de modificacion
            deleteDataBean.put("deleted", DataSQLite.DELETED); // borramos el registro lógicamente

            writableDatabase.update("dataBean", deleteDataBean, "_id="
                    + dataBean.getId(), null);
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            // writableDatabase.close();
        }
    }

}
