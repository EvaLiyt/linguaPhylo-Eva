package lphy.core.model.datatype;

import lphy.core.model.DeterministicFunction;

public class StringArrayValue extends VectorValue<String> {

    public StringArrayValue(String id, String[] value) {
        super(id, value);
    }

    public StringArrayValue(String id, String[] value, DeterministicFunction function) {
        super(id, value, function);
    }

    public static String quotedString(String str) {
        return "\"" + str + "\"";
    }

    public String valueToString() {

        String[] val = value();

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (val.length > 0) {
            builder.append(quotedString(val[0]));
        }
        for (int i = 1; i < val.length; i++) {
            builder.append(", ");
            builder.append(quotedString(val[i]));
        }
        builder.append("]");
        return builder.toString();
    }

    public String toString() {
        return (isAnonymous() ? "" : (getId() + " = ")) + valueToString();
    }
}
