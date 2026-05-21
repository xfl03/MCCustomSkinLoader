package customskinloader.bootstrap.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared matcher for protocol and ordinal range expressions.
 */
public final class RangeMatcher {
    private RangeMatcher() {
    }

    public static boolean matches(String expression, int protocolVersion, int worldVersion) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        String protocolRange = "";
        String worldRange = "";
        int index = expression.indexOf("+");
        if (index >= 0) {
            protocolRange = expression.substring(0, index);
            worldRange = expression.substring(index + 1);
        } else {
            protocolRange = expression;
        }
        return matches(protocolRange, protocolVersion) && matches(worldRange, worldVersion);
    }

    private static boolean matches(String expression, int value) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        for (String token : splitTokens(expression)) {
            String trimmedToken = token.trim();
            if (trimmedToken.isEmpty()) {
                continue;
            }

            if (trimmedToken.startsWith("[") && trimmedToken.endsWith("]")) {
                int commaIndex = trimmedToken.indexOf(',');
                if (commaIndex < 0) {
                    throw new IllegalArgumentException("Invalid range token: " + trimmedToken);
                }

                String leftPart = trimmedToken.substring(1, commaIndex).trim();
                String rightPart = trimmedToken.substring(commaIndex + 1, trimmedToken.length() - 1).trim();
                int lowerBound = leftPart.isEmpty() ? 0 : parseNumber(leftPart);
                int upperBound = rightPart.isEmpty() ? Integer.MAX_VALUE : parseNumber(rightPart);
                if (value >= lowerBound && value <= upperBound) {
                    return true;
                }
                continue;
            }

            if (value == parseNumber(trimmedToken)) {
                return true;
            }
        }

        return false;
    }

    private static List<String> splitTokens(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        int bracketDepth = 0;

        for (int index = 0; index < expression.length(); index++) {
            char currentChar = expression.charAt(index);
            if (currentChar == '[') {
                bracketDepth++;
            } else if (currentChar == ']') {
                bracketDepth--;
            }

            if (currentChar == ',' && bracketDepth == 0) {
                tokens.add(currentToken.toString());
                currentToken.setLength(0);
                continue;
            }

            currentToken.append(currentChar);
        }

        tokens.add(currentToken.toString());
        return tokens;
    }

    private static int parseNumber(String token) {
        String trimmedToken = token.trim();
        if (trimmedToken.startsWith("0x")) {
            return Integer.parseInt(trimmedToken.substring(2), 16);
        }

        return Integer.parseInt(trimmedToken, 10);
    }
}
