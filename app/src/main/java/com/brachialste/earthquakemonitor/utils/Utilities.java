package com.brachialste.earthquakemonitor.utils;

import android.util.Log;

import com.brachialste.earthquakemonitor.ApplicationManager;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase con diferentes utilizades para el manejo de bytes
 * 
 * @author brachialste
 * 
 */
public class Utilities {
	
	private static final String TAG = "Utilities";
	
	/**
	 * Convierte un objeto a un arreglo de bytes
	 * 
	 * @param obj
	 *            objeto
	 * @return arreglo de bytes correspondiente al objeto
	 */
	public static byte[] getBytes(Object obj) {
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			bos.close();
			byte[] data = bos.toByteArray();
			return data;
		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			try {
				oos.close();
			} catch (IOException ex) {
				Log.e(TAG, ex.getMessage());
			}
		}
		return null;
	}

	/**
	 * Convierte un arreglo de bytes a un objeto
	 * 
	 * @param bytes
	 *            arreglo de bytes
	 * @return objeto correspondiente al arreglo de bytes
	 */
	public static Object toObject(byte[] bytes) {
		Object object = null;
		try {
			object = new java.io.ObjectInputStream(
					new java.io.ByteArrayInputStream(bytes)).readObject();
		} catch (java.io.IOException ioe) {
			Log.e(TAG, ioe.getMessage());
		} catch (java.lang.ClassNotFoundException cnfe) {
			Log.e(TAG, cnfe.getMessage());
		}
		return object;
	}

	/**
	 * Método utilizado para sumar dos arreglos de bytes
	 * 
	 * @param a1
	 *            arreglo de bytes
	 * @param a2
	 *            arreglo de bytes
	 * @return suma de los arreglos de bytes
	 */
	public static byte[] appendByteArray(byte[] a1, byte[] a2) {
		byte[] result = new byte[a1.length + a2.length];
		System.arraycopy(a1, 0, result, 0, a1.length);
		System.arraycopy(a2, 0, result, a1.length, a2.length);
		return result;
	}

	/**
	 * Método que convierte de un arreglo de bytes a un entero
	 * 
	 * @param b
	 * @return
	 */
	public static int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16
				| (b[0] & 0xFF) << 24;
	}

	/**
	 * Método que convierte de un entero a un arreglo de bytes
	 * 
	 * @param a
	 * @return
	 */
	public static byte[] intToByteArray(int a) {
		return new byte[] { (byte) ((a >> 24) & 0xFF),
				(byte) ((a >> 16) & 0xFF), (byte) ((a >> 8) & 0xFF),
				(byte) (a & 0xFF) };
	}

	/**
	 * Método que copia un stream de entrada a un stream de salida
	 * 
	 * @param is
	 * @param os
	 */
	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	/**
	 * Método encargado de hacer XOR de una cadena con una llave
	 * @param clearData
	 * @param key
	 * @return
	 */
	public static String stringXOR(String clearData, char key){
		StringBuilder encoded = new StringBuilder();
		char temp;
		for(int i=0; i<clearData.length(); i++){
			temp = (char)(clearData.charAt(i) ^ key);
			encoded.append(temp);
		}
		return encoded.toString();
	}

	/**
	 * Método que obtiene el MD5 de una función dada
	 * 
	 * @param pass
	 * @return
	 */
	public static String Md5Hash(String pass) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			byte[] data = pass.getBytes("UTF-8");
			m.update(data, 0, data.length);
			BigInteger i = new BigInteger(1, m.digest());
			String hash = i.toString(16);
			hash = Utilities.padLeft(hash, 32).replace(' ', '0');
			return hash;
		} catch (NoSuchAlgorithmException e1) {
			Log.e(TAG, e1.getMessage());
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		}
		return pass;
	}

//    /**
//     * Método encargado de adignar el idioma de la aplicación
//     * @param context
//     */
//    public static void checkLanguage(Context context) {
//        SharedPreferences prefs = PreferenceManager
//                .getDefaultSharedPreferences(context);
//        // seteamos el idioma seleccionado
//        if (prefs.getString("language", "2").equals("2")) {
//            Log.d(TAG, "English");
//            Locale appLoc = new Locale("en");
//            Locale.setDefault(appLoc);
//            Configuration appConfig = new Configuration();
//            appConfig.locale = appLoc;
//            context.getResources().updateConfiguration(appConfig,
//                    context.getResources().getDisplayMetrics());
//        }
//    }

	/**
	 * Limpia un archivo existente con datos
	 *
	 * @param path
	 * @return
	 */
	public static boolean clearFile(String path) {
		if (ApplicationManager.D) {
			Log.d(TAG, "-> clearFile()");
		}
		try {
			File file = new File(path);
			// si el archivo no existe, se crea
			if (!file.exists()) {
				file.createNewFile();
			}
			// se escribe vacio el archivo
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(new String());
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Convierte un arreglo de bytes a su representación en cadena.
	 *
	 * @param data
	 *            dato tipo byte[] que sera convertido a string
	 * @return dato byte[] convertido en String
	 */
	public static String byteToString(byte[] data) {
		if (ApplicationManager.D) {
			Log.d(TAG, "-> byteToString()");
		}
		try {
			char[] cData = new char[data.length];

			for (int i = 0; i < cData.length; i++) {
				cData[i] = (char) data[i];
			}

			return new String(cData);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Obtiene los datos binarios de un hexadecimal
	 *
	 * @param strHex
	 * @return
	 */
	public static String hexToBin(String strHex) {
		try {
			return new BigInteger(strHex, 16).toString(2);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Convierte un valor entero a su representación hexadecimal en un arreglo
	 * de bytes
	 *
	 * @param valor
	 *            dato tipo entero que sera convertido a hexadecimal
	 * @param size
	 *            tamaño del dato
	 * @return
	 */
	public static byte[] intToHex(int valor, int size) {
		try {
			byte[] hex = initArray(size);
			int pos = size - 1;
			do {
				if ((valor % 16) < 10) {
					hex[pos--] = (byte) ((valor % 16) + 48);
				} else
					hex[pos--] = (byte) ((valor % 16) + 55);
			} while ((valor /= 16) != 0 && pos >= 0);
			return hex;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] initArray(int size) {
		byte[] array = new byte[size];
		for (int i = 0; i < size; i++) {
			array[i] = (byte) '0';
		}
		return array;
	}

	/**
	 * Convierte un valor byte hexadecimal a entero
	 *
	 * @param data
	 *            arrreglo de bytes hexadecimal que se convertira a entero
	 * @return el byte hexadecimal
	 */
	public static int byteHexToInt(byte[] data) {
		try {
			return new BigInteger(new String(data), 16).intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Obtiene un subarreglo de bytes contenido en un arreglo de bytes
	 *
	 * @param array
	 *            arreglo de bytes
	 * @param index
	 *            la posicion donde comienza
	 * @param count
	 *            la posicion donde termina
	 * @return arreglo de byte con los arreglos a y b concatenados
	 */
	public static byte[] subArray(byte[] array, int index, int count) {
		try {
			byte[] sub = new byte[count];

			for (int i = index, j = 0; j < count; j++, i++) {
				sub[j] = array[i];
			}

			return sub;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Convierte una cadena a su representación en un arreglo de bytes
	 *
	 * @param str
	 *            Cadena que sera convertida en byte[]
	 * @return La cadena convertida en byte[]
	 */
	public static byte[] stringToByte(String str) {
		try {
			byte[] byteArray = new byte[str.length()];

			for (int i = 0; i < str.length(); i++) {
				byteArray[i] = (byte) str.charAt(i);
			}
			return byteArray;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Valida si el dato es un digito
	 *
	 * @param dat
	 * @return
	 */
	public static boolean isDigit(String dat) {
		try {
			int cont = 0;
			for (int i = 0; i < dat.length(); i++) {
				if (Character.isDigit(dat.charAt(i))) {
					cont++;
				}
			}
			if (cont == dat.length() - 1) {
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Método utilizado para el padding a la derecha
	 *
	 * @param s
	 * @param n
	 * @return
	 */
	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

	/**
	 * Método utilizado para el padding a la izquierda
	 *
	 * @param s
	 * @param n
	 * @return
	 */
	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + "s", s);
	}

	/**
	 * Calcula un crc de 16 bits.
	 *
	 * @param liCRC
	 * @param lbData
	 * @return
	 */
	private static int crc16bits(int liCRC, byte lbData) {
		int lbIndex;
		liCRC ^= (int) ((int) lbData << 8);
		lbIndex = 8;
		do {
			if ((liCRC & 0x8000) != 0) {
				liCRC = (int) ((liCRC << 1) ^ 0x1021);
			} else {
				liCRC <<= 1;
			}
		} while (--lbIndex > 0);
		return liCRC;
	}

	/**
	 * Método encargado de separar los valores de un byte en la parte mas significativa y la menos significativa y regresando su
	 * representación en un arreglo de bytes
	 * @param dato
	 * @return
	 */
	public static byte[] separaDato(byte dato){
		byte[] res = stringToByte(padLeft(Integer.toString((byte)(dato & 0xFF), 16).toUpperCase(), 2).replace(' ', '0'));
		return res;
	}


}
