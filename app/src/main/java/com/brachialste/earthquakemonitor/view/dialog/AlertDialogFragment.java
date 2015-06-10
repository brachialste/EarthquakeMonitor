package com.brachialste.earthquakemonitor.view.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.R;
import com.brachialste.earthquakemonitor.view.CircleImageView;

/**
 * Created by brachialste on 8/04/15.
 */
public class AlertDialogFragment extends DialogFragment {

    // Debug
    private static final String TAG = "AlertDialogFragment";

    private static String title = null;
    private static String message = null;
    private static String stat = null;

    // variables para indicar el tipo de diálogo
    public static final int DIALOG_OK = 1;
    public static final int DIALOG_FAIL = 2;
    public static final int DIALOG_INFO = 3;

    public static AlertDialogFragment newInstance(String titulo, String mensaje, String estado) {
        if(ApplicationManager.D) {
            Log.d(TAG, "-> newInstance()");
        }
        if(ApplicationManager.D) {
            Log.d(TAG, "titulo = " + titulo);
            Log.d(TAG, "mensaje = " + mensaje);
            Log.d(TAG, "estado = " + estado);
        }

        // asignamos los valores de las variables
        title = titulo;
        message = mensaje;
        stat = estado;

        // validamos que los valores no sean nulos
        if (title != null && message != null && stat != null) {
            // regresamos un nuevo diálogo
            return new AlertDialogFragment();
        }

        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_alert_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);

        int tipoDialog = 0;
        if (stat.equals("true")) {
            tipoDialog = DIALOG_OK;
        } else if (stat.equals("false")) {
            tipoDialog = DIALOG_FAIL;
        } else if (stat.equals("info")){
            tipoDialog = DIALOG_INFO;
        }

        // title
        TextView dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
        if(!title.isEmpty()){
            dialogTitle.setText(title);
        }else{
            Log.e(TAG, "Título del diálogo vacío");
        }

        // imagen
        CircleImageView dialogImg = (CircleImageView) dialog.findViewById(R.id.dialog_img);
        if(tipoDialog == DIALOG_OK){
            dialogImg.setImageResource(R.drawable.accept);
        }else if(tipoDialog == DIALOG_FAIL){
            dialogImg.setImageResource(R.drawable.cancel);
        }else if(tipoDialog == DIALOG_INFO){
            dialogImg.setImageResource(R.drawable.info);
        }

        // mensaje
        TextView dialogMsg = (TextView) dialog.findViewById(R.id.dialog_msg);
        if(!message.isEmpty()){
            dialogMsg.setText(message);
        }else{
            Log.e(TAG, "Mensaje del diálogo vacío");
        }

        // botón de ok
        Button positiveBtn = (Button) dialog.findViewById(R.id.dialog_positive_button);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // quitamos el diálogo
                dismiss();
                // terminamos la actividad padre
                getActivity().finish();
            }
        });

        return dialog;
    }

    @Override
    public void onStop(){
        super.onStop();
        // terminamos la actividad padre
        getActivity().finish();
    }

}
