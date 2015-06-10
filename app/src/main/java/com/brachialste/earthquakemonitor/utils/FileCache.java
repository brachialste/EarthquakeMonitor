package com.brachialste.earthquakemonitor.utils;

import java.io.File;

import android.content.Context;

/**
 * Clase encargada de obtener la imágen de la SD
 * 
 * @author brachialste
 * 
 */
public class FileCache {
	private File cacheDir;

	/**
	 * Constructor de la clase
	 * 
	 * @param context
	 *            Contexto de la aplicación
	 */
	public FileCache(Context context) {
		// Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					"LazyList");
		else
			cacheDir = context.getCacheDir();
		if (!cacheDir.exists())
			cacheDir.mkdirs();
	}

	/**
	 * Método que obtiene el archivo correspondiente a la imagen
	 * 
	 * @param url
	 * @return
	 */
	public File getFile(String url) {
		// se identifican las imagenes utilizando un hashcode
		String filename = String.valueOf(url.hashCode());
		// String filename = URLEncoder.encode(url);
		File f = new File(cacheDir, filename);
		return f;

	}

	/**
	 * Método que limpia el cache
	 */
	public void clear() {
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}
}
