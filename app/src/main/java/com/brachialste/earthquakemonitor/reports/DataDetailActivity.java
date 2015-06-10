package com.brachialste.earthquakemonitor.reports;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.BaseActivity;
import com.brachialste.earthquakemonitor.R;
import com.brachialste.earthquakemonitor.db.DataBean;
import com.brachialste.earthquakemonitor.db.DataSQLite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;

/**
 * Created by brachialste on 23/02/15.
 */
public class DataDetailActivity extends BaseActivity {

    // Debug
    private static final String TAG = "DataListActivity";

    public static final String NAME_INNER_REPORT_CONTAINER = "innerReportContainer";

    // bean de dataBean seleccionado
    private DataBean dataBean;

    // manager de reportes
    private DataManager dataManager;

    // componente de base de datos del mapa
    private GoogleMap nMap = null;
    private Marker estacion_marker = null;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Asigamos el valor del idioma a la aplicaicon
//        Utilities.checkLanguage(getApplicationContext());

        // seteamos el valor del titulo
        setTitle("");
        String title = getIntent().getExtras().getString(DataListActivity.REP_ID);
        if(ApplicationManager.D){
            Log.d(TAG, "title = " + title);
        }

        dataManager = DataManager.getInstance(DataDetailActivity.this);

        // obtenemos el reporte
        if(title != null) {
            DataBean dataBean = dataManager.obtieneDataBeanId(Integer.parseInt(title));

            // componentes de pantalla
            // magnitud
            ((TextView) findViewById(R.id.data_magnitude)).setText("Magnitud: " + dataBean.getMagnitude());
            // profundidad
            ((TextView) findViewById(R.id.data_depth)).setText("Profundidad: " + dataBean.getDepth() + " km.");
            // lugar
            ((TextView) findViewById(R.id.data_place)).setText("Lugar: " + dataBean.getPlace());

            // toolbar
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // atras cuando presione back
                    onBackPressed();
                }
            });

            if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(DataDetailActivity.this) ==
                    ConnectionResult.SUCCESS) {
                // ubicacion
                if (nMap == null) {
                    // hacemos el mapa del tamaño deseado
                    mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_data);

                    // obtenemos la vista del mapa
                    final View mapView = mapFragment.getView();

                    // se intenta obtener el mapa del SupportMapFragment
                    nMap = mapFragment.getMap();

                    // inicializamos el mapa
                    nMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                    // validamos si el gps es nulo o tiene datos
                    if (dataBean.getLatLng() != null) {
                        if (dataBean.getMagnitude() > 0.9) {
                            estacion_marker = nMap.addMarker(new MarkerOptions()
                                    .position(dataBean.getLatLng())
                                    .draggable(false)
                                    .title("" + dataBean.getMagnitude())
                                    .snippet(new SimpleDateFormat(DataSQLite.DATE_SQLITE_FORMAT).
                                            format(new DateTime(dataBean.getTime()).toDate()))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }else{
                            estacion_marker = nMap.addMarker(new MarkerOptions()
                                    .position(dataBean.getLatLng())
                                    .draggable(false)
                                    .title("" + dataBean.getMagnitude())
                                    .snippet(new SimpleDateFormat(DataSQLite.DATE_SQLITE_FORMAT).
                                            format(new DateTime(dataBean.getTime()).toDate()))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        }


                        // movemos la camara al zoom mas grande posible que incluya los puntos
                        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dataBean.getLatLng(), 14));

                        // deshabilitamos el componente
                        nMap.getUiSettings().setAllGesturesEnabled(false);
                    } else {
                        Log.e(TAG, "No se tiene una posición de la estación");
                    }
                }
            } else{
                // regresamos a la vista anterior
                onBackPressed();
            }

        }else{
            if(ApplicationManager.D) {
                Log.e(TAG, "Error al obtener el parámetro");
            }
            // regresamos a la vista anterior
            onBackPressed();
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.data_map_detail;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

}
