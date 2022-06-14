package lphy.core.distributions;

import lphy.graphicalModel.*;
import lphy.util.RandomUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

import static lphy.core.distributions.DistributionConstants.meanParamName;
import static lphy.core.distributions.DistributionConstants.sdParamName;
import static lphy.graphicalModel.ValueUtils.doubleValue;
import static org.apache.commons.math3.distribution.NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY;

/**
 * Normal distribution prior.
 * @see NormalDistribution
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class Normal extends PriorDistributionGenerator<Double> implements GenerativeDistribution1D<Double> {

    private Value<Number> mean;
    private Value<Number> sd;

    NormalDistribution normalDistribution;

    public Normal(@ParameterInfo(name = "mean", description = "the mean of the distribution.") Value<Number> mean,
                  @ParameterInfo(name = "sd", narrativeName = "standard deviation", description = "the standard deviation of the distribution.") Value<Number> sd) {
        super();
        this.mean = mean;
        this.sd = sd;

        constructDistribution(random);
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
        if (mean == null) throw new IllegalArgumentException("The mean value can't be null!");
        if (sd == null) throw new IllegalArgumentException("The sd value can't be null!");

        normalDistribution = new NormalDistribution(RandomUtils.getRandom(), doubleValue(mean), doubleValue(sd),
                DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    @GeneratorInfo(name = "Normal", verbClause = "has", narrativeName = "normal prior",
            category = GeneratorCategory.PROB_DIST, examples = {"simplePhyloBrownian.lphy","simplePhyloOU.lphy"},
            description = "The normal probability distribution.")
    public RandomVariable<Double> sample() {
        // constructDistribution() only required in constructor and setParam
        double x = normalDistribution.sample();
        return new RandomVariable<>("x", x, this);
    }

    @Override
    public double density(Double x) {
        return normalDistribution.density(x);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(meanParamName, mean);
            put(sdParamName, sd);
        }};
    }

    public Value<Number> getMean() {
        return mean;
    }

    public Value<Number> getSd() {
        return sd;
    }

    private static final Double[] domainBounds = {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};

    public Double[] getDomainBounds() {
        return domainBounds;
    }
}