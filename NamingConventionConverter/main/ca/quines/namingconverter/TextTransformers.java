package ca.quines.namingconverter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TextTransformers {

	/** "Remove Dashes and Trim" */
    public static String removeDashesAndTrim(String input) {
        if (input == null) return "";
        return input.replace("-", "").trim();
    }

    /** "Replace Dashes with Spaces" */
    public static String replaceDashesWithSpaces(String input) {
        if (input == null) return "";
        return input.replace("-", " ");
    }

    /** "Change Underscores to Spaces" */
    public static String underscoresToSpaces(String input) {
        if (input == null) return "";
        return input.replace("_", " ");
    }

    /** "Replace Whitespace with One Space and Trim" */
    public static String collapseWhitespace(String input) {
        if (input == null) return "";
        // \\s+ matches one or more whitespace characters (space, tab, newline)
        return input.replaceAll("\\s+", " ").trim();
    }

    public static String toCamelCase(String input, boolean upperFirst) {
        if (input == null || input.isEmpty()) return input;
        // Split by spaces, underscores, or dashes
        String[] parts = input.split("[\\s_-]+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String word = parts[i].toLowerCase();
            if (i == 0 && !upperFirst) {
                sb.append(word);
            } else {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return sb.toString();
    }

    public static String toUpperSnake(String input) {
        // Handle CamelCase -> SNAKE_CASE
        String result = input.replaceAll("([a-z])([A-Z]+)", "$1_$2");
        return result.replace(" ", "_").toUpperCase();
    }

    public static String toSqlInClause(String input, boolean isString) {
        String[] lines = input.split("\\R");
        String joined = Arrays.stream(lines)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> isString ? "'" + s + "'" : s)
                .collect(Collectors.joining(", "));
        return "(" + joined + ")";
    }

    /** "CamelCase to Spaces" -> "Camel Case" */
    public static String camelCaseToSpaces(String input) {
        if (input == null) return "";
        // Insert a space between a lowercase letter and an uppercase letter
        // Using $1 $2 to reference the captured groups
        return input.replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    /** "Spaces to UPPER_SNAKE_CASE" -> "UPPER_SNAKE_CASE" */
    public static String spacesToUpperSnake(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", "_").toUpperCase();
    }

    /** "Camel Case to UPPER_SNAKE_CASE" -> "CAMEL_CASE" */
    public static String camelCaseToUpperSnake(String input) {
        if (input == null) return "";
        // First convert humps to spaces, then spaces to underscores
        String spaced = camelCaseToSpaces(input);
        return spacesToUpperSnake(spaced);
    }

    /** Helper for snake to camel/Camel */
    private static String snakeToGenericCamel(String input, boolean upperFirst) {
        if (input == null || input.isEmpty()) return "";
        String[] parts = input.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String word = parts[i];
            if (word.isEmpty()) continue;
            if (i == 0 && !upperFirst) {
                sb.append(word);
            } else {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return sb.toString();
    }

    /** "UPPER_SNAKE_CASE to camelCase" */
    public static String snakeToCamelCase(String input) {
        return snakeToGenericCamel(input, false);
    }

    /** "UPPER_SNAKE_CASE to CamelCase" */
    public static String snakeToPascalCase(String input) {
        return snakeToGenericCamel(input, true);
    }

    // A set of words that should remain lowercase in Title Case
    private static final Set<String> MINOR_WORDS = new HashSet<>(Arrays.asList(
        "a", "an", "and", "as", "at", "but", "by", "for", "in", "nor", "of", "on", "or", "so", "the", "to", "up", "yet"
    ));

    /** "UPPERCASE" */
    public static String toUpperCase(String input) {
        return input == null ? "" : input.toUpperCase();
    }

    /** "lowercase" */
    public static String toLowerCase(String input) {
        return input == null ? "" : input.toLowerCase();
    }

    /** "All Initial Capitals" - Every word starts with a Cap */
    public static String allInitialCaps(String input) {
        if (input == null || input.isEmpty()) return "";
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    /** "Convert to Title Case" - Smart capitalization */
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return "";
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i > 0 && i < words.length - 1 && MINOR_WORDS.contains(word)) {
                sb.append(word);
            } else {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
            if (i < words.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    /** "lowercase First Character" */
    public static String lowercaseFirst(String input) {
        if (input == null || input.isEmpty()) return "";
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

    /** "Uppercase First Character" */
    public static String uppercaseFirst(String input) {
        if (input == null || input.isEmpty()) return "";
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    /** "Newline separated to Comma Delimited" -> 1, 2, 3 */
    public static String newlineToComma(String input) {
        if (input == null) return "";
        return Arrays.stream(input.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
    }

    /** "Newline separated to Quoted Comma Delimited" -> 'A', 'B', 'C' */
    public static String newlineToQuotedComma(String input) {
        if (input == null) return "";
        return Arrays.stream(input.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(", "));
    }

    /** "In Clause for Integers" -> IN (1, 2, 3) */
    public static String toInClauseInt(String input) {
        if (input == null || input.isEmpty()) return "";
        return "IN (" + newlineToComma(input) + ")";
    }

    /** "In Clause for Strings" -> IN ('A', 'B', 'C') */
    public static String toInClauseString(String input) {
        if (input == null || input.isEmpty()) return "";
        return "IN (" + newlineToQuotedComma(input) + ")";
    }

}
