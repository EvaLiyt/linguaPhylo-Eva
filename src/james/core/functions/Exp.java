package james.core.functions;

import james.graphicalModel.DoubleValue;
import james.graphicalModel.Function;
import james.graphicalModel.FunctionInfo;
import james.graphicalModel.Value;

public class Exp extends Function<Double, Double> {

    @FunctionInfo(name="exp",description = "The exponential function: e^x")
    public Value<Double> apply(Value<Double> v) {
        setParam("x", v);
        return new DoubleValue("exp(" + v.getId() + ")", Math.exp(v.value()), this);
    }
}
