package com.brachialste.earthquakemonitor.security;

import android.util.Log;

import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.utils.Utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Clase utilizada para la extracción de la información en los archivos .zip que
 * serán los equivalentes de los .obb en el Google Play
 * 
 * @author brachialste
 * 
 */
public class ZipHelper {

	public static String TAG = "ZipHelper";

	static boolean zipError = false;

	public static boolean isZipError() {
		return zipError;
	}

	public static void setZipError(boolean zipError) {
		ZipHelper.zipError = zipError;
	}

	/**
	 * Método para extraer la información del archivo zip
	 * 
	 * @param archive
	 * @param outputDir
	 */
	public static void unzip(String archive, File outputDir) {
		try {
			if (ApplicationManager.D) {
				Log.d(TAG, "Archivo: " + archive);
			}
			ZipFile zipfile = new ZipFile(archive);
			for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e
					.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				unzipEntry(zipfile, entry, outputDir);

			}
		} catch (Exception e) {
			if (ApplicationManager.D) {
				Log.d(TAG, "Error extrayendo el archivo " + archive + ": " + e);
			}
			setZipError(true);
		}
	}

	/**
	 * Método encargado de descomprimir cierta entrada
	 * 
	 * @param zipfile
	 * @param entry
	 * @param outputDir
	 * @throws IOException
	 */
	private static void unzipEntry(ZipFile zipfile, ZipEntry entry,
			File outputDir) throws IOException {
		if (entry.isDirectory()) {
			createDirectory(new File(outputDir, entry.getName()));
			return;
		}

		File outputFile = new File(outputDir, entry.getName());
		if (!outputFile.getParentFile().exists()) {
			createDirectory(outputFile.getParentFile());
		}

		if (ApplicationManager.D) {
			Log.d(TAG, "Extrayendo: " + entry);
		}
		BufferedInputStream inputStream = new BufferedInputStream(
				zipfile.getInputStream(entry));
		BufferedOutputStream outputStream = new BufferedOutputStream(
				new FileOutputStream(outputFile));

		try {
			Utilities.copyStream(inputStream, outputStream);
		} catch (Exception e) {
			Log.d(TAG, "Error: " + e);
			setZipError(true);
		} finally {
			outputStream.close();
			inputStream.close();
		}
	}

	/**
	 * Métodoe encargado de crear un directorio
	 * 
	 * @param dir
	 */
	public static void createDirectory(File dir) {
		if (ApplicationManager.D) {
			Log.d(TAG, "Creando el directorio: " + dir.getName());
		}
		if (!dir.exists()) {
			if (!dir.mkdirs())
				throw new RuntimeException("No se puede crear el directorio "
						+ dir);
		} else {
			if (ApplicationManager.D) {
				Log.d(TAG, "Directorio existente: " + dir.getName());
			}
		}
	}
}
