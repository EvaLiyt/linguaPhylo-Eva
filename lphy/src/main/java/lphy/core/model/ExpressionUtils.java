package lphy.core.model;

public class ExpressionUtils {


    public static boolean isLiteral(String expression) {

        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            // is string
            return true;
        }

        if (expression.startsWith("[") && expression.endsWith("]")) {
            // is list
            return true;
        }

        if (isDouble(expression)) return true;

        if (isInteger(expression)) return true;

        return (isBoolean(expression));
    }

    public static boolean isInteger(String s) {
        try {
            Integer intVal = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String s) {
        try {
            Double doubleVal = Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isBoolean(String s) {
        return s.equals("true") || s.equals("false");
    }
}
