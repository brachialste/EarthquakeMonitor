package com.brachialste.earthquakemonitor.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecManager {

	/**
	 * Objeto MessageDigest que calcula una funcion hash sobre el arreglo de
	 * bytes alimentado a este objeto. Usa el algoritmo de SHA-256.
	 */
	private MessageDigest FuncionHash;
	private Cipher CifradorAES;
	private CipherInputStream cis;
	private FileOutputStream fos;

	public SecManager() throws NoSuchAlgorithmException, NoSuchPaddingException {
		CifradorAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
	}

	/**
	 * Metodo privado para la obtencion del hash de la cadena pasada como
	 * argumento.
	 * 
	 * @param cadena
	 *            Cadena con los caracteres los cuales van a sufrir la
	 *            transformacion con la funcion hash,
	 * @exception NoSuchAlgorithmException
	 *                Si no existe implementacion para el algoritmo de SHA-256.
	 */
	public byte[] obtenerHash(String cadena) throws NoSuchAlgorithmException {
		if (cadena != null) {
			// Obtener hash de la contrasenia
			this.FuncionHash = MessageDigest.getInstance("SHA-256");
			byte data[] = cadena.getBytes();
			this.FuncionHash.update(data);
			byte hash[] = this.FuncionHash.digest();
			return hash;
		} else {
			return null;
		}
	}

	/**
	 * Genera el vector de inicializacion necesario para el algoritmo AES en
	 * modo CBC, este vector se toma a partir de los primeros 16 bytes del hash
	 * de la clave intercambiada por Diffie-Hellman.
	 * 
	 * @param cadena
	 *            Cadena con la contrasenia de usuario.
	 * @exception NoSuchAlgorithmException
	 *                Si no existe implementacion para el algoritmo de SHA-256.
	 */
	private IvParameterSpec obtenerVI(String cadena)
			throws NoSuchAlgorithmException {
		// Obtener un vector de inicializacion usando
		// los primeros 16 bytes del hash (deben ser 16!)
		IvParameterSpec vi = new IvParameterSpec(this.obtenerHash(cadena), 0,
				16);
		return vi;
	}

	/**
	 * Genera la clave secreta necesaria para el algoritmo AES a partir del NIP
	 * que es transformado a traves de una funcion hash.
	 * 
	 * @param key
	 *            Cadena con el NIP del usuario.
	 * @exception NoSuchAlgorithmException
	 *                Si no existe implementacion para el algoritmo de AES.
	 */
	private SecretKey generateDataKey(String key)
			throws NoSuchAlgorithmException {
		// Usar el hash de la contraseña como clave para el cifrado
		// Inicializar una clave secreta
		SecretKey clave = new SecretKeySpec(this.obtenerHash(key), "AES");
		return clave;
	}

	/**
	 * Cifra un archivo de licencia .lic
	 * 
	 * @throws Exception
	 *             Cualquier incidencia
	 */
    @Deprecated
	public void cifrarLicencia(String key, String archivo) {
		InputStream fis = null;
		try {
			// Obtenemos la clave para encriptar el elemento.
			CifradorAES.init(Cipher.ENCRYPT_MODE, this.generateDataKey(key),
					this.obtenerVI(key));

			// obtenemos el archivo como un flujo de bytes
			fis = new FileInputStream(new File(archivo));

			// extraemos el archivo de licencia en los datos internos
			File dataFile = new File("data/data/com.brachialste.earthquakemonitor/data/", "license.lic");
			if (!dataFile.exists()) {
				cis = new CipherInputStream(fis, CifradorAES);
				fos = new FileOutputStream(dataFile);
				byte[] b = new byte[256];
				int i;
				while ((i = cis.read(b)) != -1) {
					fos.write(b, 0, i);
				}
				if (fos != null) {
					fos.flush();
					fos.close();
				}
				cis.close();
				fis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Descifra un archivo .lic
	 * 
	 * @throws Exception
	 *             Cualquier incidencia
	 */
    @Deprecated
	public void descifrarLicencia(String key, String archivo) {
		FileInputStream fis = null;
		try {
			// obtenemos el archivo como un flujo de bytes
			File dataFile = new File(archivo); 
			
			//obtenemos el archivo descifrado temporalmente
			File newDataFile = new File("data/data/com.brachialste.earthquakemonitor/data/", "license_dec.lic");

			// Obtenemos la clave para descifrar elemento.
			CifradorAES.init(Cipher.DECRYPT_MODE, this.generateDataKey(key),
					this.obtenerVI(key));

			fis = new FileInputStream(dataFile);
			if (dataFile.exists()) {
				cis = new CipherInputStream(fis, CifradorAES);
				fos = new FileOutputStream(newDataFile);
				byte[] b = new byte[256];
				int i;
				while ((i = cis.read(b)) != -1) {
					fos.write(b, 0, i);
				}
				if (fos != null) {
					fos.flush();
					fos.close();
				}
				cis.close();
				fis.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
