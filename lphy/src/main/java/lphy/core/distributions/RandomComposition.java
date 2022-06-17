package lphy.core.distributions;

import lphy.graphicalModel.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

import static lphy.core.distributions.DistributionConstants.nParamName;

public class RandomComposition extends PriorDistributionGenerator<Integer[]> {

    private static final String kParamName = "k";
    private Value<Integer> n;
    private Value<Integer> k;

    public RandomComposition(@ParameterInfo(name = nParamName, description = "the sum of the random tuple.") Value<Integer> n,
                             @ParameterInfo(name = kParamName, description = "the size of the random tuple.") Value<Integer> k) {
        super();
        this.n = n;
        this.k = k;
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "RandomComposition",
            category = GeneratorCategory.PRIOR,
            examples = {"skylineCoalescent.lphy", "https://linguaphylo.github.io/tutorials/skyline-plots/"},
            description = "Samples a random "+ kParamName + "-tuple of positive integers that sum to " + nParamName + ".")
    public RandomVariable<Integer[]> sample() {
        List<Integer> bars = new ArrayList<>();

        bars.add(0);
        while (bars.size() < k.value()) {
            int candidate = random.nextInt(n.value() - 1) + 1;
            if (!bars.contains(candidate)) {
                bars.add(candidate);
            }
        }
        bars.add(n.value());
        Collections.sort(bars);

        Integer[] composition = new Integer[k.value()];
        for (int i = 0; i < composition.length; i++) {
            composition[i] = bars.get(i + 1) - bars.get(i);
        }
        return new RandomVariable<>("x", composition, this);
    }

    public Map<String, Value> getParams() {
        return new TreeMap<>() {{
            put(nParamName, n);
            put(kParamName, k);
        }};
    }

}
