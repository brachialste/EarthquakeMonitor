package com.brachialste.earthquakemonitor.web.error;

/**
 * Clase encargada de definir la excepción especial cuando la sesión ha caducado
 * para manejar la reconexión con el SGC Web
 * 
 * @author rcarvente
 * 
 */
public class ExpiredSessionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2601136657673058497L;

	// Parameterless Constructor
	public ExpiredSessionException() {
	}

	// Constructor that accepts a message
	public ExpiredSessionException(String message) {
		super(message);
	}
}
