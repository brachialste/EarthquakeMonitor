package com.brachialste.earthquakemonitor.security;

import java.security.SecureRandom;

/**
 * Created by brachialste on 24/03/15.
 */
public class RandomGenerator {

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        SecureRandom rand = new SecureRandom();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /**
     * Regresa un arreglo pseudo-aleatorio de bytes de longitud size.
     *
     * @param size Tamaño del arreglo
     *
     * @return byte array
     */
    public static byte[] randByteArray(int size){
        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        SecureRandom rand = new SecureRandom();
        byte[] b = new byte[size];
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        rand.nextBytes(b);

        return b;
    }
}
