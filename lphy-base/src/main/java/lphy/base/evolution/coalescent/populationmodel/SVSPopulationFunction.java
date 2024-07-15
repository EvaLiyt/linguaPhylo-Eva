package lphy.base.evolution.coalescent.populationmodel;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.Value;

import java.util.Map;

import static lphy.base.evolution.coalescent.populationmodel.SVSFunction.INDICATOR_PARAM_NAME;
import static lphy.base.evolution.coalescent.populationmodel.SVSFunction.MODELS_PARAM_NAME;

public class SVSPopulationFunction implements PopulationFunction {

    private PopulationFunction model;

    public SVSPopulationFunction(PopulationFunction f) {
        this.model = f;
    }

    @Override
    public double getTheta(double t) {
        return model.getTheta(t);
    }

    @Override
    public double getIntensity(double t) {
        return model.getIntensity(t);
    }

    @Override
    public double getInverseIntensity(double x) {
        return model.getInverseIntensity(x);
    }

    @Override
    public boolean isAnalytical() {
        return model.isAnalytical();
    }


    public PopulationFunction getModel() {
        return model;
    }

    public Value getIndicator() {
        return (Value<Integer>) getParams().get(INDICATOR_PARAM_NAME);
    }

    private Map<Object, Object> getParams() {
        return null;
    }

    public Value<PopulationFunction[]> getModels() {
        return (Value<PopulationFunction[]>) getParams().get(MODELS_PARAM_NAME);
    }

}