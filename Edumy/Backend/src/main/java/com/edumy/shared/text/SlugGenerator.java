package com.edumy.shared.text;

import java.util.regex.Pattern;

/**
 * URL slug generator for categories and courses using VietnameseNormalizer.
 */
public final class SlugGenerator {

    private static final Pattern NOT_SLUG_CHARS = Pattern.compile("[^a-z0-9]+");

    private SlugGenerator() {
    }

    /** "Lập trình Java & Spring" -> "lap-trinh-java-spring" */
    public static String slugify(String input) {
        if (input == null) {
            return "";
        }
        String hyphenated = NOT_SLUG_CHARS.matcher(VietnameseNormalizer.normalize(input)).replaceAll("-");
        return trimHyphens(hyphenated);
    }

    /** slugify cut to at most maxLength characters, never ending with a hyphen. */
    public static String slugify(String input, int maxLength) {
        if (maxLength < 1) {
            throw new IllegalArgumentException("maxLength must be positive");
        }
        String slug = slugify(input);
        return slug.length() <= maxLength ? slug : trimHyphens(slug.substring(0, maxLength));
    }

    private static String trimHyphens(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == '-') {
            start++;
        }
        while (end > start && value.charAt(end - 1) == '-') {
            end--;
        }
        return value.substring(start, end);
    }
}
