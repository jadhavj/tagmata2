/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gauronit.tagmata.util;

import java.io.FileWriter;

/**
 *
 * @author jjayesh
 */
public class StringUtil {

    public static String removeSpecialChars(String source) {
        return source.replaceAll("[^\\p{Alpha}\\p{Digit}]+", " ");
    }

    public static String[] getTokens(String source) {
        return source.split("[\\s,]+");
    }

    public static String getHTMLEntities(String source) {
        source = source.replace("<", "&lt;");
        source = source.replace(">", "&gt;");
        source = source.replace("&lt;B&gt;", "<B>");
        source = source.replace("&lt;/B&gt;", "</B>");
        return source;
    }
}
