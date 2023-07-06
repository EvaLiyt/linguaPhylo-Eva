package lphy.core.spi;

import lphy.core.logger.ValueFormatter;
import lphy.core.simulator.SimulatorListener;

import java.util.*;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link ValueFormatter} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyCoreValueFormatterImpl implements LPhyValueFormatter {

    /**
     * Required by ServiceLoader.
     */
    public LPhyCoreValueFormatterImpl() { }

    @Override
    public Map<Class<?>, Set<Class<? extends ValueFormatter>>> getValueFormatterMap() {
        return new HashMap<>();
    }

    @Override
    public List<Class<? extends SimulatorListener>> getSimulatorListenerClasses() {
        return new ArrayList<>();
    }

    public String getExtensionName() {
        return "LPhy core loggers";
    }
}
