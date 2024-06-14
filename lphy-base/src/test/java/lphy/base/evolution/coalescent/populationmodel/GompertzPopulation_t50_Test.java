package lphy.base.evolution.coalescent.populationmodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;

public class GompertzPopulation_t50_Test {

    private static final double DELTA = 1e-6;
    private GompertzPopulation_t50 gompertzPopulation_t50;

    @BeforeEach
    public void setUp() {
        double t50 = 10.0;
        double b = 0.1;
        double NInfinity = 1000;
        gompertzPopulation_t50 = new GompertzPopulation_t50(t50, b, NInfinity);
    }

    @Test
    public void testGetTheta() {
        double t = gompertzPopulation_t50.getTimeForGivenProportion(0.5);
        double N0 = gompertzPopulation_t50.getN0();
        double NInfinity = 1000;
  //      double b = 0.1;

        double expectedTheta = NInfinity / 2;
        double actualTheta = gompertzPopulation_t50.getTheta(t);
        assertEquals(expectedTheta, actualTheta, DELTA);
    }

    @Test
    public void testGetThetaAtT50() {
        double t50 = 10.0;  // Example t50 value
        double b = 0.1;     // Example growth rate
        double NInfinity = 1000;  // Example carrying capacity

        GompertzPopulation_t50 gompertzPopulation_t50 = new GompertzPopulation_t50(t50, b, NInfinity);

        double expectedTheta = NInfinity / 2;
        double actualTheta = gompertzPopulation_t50.getTheta(t50);
        assertEquals(expectedTheta, actualTheta, DELTA);
    }

    @Test
    public void testGetIntensity() {
        double t = 5.0;
        UnivariateFunction function = time -> 1 / gompertzPopulation_t50.getTheta(time);
        IterativeLegendreGaussIntegrator integrator = new IterativeLegendreGaussIntegrator(5, 1.0e-10, 1.0e-9, 2, 100000);
        double expectedIntensity = integrator.integrate(Integer.MAX_VALUE, function, 0, t);

        double actualIntensity = gompertzPopulation_t50.getIntensity(t);
        assertEquals(expectedIntensity, actualIntensity, DELTA);
    }

    @Test
    public void testGetInverseIntensity() {
        double t = 5.0;
        double intensity = gompertzPopulation_t50.getIntensity(t);

        double computedTime = gompertzPopulation_t50.getInverseIntensity(intensity);

        assertEquals(t, computedTime, DELTA);
    }

    @Test
    public void testInverseIntensity() {
        double targetIntensity = 0.309539;


        double calculatedTime = gompertzPopulation_t50.getInverseIntensity(targetIntensity);
        double calculatedIntensityAtCalculatedTime = gompertzPopulation_t50.getIntensity(calculatedTime);

        assertEquals(targetIntensity, calculatedIntensityAtCalculatedTime, DELTA);
    }

}