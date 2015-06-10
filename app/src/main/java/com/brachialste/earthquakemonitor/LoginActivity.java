package com.brachialste.earthquakemonitor;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brachialste.earthquakemonitor.data.DataListActivity;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.brachialste.earthquakemonitor.web.communication.WebServerCommunication;
import com.brachialste.earthquakemonitor.web.utils.ConnectionDetector;

/**
 * Created by brachialste on 24/03/15.
 */
public class LoginActivity extends Activity implements ProviderInstaller.ProviderInstallListener {

    // Debug
    private static final String TAG = "LoginActivity";

    // objeto para conectarse al WebService
    private WebServerCommunication web;
    // used to know if the back button was pressed in the splash screen activity
    // and avoid opening the next activity
    private boolean mIsBackButtonPressed;
    // objetos de la sección de Login
    private LinearLayout errorLayout;
    private LinearLayout securityFailLayout;
    private LinearLayout progressLayout;
    // obtejo para el estatus
    public TextView statusTxt;
    // objeto para la versión de la aplicación
    public TextView versionTxt;

    // ProviderInstaller
    private static final int ERROR_DIALOG_REQUEST_CODE = 1;

    private boolean mRetryProviderInstall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (ApplicationManager.D) {
            Log.d(TAG, "-> onCreate()");
        }
        super.onCreate(savedInstanceState);

        // Asigamos el valor del idioma a la aplicaicon
//        Utilities.checkLanguage(getApplicationContext());

        // translucent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // seteamos la vista del splash
        setContentView(R.layout.login);

        // obtenemos el error layout
        errorLayout = (LinearLayout) findViewById(R.id.serverErrorLayout);
        // boton de reintentar
        Button retryButton = (Button) errorLayout.findViewById(R.id.btn_error_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ocultamos la vista de error
                errorLayout.setVisibility(View.GONE);
                // hacemos visible la vista del progress
                progressLayout.setVisibility(View.VISIBLE);

                // iniciamos la revisión del servidor
                CheckServerTask checkServerTask = new CheckServerTask();
                checkServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        // boton de salir
        Button cancelbutton2 = (Button) errorLayout.findViewById(R.id.btn_error_exit);
        cancelbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancelamos la actividad
                LoginActivity.this.finish();

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
        });

        // obtenemos el security error layout
        securityFailLayout = (LinearLayout) findViewById(R.id.securityErrorLayout);
        // boton de continuar
        Button continueButton = (Button) securityFailLayout.findViewById(R.id.btn_error_continue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ocultamos la vista de error
                securityFailLayout.setVisibility(View.GONE);
                // hacemos visible la vista del progress
                progressLayout.setVisibility(View.VISIBLE);

                // iniciamos la revisión del servidor
                CheckServerTask checkServerTask = new CheckServerTask();
                checkServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        // boton de salir2
        Button exitButton = (Button) securityFailLayout.findViewById(R.id.btn_error_exit2);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancelamos la actividad
                LoginActivity.this.finish();

                // matamos los procesos en segundo plano
                ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                for (ApplicationInfo packageInfo : getPackageManager().getInstalledApplications(0)) {
                    if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) continue;
                    if (packageInfo.packageName.equals("com.brachialste.earthquakemonitor")) continue;
                    mActivityManager.killBackgroundProcesses(packageInfo.packageName);
                }

                // matamos cualquier resto
                android.os.Process.sendSignal(android.os.Process.myPid(), android.os.Process.SIGNAL_KILL);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        // obtenemos el layout del progreso
        progressLayout = (LinearLayout) findViewById(R.id.loginProgressLayout);
        // obtenemos el texto del status
        statusTxt = (TextView) findViewById(R.id.splash_status);
        // obtenemos el compoenente de la versión
        versionTxt = (TextView) findViewById(R.id.splash_version);
        PackageInfo pinfo;
        try {
            // obtenemos la información de la versión de la App
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (pinfo != null)
                versionTxt.setText(versionTxt.getText() + " " + pinfo.versionName);
        } catch (Exception e) {
            if (ApplicationManager.D) {
                Log.e(TAG, "No se pudo obtener la version de la aplicación: "
                        + e.getMessage());
            }
        }

        // INICIO
        progressLayout.setVisibility(View.VISIBLE);

        // seteamos el estatus
        statusTxt.setText(getText(R.string.spl_valid_security));

        // instanciamos el Web Service
        web = WebServerCommunication.getInstance(LoginActivity.this);

        // lamamos al provider installer
        ProviderInstaller.installIfNeededAsync(LoginActivity.this, LoginActivity.this);

    }

    @Override
    public void onBackPressed() {
        // set the flag to true so the next activity won't start up
        mIsBackButtonPressed = true;
        super.onBackPressed();
    }

    @Override
    public void onProviderInstalled() {
        // Provider is up-to-date, app can make secure network calls.
        // iniciamos la aplicación
        loadAppMethod();
    }

    @Override
    public void onProviderInstallFailed(int errorCode, Intent intent) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            GooglePlayServicesUtil.showErrorDialogFragment(
                    errorCode,
                    LoginActivity.this,
                    ERROR_DIALOG_REQUEST_CODE,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // The user chose not to take the recovery action
                            onProviderInstallerNotAvailable();
                        }
                    });
        } else {
            // Google Play services is not available.
            onProviderInstallerNotAvailable();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GooglePlayServicesUtil.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which will cause the fragment to delay until
            // onPostResume.
            mRetryProviderInstall = true;
        }
    }

    /**
     * On resume, check to see if we flagged that we need to reinstall the
     * provider.
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mRetryProviderInstall) {
            // We can now safely retry installation.
            ProviderInstaller.installIfNeededAsync(LoginActivity.this, LoginActivity.this);
        }
        mRetryProviderInstall = false;
    }

    private void onProviderInstallerNotAvailable() {
        // This is reached if the provider cannot be updated for some reason.
        // App should consider all HTTP communication to be vulnerable, and take
        // appropriate action.
        // ocultamos la vista del progress
        progressLayout.setVisibility(View.GONE);
        // hacemos visible el login
        securityFailLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Método encargado de setear los valores en la aplicación de prefs
     */
    private void loadAppMethod(){
        if(ApplicationManager.D){
            Log.d(TAG, "-> loadAppMethod()");
        }
        progressLayout.setVisibility(View.VISIBLE);

        // seteamos el estatus
        statusTxt.setText(getText(R.string.spl_loading));

        // termina de girar la progressbar
        if (!mIsBackButtonPressed) {
            // iniciamos la revisión del servidor
            CheckServerTask checkServerTask = new CheckServerTask();
            checkServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    /**
     * Clase encargada de revisar servidor del Web Service
     *
     * @author brachialste
     *
     */
    private class CheckServerTask extends AsyncTask<Void, Void, Boolean> {

        // objeto para validar la comunicación
        private ConnectionDetector conn_det;

        @Override
        protected void onPreExecute() {
            if (ApplicationManager.D) {
                Log.d(TAG, "-> CheckServerTask.onPreExecute()");
            }
            super.onPreExecute();
            progressLayout.setVisibility(View.VISIBLE);

            // obtenemos memoria para el detector de conexión
            conn_det = ConnectionDetector.getInstance(LoginActivity.this);

            // seteamos el estatus
            statusTxt.setText(getString(R.string.spl_serv));
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            synchronized (this) {
                // variable para saber si se ha loggeado o no correctamente el
                // usuario
                if (ApplicationManager.D) {
                    Log.d(TAG, "-> CheckServerTask.doInBackground()");
                }
                return Boolean.valueOf(conn_det.hasInternetAccess());
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (ApplicationManager.D) {
                Log.d(TAG, "-> CheckServerTask.onPostExecute()");
            }
            // termina de girar la progressbar
            if (result.booleanValue()) {
                // iniciamos la UI
                LoginActivity.this.initUI();
            } else {
                // ocultamos la vista del progress
                progressLayout.setVisibility(View.GONE);
                // hacemos visible el errorLayout
                errorLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Método encargado de iniciar la interfase de usuario
     */
    private void initUI(){
        if(ApplicationManager.D) {
            Log.d(TAG, "-> initUI()");
        }
        // termina de girar la progressbar
        statusTxt.setText(getString(R.string.spl_init));

        Intent main = new Intent(LoginActivity.this, DataListActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        main.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // iniciamos la aplicación
        startActivity(main);

        // finalizamos la actividad de Splash
        LoginActivity.this.finish();
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();
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