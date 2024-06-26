package lphystudio.app.graphicalmodelpanel.viewer;

import lphy.core.model.Value;

public class NumberArrayLabel extends ArrayLabel<Number> {

    public NumberArrayLabel(Value<Number[]> values) {
        super(values);
    }
    
    public NumberArrayLabel(Number[] values) {
        super(values);
    }


    @Override
    public String valueToString(Number rawValue) {
        return DecimalFormat.FORMAT.format(rawValue.doubleValue());
    }
}
