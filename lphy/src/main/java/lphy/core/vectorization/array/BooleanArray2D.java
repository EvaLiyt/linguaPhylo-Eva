package lphy.core.vectorization.array;

import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.BooleanArray2DValue;

public class BooleanArray2D extends ArrayFunction<Boolean[][]> {

    Value<Boolean[]>[] x;

    public BooleanArray2D(Value<Boolean[]>... x) {
        this.x = x;
        super.setInput(x);
    }

    @GeneratorInfo(name = "booleanArray", description = "The constructor function for a 2d array of booleans.")
    public Value<Boolean[][]> apply() {

        Boolean[][] values = new Boolean[x.length][];

        for (int i = 0; i < x.length; i++) {
            if (x[i] != null) // handle null
                values[i] = x[i].value();
        }

        return new BooleanArray2DValue(null, values, this);
    }

    @Override
    public void setElement(Value value, int i) {
        x[i] = value;
    }

    @Override
    public Value<Boolean[]>[] getValues() {
        return x;
    }
}
