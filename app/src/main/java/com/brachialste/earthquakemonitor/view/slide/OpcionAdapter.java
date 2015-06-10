package com.brachialste.earthquakemonitor.view.slide;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.brachialste.earthquakemonitor.R;
import com.brachialste.earthquakemonitor.utils.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by brachialste on 22/10/13.
 */
public class OpcionAdapter extends BaseAdapter {

    // atributos publicos
    public static final String OPC_LABEL = "label";
    public static final String OPC_IMAGE = "image";

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader;

    public OpcionAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.opc_row, null);

        // asignamos el valor mínimo de la altura del componente
        vi.setMinimumHeight(100);

        TextView label = (TextView) vi.findViewById(R.id.opc_title); // label_opc
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.opc_image); // imagen_opc

        HashMap<String, String> opcion = new HashMap<String, String>();
        opcion = data.get(position);

        // se asignan todos los valores del componente personalizado
        label.setText(opcion.get(OPC_LABEL));
        if (opcion.get(OPC_IMAGE) != null)
            imageLoader.DisplayImage(
                    Integer.parseInt(opcion.get(OPC_IMAGE)),
                    thumb_image);
        else
            imageLoader.DisplayImage(opcion.get(OPC_IMAGE),
                    thumb_image);
        return vi;

    }
}
