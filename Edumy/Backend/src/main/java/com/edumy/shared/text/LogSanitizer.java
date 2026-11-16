package com.edumy.shared.text;

/**
 * Sanitizes free text entered by users before writing to logs (CWE-117 log injection prevention).
 */
public final class LogSanitizer {

    public static final int MAX_LENGTH = 200;
    private static final char REPLACEMENT = '_';

    private LogSanitizer() {
    }

    public static String clean(String text) {
        if (text == null) {
            return "-";
        }
        int kept = Math.min(text.length(), MAX_LENGTH);
        StringBuilder out = new StringBuilder(kept + 16);
        for (int i = 0; i < kept; i++) {
            char c = text.charAt(i);
            out.append(isLineBreaking(c) ? REPLACEMENT : c);
        }
        if (text.length() > MAX_LENGTH) {
            out.append("…(+").append(text.length() - MAX_LENGTH).append(')');
        }
        return out.toString();
    }

    private static boolean isLineBreaking(char c) {
        return Character.isISOControl(c) || c == '\u2028' || c == '\u2029';
    }
}
