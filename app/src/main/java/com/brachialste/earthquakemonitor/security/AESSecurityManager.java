package com.brachialste.earthquakemonitor.security;

import android.util.Log;

import com.brachialste.earthquakemonitor.utils.AndroidIdentifier;
import com.brachialste.earthquakemonitor.utils.Utilities;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by brachialste on 12/09/14.
 */
public class AESSecurityManager {

    public static String TAG = "AESSecurityManager";

    private String seed = null;

    // referencia a la instancia actual del RegisterManager
    private static AESSecurityManager aes_man;

    /**
     * Método encargado de obtener la instancia actual del AESSecurityManager o una nueva
     * @return
     */
    public static synchronized AESSecurityManager getInstance() {
        if (aes_man == null) {
            aes_man = new AESSecurityManager();
        }
        return aes_man;
    }

    /**
     * Constructor de la clase
     */
    public AESSecurityManager(){
        // obtenemos la semilla para el cifrado/descifrado
        seed = "7ea051eacabb2db9"; // TODO: proteger
    }

    /**
     * Metodo privado para la obtencion del hash de la cadena pasada como
     * argumento.
     *
     * @param cadena
     *            Cadena con los caracteres los cuales van a sufrir la
     *            transformacion con la funcion hash,
     * @return
     * @throws NoSuchAlgorithmException
     *              Si no existe implementacion para el algoritmo de MD5.
     * @throws UnsupportedEncodingException
     *              Si no existe la codificación UTF-8
     */
    private byte[] obtenerHash(String cadena) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        if (cadena != null) {
            try {
                MessageDigest m = MessageDigest.getInstance("MD5");
                byte[] data = cadena.getBytes("UTF-8");
                m.update(data, 0, data.length);
                BigInteger i = new BigInteger(1, m.digest());
                String hash = i.toString(16);
                hash = Utilities.padLeft(hash, 32).replace(' ', '0');
                return hash.getBytes("UTF-8");
            } catch (NoSuchAlgorithmException e1) {
                Log.e(TAG, e1.getMessage());
                return null;
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }else{
            return null;
        }
    }

    /**
     * Genera el vector de inicializacion necesario para el algoritmo AES en
     * modo CBC, este vector se toma a partir de los primeros 16 bytes del hash
     * de la clave generada.
     *
     * @param cadena
     *            Cadena con la semilla secreta
     * @exception NoSuchAlgorithmException
     *                Si no existe implementacion para el algoritmo de MD5.
     */
    private IvParameterSpec obtenerVI(String cadena)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // Obtener un vector de inicializacion usando
        // los primeros 16 bytes del hash (deben ser 16!)
        String iv = new String(obtenerHash(cadena)).substring(0, 16);
        IvParameterSpec vi = new IvParameterSpec(iv.getBytes("UTF-8"));
        return vi;
    }

    /**
     * Genera la clave secreta necesaria para el algoritmo AES a partir de una semilla secreta
     * que es transformado a traves de una funcion hash.
     *
     * @param secret_seed
     *            Cadena con la semilla secreta
     * @exception NoSuchAlgorithmException
     *                Si no existe implementacion para el algoritmo de AES.
     */
    private SecretKey generateDataKey(String secret_seed)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // Usar el hash del identificador del dispositivo como la clave par el cifrado
        // Inicializar una clave secreta
        SecretKey clave = new SecretKeySpec(obtenerHash(secret_seed), "AES");
        return clave;
    }

    /**
     * Método encargado de cifrar un texto en claro
     * @param cleartext
     * @return
     * @throws Exception
     */
    public String encryptAES(String cleartext) throws Exception {
        byte[] result = encrypt(cleartext.getBytes("UTF-8"));
        return android.util.Base64.encodeToString(result, android.util.Base64.DEFAULT);
    }

    /**
     * Método encargado de descifrar un texto cifrado
     * @param encrypted
     * @return
     * @throws Exception
     */
    public String decryptAES(String encrypted) throws Exception {
        byte[] enc = android.util.Base64.decode(encrypted, android.util.Base64.DEFAULT);
        byte[] result = decrypt(enc);
        return new String(result, "UTF-8");
    }

    /**
     * Método para cifrar contenido en claro
     * @param clear
     * @return
     * @throws Exception
     */
    private byte[] encrypt(byte[] clear) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, generateDataKey(seed), obtenerVI(seed));
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    /**
     * Método para descifrar un contenido cifrado
     * @param encrypted
     * @return
     * @throws Exception
     */
    private byte[] decrypt(byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, generateDataKey(seed), obtenerVI(seed));
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

}
