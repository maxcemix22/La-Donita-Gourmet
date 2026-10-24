package com.ladonita.util;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * Validaciones de datos de entrada del lado del servidor.
 * Usa Apache Commons Lang (texto y números) y Commons Validator (correos según RFC).
 */
public final class Validador {

    private static final Pattern ID = Pattern.compile("^[A-Z]{3}\\d{3}$");
    private static final EmailValidator CORREO = EmailValidator.getInstance();

    private Validador() {
    }

    /** null, vacío o solo espacios. */
    public static boolean vacio(String s) {
        return StringUtils.isBlank(s);
    }

    public static boolean longitudValida(String s, int max) {
        return StringUtils.isNotBlank(s) && StringUtils.length(s.trim()) <= max;
    }

    /** Solo dígitos, entre 7 y 15 (celulares y fijos con o sin código de país). */
    public static boolean telefono(String s) {
        String t = StringUtils.trimToEmpty(s);
        return StringUtils.isNumeric(t) && t.length() >= 7 && t.length() <= 15;
    }

    public static boolean correo(String s) {
        String t = StringUtils.trimToEmpty(s);
        return t.length() <= 100 && CORREO.isValid(t);
    }

    /** IDs del sistema: tres letras + tres dígitos (PRO001, PED001...). */
    public static boolean id(String s) {
        return s != null && ID.matcher(s).matches();
    }

    /** Quita espacios al inicio y al final y junta los espacios repetidos ("Ana   María " → "Ana María"). */
    public static String limpiar(String s) {
        return s == null ? null : StringUtils.normalizeSpace(s);
    }

    public static int entero(String s, int porDefecto) {
        return NumberUtils.toInt(StringUtils.trimToEmpty(s), porDefecto);
    }
}
