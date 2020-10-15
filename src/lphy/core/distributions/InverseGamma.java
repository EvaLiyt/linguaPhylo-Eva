package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.GammaDistribution;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.scaleParamName;
import static lphy.core.distributions.DistributionConstants.shapeParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;

/**
 * Gamma distribution
 */
public class InverseGamma implements GenerativeDistribution1D<Double> {

    private Value<Number> shape;
    private Value<Number> scale;

    GammaDistribution gammaDistribution;

    public InverseGamma(@ParameterInfo(name = shapeParamName, description = "the shape of the distribution.") Value<Number> shape,
                        @ParameterInfo(name = scaleParamName, description = "the scale of the distribution.") Value<Number> scale) {

        this.shape = shape;
        if (shape == null) throw new IllegalArgumentException("The " + shapeParamName + " value can't be null!");
        this.scale = scale;
        if (scale == null) throw new IllegalArgumentException("The " + scaleParamName + " value can't be null!");

        constructGammaDistribution();
    }

    @GeneratorInfo(name = "InverseGamma", description = "The inverse-gamma probability distribution.")
    public RandomVariable<Double> sample() {
        constructGammaDistribution();
        double x = 1.0 / gammaDistribution.sample();
        return new RandomVariable<>(null, x, this);
    }

    @Override
    public double density(Double x) {
        return gammaDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(shapeParamName, shape);
            put(scaleParamName, scale);
        }};
    }

    @Override
    public void setParam(String paramName, Value value) {
        if (shapeParamName.equals(paramName)) {
            shape = value;
        } else if (scaleParamName.equals(paramName)) {
            scale = value;
        } else {
            throw new RuntimeException("Unrecognised parameter name: " + paramName);
        }

        constructGammaDistribution();
    }

    private void constructGammaDistribution() {
        double a = doubleValue(shape);
        double b = doubleValue(scale);

        gammaDistribution = new GammaDistribution(a, b);
    }

    public String toString() {
        return getName();
    }

    public Value<Number> getScale() {
        return scale;
    }

    public Value<Number> getShape() {
        return shape;
    }

    private static final Double[] domainBounds = {0.0, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}