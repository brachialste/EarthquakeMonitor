package com.brachialste.earthquakemonitor.data;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brachialste.earthquakemonitor.db.DataBean;
import com.brachialste.earthquakemonitor.view.slide.NavigationDrawerFragment;
import com.brachialste.earthquakemonitor.web.error.UnCaughtException;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.BaseActivity;
import com.brachialste.earthquakemonitor.R;
import com.brachialste.earthquakemonitor.view.fab.MyFloatingActionsMenu;
import com.brachialste.earthquakemonitor.view.progresswheel.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brachialste on 16/02/15.
 */
public class DataListActivity extends BaseActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        RecyclerView.OnItemTouchListener,
        View.OnClickListener,
        ActionMode.Callback,
        DeleteConfirmationFragmentDialog.Callbacks {

    // Debug
    private static final String TAG = "DataListActivity";

    public static final String NAME_INNER_CONTAINER = "innerContainer";

    // variable para mantener la pantalla activa
    protected PowerManager.WakeLock mWakeLock;
    // componente para el progress dialog
    public static ProgressDialog progDailog;
    // variable para las preferencias
    private SharedPreferences prefs;

    // varaiable para saber si se sale o no de la aplicación con el botón de atras
    private boolean doubleBackToExitPressedOnce;
    private Handler mBackHandler = new Handler();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    // manejador de los reportes
    private DataManager dataManager;

    // objeto para el progress
    private RelativeLayout progressLayout;
    private ProgressWheel progressWheel;

    private RecyclerView recyclerView;
    private LinearLayoutManager llm;
    private GestureDetectorCompat gestureDetector;
    private DataAdapter dataAdapter;
    private ActionMode actionMode;
    private List<DataBeanInfo> mReportes;

    DeleteConfirmationFragmentDialog deleteConfirmationFragmentDialog = null;

    // identificador unico para el elemento seleccionado de la vista de reportes
    public static final String REP_ID = "repId";

    // variables para los diálogos
    public static final int START_DATE_DIALOG_ID = 0x58;
    public static final int END_DATE_DIALOG_ID = 0x56;

    // referencia para el botón FAB
    public static MyFloatingActionsMenu mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Asigamos el valor del idioma a la aplicaicon
//        Utilities.checkLanguage(getApplicationContext());

        setTitle(getString(R.string.app_name));

        // asignamos un nuevo diálogo de error
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(DataListActivity.this));

        // ponemos la pantalla activa
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "com.brachialste.earthquakemonitor");
        this.mWakeLock.acquire();

        // obtenemos las preferencias
        prefs = PreferenceManager.getDefaultSharedPreferences(DataListActivity.this);

        // seteamos la progress dialog
        progDailog = new ProgressDialog(DataListActivity.this);

        // obtenemos la referencia del manager de los datos
        dataManager = DataManager.getInstance(DataListActivity.this);

        // INICIO DRAWER
        // hacemos referencia al drawer
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.main_drawer_layout));

        // FIN DRAWER

        // obtenemos el acceso del usuario
        String appname = getString(R.string.data_servname);
        // seteamos los datos del acceso
        mNavigationDrawerFragment.setDrawerDetailText(appname);

        // CONTENIDO

        // definimos los componentes para el progress
        progressLayout = (RelativeLayout) findViewById(R.id.progress_view);
        progressWheel = (ProgressWheel) findViewById(R.id.pw_spinner);

        // recycler view
        recyclerView = (RecyclerView) findViewById(R.id.rep_recycler_view);
        recyclerView.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        mReportes = new ArrayList<DataBeanInfo>();
        // obtenemos la lista de las estaciones
        dataAdapter = new DataAdapter(DataListActivity.this, mReportes);
        recyclerView.setAdapter(dataAdapter);

        // this is the default; this call is actually only necessary with custom ItemAnimators
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(this);

        recyclerView.setOnClickListener(this);

        gestureDetector =
                new GestureDetectorCompat(this, new RecyclerViewReportesOnGestureListener());

        // FIN CONTENIDO

        // obtenemos la referencia de los botones FAB
        mFab = ((MyFloatingActionsMenu) findViewById(R.id.multiple_actions));

        // botón de actualizar
        FloatingActionButton actionVisible = (FloatingActionButton) findViewById(R.id.action_refresh);
        actionVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // actualizamos la lista
                refreshReportesList();

                // ocultamos las opciones del FAB
                mFab.toggle();
            }
        });

        // implementamos un listener para
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastKnownFirst; // metodo para la ultima posición conocida
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (llm.findFirstVisibleItemPosition() > lastKnownFirst) {
                    mFab.hide(true);
                } else if (llm.findFirstVisibleItemPosition() < lastKnownFirst) {
                    mFab.hide(false);
                }
                lastKnownFirst = llm.findFirstVisibleItemPosition();
            }
        });

        // ejecutamos la obtencion de las los reportes
        refreshReportesList();
    }

    @Override protected int getLayoutResource() {
        return R.layout.data_list;
    }

    /**
     * Método encargado de actualizar el listado de estaciones
     */
    public void refreshReportesList(){
        if(ApplicationManager.D){
            Log.d(TAG, "-> refreshReportesList()");
        }
        // actualizamos los valores del componente
        dataManager.consultData(StaticDataHandlerFactory.create(handler));
        // ocultamos el grid
        recyclerView.setVisibility(View.GONE);
        // mostramos el progress
        progressLayout.setVisibility(View.VISIBLE);
        // empezamos a girar el wheel
        progressWheel.spin();
    }

    /**
     * Handler de la actividad principal
     */
    private IStaticDataHandler handler = new IStaticDataHandler() {
        @Override
        public void handleMessage(Message msg) {
            if (ApplicationManager.D) {
                Log.d(TAG, "-> handleMessage()");
            }
            // quitamos el diálogo
            try {
                if (progressWheel != null) {
                    // dejamos de girar el wheel
                    progressWheel.stopSpinning();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {

            }

            // ocultamos el progress
            progressLayout.setVisibility(View.GONE);
            // mostramos el grid
            recyclerView.setVisibility(View.VISIBLE);

            switch (msg.what){
                case DataManager.DATA_RECEIVED:
                    if(ApplicationManager.D){
                        Log.d(TAG, "DATA_RECEIVED");
                    }
                    Toast.makeText(DataListActivity.this, getString(R.string.data_refreshed), Toast.LENGTH_SHORT).show();
                    break;
            }

            // obtenemos los valores del refresh
            mReportes = dataManager.obtenerDatos();
            // volvemos a crear un adapter con las nuevos reportes
            dataAdapter = new DataAdapter(DataListActivity.this, mReportes);
            // volvemos a setear el adapter a la vista
            recyclerView.setAdapter(dataAdapter);
            if (mReportes != null) {
                // notificamos que se han modificado los datos
                dataAdapter.notifyDataSetChanged();
            }
        }
    };

    // BEGIN_INCLUDE(create_menu)
    /**
     * Use this method to instantiate your menu, and add your items to it. You
     * should return true if you have added items to it and want the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.data_bar_menu, menu);
        return true;
    }

    // END_INCLUDE(create_menu)

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    // BEGIN_INCLUDE(menu_item_selected)

    /**
     * This method is called when one of the menu items to selected. These items
     * can be on the Action Bar, the overflow menu, or the standard options menu. You
     * should return true if you handle the selection.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.update_est) {
            // ejecutamos la obtencion de las los reportes
            refreshReportesList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if(ApplicationManager.D){
            Log.d(TAG, "-> onNavigationDrawerItemSelected(): " + position);
        }
        switch (position){
            case 1: // acerca de
                if (ApplicationManager.D) {
                    Log.d(TAG, "Acerca de...");
                }
                aboutDialog();

                break;
            case 2: // salir
                if (ApplicationManager.D) {
                    Log.d(TAG, "Salir");
                }
                // salimos de la aplicación
                secureExit();

                break;
        }
    }
    // END_INCLUDE(menu_item_selected)


    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.data_action_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.delete_rep:
                // mostramos un diálogo para confirmar la eliminación del registro
                deleteConfirmationFragmentDialog = DeleteConfirmationFragmentDialog
                        .newInstance(DataListActivity.this, actionMode.getTitle().toString());
                if (deleteConfirmationFragmentDialog != null) {
                    // mostramos el dialogo de confirmación
                    deleteConfirmationFragmentDialog.show(getFragmentManager(), "conf_dialog");
                    deleteConfirmationFragmentDialog.setCancelable(false);
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        // colocamos la toolbox
        setToolbarVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        // item click
        int idx = recyclerView.getChildPosition(view);
        if(idx != -1) {
            DataBeanInfo dataBeanInfo = dataAdapter.getItem(idx);
            if (ApplicationManager.D) {
                Log.d(TAG, "-> Click dataBeanInfo.id = " + dataBeanInfo.id);
            }

            View innerContainer = view.findViewById(R.id.card_view);
            String transName = DataDetailActivity.NAME_INNER_REPORT_CONTAINER + "_" + dataBeanInfo.id;
            ViewCompat.setTransitionName(innerContainer, transName);
            Intent startIntent = new Intent(DataListActivity.this, DataDetailActivity.class);
            startIntent.putExtra(REP_ID, dataBeanInfo.id);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    DataListActivity.this,
                    innerContainer,
                    transName);
            ActivityCompat.startActivity(DataListActivity.this, startIntent, options.toBundle());

        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

    }

    @Override
    public void onResume(){
        super.onResume();

        // actualizamos la lista de reportes
        mReportes = dataManager.obtenerDatos();
        // volvemos a crear un adapter con las nuevos reportes
        dataAdapter = new DataAdapter(DataListActivity.this, mReportes);
        // volvemos a setear el adapter a la vista
        recyclerView.setAdapter(dataAdapter);
        if (mReportes != null) {
            // notificamos que se han modificado los datos
            dataAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDeleteConfirmationReceived(String idData) {
        if(ApplicationManager.D) {
            Log.e(TAG, "-> onDeleteConfirmationReceived()");
        }
        // quitamos el diálogo
        try {
            if (deleteConfirmationFragmentDialog != null) {
                deleteConfirmationFragmentDialog.dismiss();
            }
        } catch (final IllegalArgumentException e) {
            // Handle or log or ignore
        } catch (final Exception e) {
            // Handle or log or ignore
        } finally {
            deleteConfirmationFragmentDialog = null;
        }

        // borramos el elemento de la lista
        DataBean dataBean = dataManager.obtieneDataBeanId(Integer.parseInt(idData));
        if(dataBean != null){
            dataManager.borrarDataBean(dataBean);
        }
        // obtenemos los valores del refresh
        mReportes = dataManager.obtenerDatos();
        // volvemos a crear un adapter con las nuevos reportes
        dataAdapter = new DataAdapter(DataListActivity.this, mReportes);
        // volvemos a setear el adapter a la vista
        recyclerView.setAdapter(dataAdapter);
        if (mReportes != null) {
            // notificamos que se han modificado los datos
            dataAdapter.notifyDataSetChanged();
        }
        // regresamos a toolbar anterior
        actionMode.finish();
    }

    /**
     * Metodo encargado de salir de manera segura del dispositivo
     */
    public void secureExit(){
        if(ApplicationManager.D){
            Log.d(TAG, "secureExit()");
        }
        if (mBackHandler != null) { mBackHandler.removeCallbacks(mRunnable); }
        // llamamos y terminamos el servicio persistente
        ApplicationManager app = ((ApplicationManager) getApplicationContext());
        if(app != null){
            // terminamos el servicio persistente
            app.onTerminate();
        }
        // ponemos la pantalla inactiva
        DataListActivity.this.mWakeLock.release();
        // finaliza la actividad
        DataListActivity.this.finish();

        // matamos los procesos en segundo plano
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ApplicationInfo packageInfo : getPackageManager().getInstalledApplications(0)) {
            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM)==1)continue;
            if(packageInfo.packageName.equals("com.brachialste.earthquakemonitor")) continue;
            mActivityManager.killBackgroundProcesses(packageInfo.packageName);
        }

        // matamos cualquier resto
        android.os.Process.sendSignal(android.os.Process.myPid(), android.os.Process.SIGNAL_KILL);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * Método encargado de mostrar el diálogo de Acerca de...
     */
    public void aboutDialog() {
        PackageInfo pinfo;
        String versionName = "";
        try {

            // obtenemos la información de la versión de la App
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (pinfo != null)
                versionName = pinfo.versionName;
            String content = "\n " + getString(R.string.app_name) + " v."
                    + versionName + "\n\n Copyright(C) 2015 \n\n "
                    + this.getString(R.string.about_auth)
                    + " @Brachialste \n\n "
                    + this.getString(R.string.about_ins) + "\n";

            final TextView message = new TextView(this);
            message.setText(content);

            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .create();

            // Setting Dialog Title
            alertDialog.setTitle(" ");

            // Setting Dialog Message
            alertDialog.setView(message);

            // lo ponemos como no cancelable
            alertDialog.setCancelable(false);

            // Setting alert dialog icon
            alertDialog.setIcon(R.mipmap.ic_launcher);

            // Setting OK Button
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });

            // Showing Alert Message
            alertDialog.show();

        } catch (Exception e) {
            if (ApplicationManager.D) {
                Log.e(TAG, "No se pudo obtener la version de la aplicación: "
                        + e.getMessage());
            }
        }
    }

    /**
     * Runnable para volver a poner la variable en false
     */
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    @Override
    public void onBackPressed() {
        if (ApplicationManager.D) {
            Log.d(TAG, "onBackPressed()");
        }

        // validamos si el drawer esta abierto
        if(mNavigationDrawerFragment.isDrawerOpen()) {
            // regresamos a la vista original
            mNavigationDrawerFragment.closeDrawer();
        }else if (actionMode != null && actionMode.isUiFocusable()){
            actionMode.finish();
        }else{
            if(mFab != null && mFab.isExpanded()){
                // ocultamos los submenus
                mFab.collapse();
            }else{
                // salir
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, getString(R.string.main_back), Toast.LENGTH_SHORT).show();

                if (mBackHandler != null)
                    mBackHandler.postDelayed(mRunnable, 2000);
            }
        }
    }

    /**
     * Gesture detector
     */
    private class RecyclerViewReportesOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (actionMode != null) {
                return;
            }
            int idx = recyclerView.getChildPosition(view);
            if(idx != -1) {
                // Start the CAB using the ActionMode.Callback defined above
                actionMode = startSupportActionMode(DataListActivity.this);
                // ocultamos la toolbox
                setToolbarVisibility(View.GONE);

                DataBeanInfo reportInfo = dataAdapter.getItem(idx);
                if (ApplicationManager.D) {
                    Log.d(TAG, "-> LongClick reportInfo.id = " + reportInfo.id);
                }

                actionMode.setTitle(reportInfo.id);
            }

            super.onLongPress(e);
        }
    }

}