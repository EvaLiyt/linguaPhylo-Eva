package lphy.base;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.Value;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.ParserSingleton;
import lphy.core.simulator.RandomUtils;
import lphy.core.simulator.Sampler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static lphy.core.parser.ParserSingleton.getParser;
import static lphy.core.parser.ParserSingleton.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ResampleTest {

    @BeforeEach
    void setUp() {
        RandomUtils.setSeed(777);
        // Note ParserSingleton does not support multi-thread,
        // so only 1 test method below
        ParserSingleton.clear();
    }

    /**
     * Test the core method {@link Sampler#sample(Long)} to simulate values from parser dictionary.
     * This is distinguished with parsing, which also simulate values.
     * 1. test after resampling, the dictionary still keeps the same values as the parsing process;
     * 2. test after setValue and resample, the dictionary is correct,
     *    and also the tree is resampled by new taxa.
     */
    @Test
    public void testResample() {
        final int n = 16;
        parse("Θ ~ LogNormal(meanlog=3.0, sdlog=1.0);");
        parse("ψ ~ Coalescent(n=" + n + ", theta=Θ);");

        LPhyParserDictionary parserDictionary = getParser();
        // deep clone
        Map<String, Value<?>> modelDictByParsing = parserDictionary.getModelDictionary().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Set<Value> valueSetByParsing = new HashSet<>(parserDictionary.getModelValues());

        assertEquals(2, modelDictByParsing.size(), "model dict map size by parsing : ");
        assertEquals(5, valueSetByParsing.size(), "model value set size by parsing : ");

        // resample
        Sampler sampler = new Sampler(parserDictionary);
        // at random seed
        sampler.sample(null);

        Map<String, Value<?>> modelDict2 = parserDictionary.getModelDictionary();
        Set<Value> valueSet2 = parserDictionary.getModelValues();

        assertEquals(2, modelDict2.size(), "model dict map size by resampling : ");
        assertEquals(5, valueSet2.size(), "model value set size by resampling : ");

        assertEquals(modelDictByParsing.keySet(), modelDict2.keySet());

        // random values should change
        assertNotEquals(modelDictByParsing.get("Θ").value(), modelDict2.get("Θ").value(), "Θ should change");
        assertNotEquals(modelDictByParsing.get("ψ").value().toString(), modelDict2.get("ψ").value().toString(), "ψ should change");

        //*** test set value ***//
        final int ntaxa = 5;
        for (Value nVal : valueSet2) {
            if (nVal.value().equals(n))
                nVal.setValue(ntaxa); // set taxa to 5
        }

        // resample
        sampler.sample(null);

        modelDict2 = parserDictionary.getModelDictionary();
        parserDictionary.getModelValues();
        assertEquals(2, modelDict2.size(), "model dict map size by resampling : ");
        assertEquals(5, valueSet2.size(), "model value set size by resampling : ");

        TimeTree tree = (TimeTree) modelDict2.get("ψ").value();

        assertEquals(ntaxa, tree.n());
        assertEquals(ntaxa, tree.leafCount());
    }

}
