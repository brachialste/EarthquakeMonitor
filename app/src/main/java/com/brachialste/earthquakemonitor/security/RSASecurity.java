/**
 * 
 */
package com.brachialste.earthquakemonitor.security;

import android.util.Log;

import com.brachialste.earthquakemonitor.ApplicationManager;
import com.brachialste.earthquakemonitor.utils.Utilities;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Clase encargada de manejar la seguridad con el algoritmo de RSA
 * 
 * @author brachialste
 * @version 1.1
 * 
 */
public class RSASecurity {
	// Debug
	private static final String TAG = "RSASecurity";

	// Tamaño de la llave
	public static final int KEY_SIZE = 1024;

	// Llave publica
	private PublicKey pubKey;

	public PublicKey getPubKey() {
		return pubKey;
	}

	public void setPubKey(PublicKey pubKey) {
		this.pubKey = pubKey;
	}

	/**
	 * Método encargado de leer la llave pública
	 * 
	 * @param filename
	 * @return
	 * @throws StreamCorruptedException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public PublicKey getPemPublicKey(String filename)
			throws StreamCorruptedException, FileNotFoundException, IOException {
		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);
		try {
			byte[] keyBytes = new byte[(int) f.length()];
			dis.readFully(keyBytes);

			String temp = new String(keyBytes);
			String publicKeyPEM = temp.replace("-----BEGIN PUBLIC KEY-----\n",
					"");
			publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");

			byte[] decoded = Base64.decode(publicKeyPEM);

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PublicKey pubKey = fact.generatePublic(keySpec);

			// obtenemos la llave publica y la seteamos
			setPubKey(pubKey);

			return pubKey;
		} catch (Exception e) {
			Log.e("RSA", e.getMessage());
			throw new RuntimeException("Spurious serialisation error", e);
		} finally {
			dis.close();
		}
	}

	/**
	 * Verifica si determinada firma digital es válida de acuerdo a los datos de
	 * entrada
	 * 
	 * @param firma
	 * @param data
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws IOException
	 */
	public boolean verifySignature(byte[] firma, String data)
			throws NoSuchAlgorithmException, InvalidKeyException,
			SignatureException, IOException {
		if (ApplicationManager.D) {
			Log.d(TAG, "->verifySignature()");
		}
		boolean isValid = false;
		Signature sig = Signature.getInstance("MD5WithRSA");
		sig.initVerify(getPubKey());
		sig.update(data.getBytes("UTF8"));
		isValid = sig.verify(Base64.decode(firma));
		return isValid;
	}

	/**
	 * Verifica si determinada firma digital es válida de acuerdo a los datos de
	 * entrada
	 * 
	 * @param firma
	 * @param data
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws IOException
	 */
	public boolean verifySignature(byte[] firma, byte[] data)
			throws NoSuchAlgorithmException, InvalidKeyException,
			SignatureException, IOException {
		if (ApplicationManager.D) {
			Log.d(TAG, "->verifySignature()");
		}
		boolean isValid = false;
		Signature sig = Signature.getInstance("MD5WithRSA");
		sig.initVerify(getPubKey());
		sig.update(data);
		isValid = sig.verify(Base64.decode(firma));
		return isValid;
	}

    /**
     * Método que valida la integridad de un archivo mediante una firma MD5
     *
     * @param evaluate_file
     * @param signature
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws NoSuchPaddingException
     * @throws IOException
     */
    public boolean verifyFileMD5(File evaluate_file, String signature)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidKeyException, SignatureException, NoSuchPaddingException,
            IOException {
        if (ApplicationManager.D) {
            Log.d(TAG, "->verifyFileMD5()");
        }
        if (ApplicationManager.D) {
            Log.d(TAG, "Signature: " + signature);
        }

        // calculamos el MD5 del archivo de licencia
        String licenseHashStr = Utilities.padLeft(digestMD5(evaluate_file), 32).replace(' ',
                '0').toUpperCase();

        if (ApplicationManager.D) {
            Log.d(TAG, "Data: " + licenseHashStr);
        }

        // validamos la firma MD5 con la calculada
        if (signature.trim().equals(licenseHashStr.trim())) {
            return true;
        } else {
            return false;
        }
    }

	/**
	 * Método encargado de obtener la hash MD5 de un archivo
	 * 
	 * @param dataToHashPath
	 * @return
	 */
	public static byte[] digest(File dataToHashPath) {
		if (ApplicationManager.D) {
			Log.d(TAG, "->digest()");
		}
		try {
			InputStream fin = new FileInputStream(dataToHashPath);
			MessageDigest md5Digest = MessageDigest.getInstance("MD5");

			byte[] buffer = new byte[1024];
			int read;

			do {
				read = fin.read(buffer);
				if (read > 0) {
					md5Digest.update(buffer, 0, read);
				}
			} while (read != -1);
			fin.close();

			byte[] digest = md5Digest.digest();
			if (digest == null) {
				return null;
			}

			return digest;
		} catch (Exception e) {
			return null;
		}
	}


    /**
     * Método encargado de obtener el digest MD5 de un archivo
     *
     * @param dataToHashPath
     * @return
     */
    public static String digestMD5(File dataToHashPath)
    {
        char[] hexDigits = "0123456789abcdef".toCharArray();

        try {
            byte[] bytes = new byte[4096];
            int read = 0;

            // obtenemos el path al archivo
            InputStream fin = new FileInputStream(dataToHashPath);
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            while ((read = fin.read(bytes)) != -1)
            {
                md5Digest.update(bytes, 0, read);
            }

            byte[] messageDigest = md5Digest.digest();

            StringBuilder sb = new StringBuilder(32);

            // Oh yeah, this too.  Integer.toHexString doesn't zero-pad, so
            // (for example) 5 becomes "5" rather than "05".
            for (byte b : messageDigest)
            {
                sb.append(hexDigits[(b >> 4) & 0x0f]);
                sb.append(hexDigits[b & 0x0f]);
            }

            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

	/**
	 * 
	 * @param clearText
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] cifrarRSA(byte[] clearText, PublicKey key)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		if (ApplicationManager.D) {
			Log.d(TAG, "->cifrarRSA()");
		}
		// creamos la instancia de RSA
		Cipher cipher = Cipher.getInstance("RSA");
		// cifrando con la llave publica
		cipher.init(Cipher.ENCRYPT_MODE, key);
		// regresamos los bytes cifrados
		return cipher.doFinal(clearText);
	}

	/**
	 * 
	 * @param cipherText
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] descifrarRSA(byte[] cipherText, PrivateKey key)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		if (ApplicationManager.D) {
			Log.d(TAG, "->descifrarRSA()");
		}
		// creamos la instancia de RSA
		Cipher cipher = Cipher.getInstance("RSA");
		// descifrando con la llave privada del receptor
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(cipherText);
	}

	/**
	 * 
	 * @param cipherData
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public String descifrarRSA(byte[] cipherData)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, IOException {
		if (ApplicationManager.D) {
			Log.d(TAG, "->descifrarRSA()");
		}
		// creamos la instancia de RSA
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		// cifrando con la llave publica
		cipher.init(Cipher.DECRYPT_MODE, getPubKey());
		// regresamos la cadena descifrada
		return new String(cipher.doFinal(cipherData));
	}

	/**
	 * 
	 * @param clearData
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public String cifrarRSA(String clearData) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		if (ApplicationManager.D) {
			Log.d(TAG, "->cifrarRSA()");
		}
		if (ApplicationManager.D) {
			Log.d(TAG, "clearData: " + clearData);
		}

		// creamos la instancia de RSA
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		// cifrando con la llave publica
		cipher.init(Cipher.ENCRYPT_MODE, getPubKey());
		// regresamos la cadena descifrada
		String encoded = Base64
				.encodeBytes(cipher.doFinal(clearData.getBytes()));
		if (ApplicationManager.D) {
			Log.d(TAG, "encoded: " + encoded);
		}

		return encoded;
	}
}
