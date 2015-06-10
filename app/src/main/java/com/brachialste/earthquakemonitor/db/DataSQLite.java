package com.brachialste.earthquakemonitor.db;

import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.R;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.util.Log;

/**
 * Clase principal de la Base de Datos de SGCMovil define la creación de las
 * tablas, de la actualización y del mantenimiento de la misma
 * 
 * @author brachialste
 * 
 */
public class DataSQLite extends SQLiteOpenHelper {

	// Debug
	private static final String TAG = "DataSQLite";
	public static final int ACTIVE = 0;
	public static final int DELETED = 1;

	public static final String DATE_SQLITE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String DAY_FORMAT = "MMMM d";
    public static final String HOUR_FORMAT = "HH:mm";

	public static final String INTERVAL_FORMAT = "yyyy-MM-dd";

    public static final String DATABASE_NAME = "EarthMonitor";

	private static DataSQLite helper;
	private Context context;

	public static synchronized DataSQLite getInstance(Context context) {
		if (helper == null) {
			helper = new DataSQLite(context);
		}

		return helper;
	}

	public DataSQLite(Context context) {
		super(context, DATABASE_NAME, null, 1);
		if (ApplicationManager.D)
			Log.d(TAG, "->DataSQLite()");
		this.context = context;
	}

	/**
	 * Método encargado de crear la base de datos
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		if (ApplicationManager.D) {
			Log.d(TAG, "->onCreate()");
		}

		// executamos las sentencias para la creción de la base de datos
		db.execSQL(context.getString(R.string.db_dataBean));
	}

	/**
	 * Método encargado de actualizar la base de datos
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (ApplicationManager.D) {
			Log.d(TAG, "->onUpgrade()");
		}
		if (ApplicationManager.D) {
			Log.w(TAG, "Actualizando base de datos de la version " + oldVersion
					+ " a la version " + newVersion
					+ ", lo cual borra cualquier dato de la tabla");
		}
        // executamos las sentencias para el borrado de la base de datos
        db.execSQL(context.getString(R.string.db_clear_dataBean));
		onCreate(db);
	}

}
