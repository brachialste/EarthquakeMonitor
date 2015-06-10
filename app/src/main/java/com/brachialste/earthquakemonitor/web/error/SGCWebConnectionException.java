package com.brachialste.earthquakemonitor.web.error;

/**
 * Clase encargada de definir las excepciones causadas por algún error en la
 * conexión con el SGC Web
 * 
 * @author rcarvente
 * 
 */
public class SGCWebConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8664757522936901795L;

	// Parameterless Constructor
	public SGCWebConnectionException() {
	}

	// Constructor that accepts a message
	public SGCWebConnectionException(String message) {
		super(message);
	}
}
