package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.offsetParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;
import static org.apache.commons.math3.distribution.LogNormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY;

/**
 * log-normal prior.
 * @see LogNormalDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class LogNormal extends ParametricDistribution<Double> implements GenerativeDistribution1D<Double> {

    public static final String meanLogParamName = "meanlog";
    public static final String sdLogParamName = "sdlog";
    private Value<Number> M;
    private Value<Number> S;
    private Value<Number> offset;

    LogNormalDistribution logNormalDistribution;

    public LogNormal(@ParameterInfo(name = meanLogParamName, narrativeName = "mean in log space", description = "the mean of the distribution on the log scale.") Value<Number> M,
                     @ParameterInfo(name = sdLogParamName, narrativeName = "standard deviation in log space", description = "the standard deviation of the distribution on the log scale.") Value<Number> S,
                     @ParameterInfo(name = offsetParamName, optional = true, narrativeName = "offset", description = "optional parameter to add a constant to the returned result. default is 0.") Value<Number> offset) {
        super();
        this.M = M;
        this.S = S;
        this.offset = offset;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        // use code available since apache math 3.1
        logNormalDistribution = new LogNormalDistribution(random, doubleValue(M), doubleValue(S),
                DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "LogNormal", verbClause = "has", narrativeName = "log-normal prior",
            category = GeneratorCategory.PRIOR, examples = {"hkyCoalescent.lphy","errorModel1.lphy"},
            description = "The log-normal probability distribution.")
    public RandomVariable<Double> sample() {
        // .sample() is before offset
        double result = logNormalDistribution.sample() + C();
        // constructDistribution() only required in constructor and setParam
        return new RandomVariable<>(null, result, this);
    }

    // default offset=0
    private double C() {
        double C = 0;
        if (offset != null) {
            C = doubleValue(offset);
        }
        return C;
    }

    public double logDensity(Double x) {
        // x is after offset, so - to get the original point
        return logNormalDistribution.logDensity(x-C());
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(meanLogParamName, M);
            put(sdLogParamName, S);
        }};
    }

    public Value<Number> getMeanLog() {
        return M;
    }

    public Value<Number> getSDLog() {
        return S;
    }

    private static final Double[] domainBounds = {0.0, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}