package com.brachialste.earthquakemonitor.reports;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.R;

/**
 * Created by brachialste on 1/04/15.
 */
public class DeleteConfirmationFragmentDialog extends DialogFragment {

    // Debug
    private static final String TAG = "DConfFragmentDialog";

    private static Context mContext;
    private static String idReporte;

    /**
     * Callback del fragment dialog, para notificar cuando se recibió confirmación
     */
    private Callbacks receivedResponseCallbacks = sReceivedResponseCallbacks;

    /**
     * Interface de callback que debe ser implementada por las actividades que contengan a
     * este Fragment. Este mecanismo permite notificar a las actividades por eventos del fragment.
     */
    public interface Callbacks {
        /**
         * Callback cuando se ha recibido un reporte
         */
        public void onDeleteConfirmationReceived(String idReport);
    }

    /**
     * Implementación local del callback
     */
    private static Callbacks sReceivedResponseCallbacks = new Callbacks() {
        @Override
        public void onDeleteConfirmationReceived(String idReport) {

        }
    };

    /**
     * Método encargado de obtener una nueva insancia del diálogo
     * @param context
     * @param idReport
     * @return
     */
    public static DeleteConfirmationFragmentDialog newInstance(Context context, String idReport) {
        if(ApplicationManager.D) {
            Log.d(TAG, "-> newInstance()");
        }
        // seteamos el contexto
        mContext = context;
        // seteamos el id del Reporte a ser borrado
        idReporte = idReport;

        return new DeleteConfirmationFragmentDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(getString(R.string.rep_title));
        alertDialogBuilder.setMessage(getString(R.string.rep_msg1));
        //null should be your on click listener
        alertDialogBuilder.setPositiveButton(getString(R.string.popup_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // llamada a la eliminación del reporte
                receivedResponseCallbacks.onDeleteConfirmationReceived(idReporte);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.popup_button_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        return alertDialogBuilder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(ApplicationManager.D){
            Log.d(TAG, "-> onAttach()");
        }
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        receivedResponseCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        receivedResponseCallbacks = sReceivedResponseCallbacks;
    }

}
