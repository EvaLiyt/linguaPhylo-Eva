package lphy.base.evolution.tree;

import lphy.base.evolution.coalescent.Coalescent;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubsampledTreeTest {
    final int nTaxa = 16;
    TimeTree tree;

    @BeforeEach
    void setUp() {
        Coalescent simulator = new Coalescent(new Value<>("Θ", 10.0), new Value<>("n", nTaxa), null);
        tree = Objects.requireNonNull(simulator.sample()).value();
        // antlr convoluted stuff (oof...)
//        String trNewick = "((1:2.0, (2:1.0, 3:1.0):1.0):2.0, 4:4.0)";
//        String trNewick = "(\"5\":12.847126431848253,(\"2\":3.6311734886498632,(\"4\":1.2502720441864856,(\"3\":1.065983209110808,\"1\":1.065983209110808):0.18428883507567773):2.380901444463378):9.215952943198388):0.0;";
//        CharStream charStream = CharStreams.fromString(trNewick);
//        NewickLexer lexer = new NewickLexer(charStream);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        NewickParser parser = new NewickParser(tokens);
//        ParseTree parseTree = parser.tree();
//        NewickASTVisitor visitor = new NewickASTVisitor();
//
//        // lphy
//        TimeTreeNode root = visitor.visit(parseTree);
//        this.tree = new TimeTree();
//        this.tree.setRoot(root);
//        System.out.println(this.tree.toNewick(true));

    }

    @Test
    void getSampleResult() {
        String[] name = tree.getTaxaNames();
        double fraction = 0.5;

        Double[] fractionList = {0.5, 0.02};
        Value<TimeTree> tree = new Value<>("timeTree", this.tree);
        Value<Double[]> fractionValue = new Value<>("fraction",fractionList);
        Value<String[]> nameList = new Value<>("nameList", this.tree.getTaxaNames());
        String[][] nameListValue = new String[2][nameList.value().length];
        nameListValue[0] = nameList.value();
        nameListValue[1] = nameList.value();
        Value<String[][]> nameListArray = new Value<>("nameListArray", nameListValue);
        SubsampledTree instance = new SubsampledTree(tree, nameListArray, fractionValue);

        String[] observe = instance.getSampleResult(fraction, name);
        assertEquals(0.5, (double) observe.length /name.length, 0.01);
    }

    @Test
    void combineTwoArray() {
        String[] array1 = {"1", "2"};
        String[] array2 = {"3", "4"};
        String[] observe = SubsampledTree.combineTwoArray(array1, array2);
        String[] expect = {"1", "2", "3", "4"};
        assertArrayEquals(expect, observe);
    }

    @Test
    void getSampledTree() {
        String[] sampledNames = {"1","4"};

        TimeTree observe = new TimeTree(tree);
        String[] newTreeLeafNodeNames = new String[observe.getRoot().getAllLeafNodes().size()];
        for (int j = 0; j< newTreeLeafNodeNames.length; j ++){
            TimeTreeNode node = observe.getRoot().getAllLeafNodes().get(j);
            newTreeLeafNodeNames[j] = node.getId();
        }
        SubsampledTree.getSampledTree(observe, sampledNames);

        String[] observedNames = new String[observe.getRoot().getAllLeafNodes().size()];
        for (int i = 0; i<observedNames.length; i++){
            TimeTreeNode node = observe.getRoot().getAllLeafNodes().get(i);
            observedNames[i] = node.getId();
        }

        assertEquals(sampledNames.length, observedNames.length);
        assertEquals(sampledNames.length, observe.getRoot().getAllLeafNodes().size());
    }

    @Test
    void getLeafList() {
        int expect = tree.getRoot().getAllLeafNodes().size();

        TimeTreeNode[] nodes = tree.getNodes().toArray(new TimeTreeNode[0]);
        String[] names = new String[nodes.length];
        for (int i = 0; i<nodes.length; i++){
            names[i] = nodes[i].getId();
        }

        int observe = SubsampledTree.getLeafList(tree, names).length;
        assertEquals(expect,observe);
    }

    @Test
    void SubsampledTree() {
        String[] tumourNames = {"2","3","4", "6"};
        String[] normalNames = {"1","5","14","17","15"};

        double tumourFraction = 0.5;
        double normalFraction = 0.4;
        Double[] fractionList = {tumourFraction, normalFraction};

        Value<TimeTree> tree = new Value<>("timeTree", this.tree);
        Value<Double[]> fractionValue = new Value<>("fraction",fractionList);
        Value<String[]> tumourNameList = new Value<>("tumourList", tumourNames);
        Value<String[]> normalNameList = new Value<>("normalList",normalNames);
        String[][] nameListValue = new String[2][tumourNameList.value().length+normalNameList.value().length];
        nameListValue[0] = tumourNameList.value();
        nameListValue[1] = normalNameList.value();
        Value<String[][]> nameListArray = new Value<>("nameListArray", nameListValue);
        SubsampledTree observe = new SubsampledTree(tree,nameListArray, fractionValue);
        RandomVariable<TimeTree> observedTree = observe.sample();

        assertEquals(tumourNames.length*tumourFraction + normalNames.length*normalFraction , observedTree.value().getRoot().getAllLeafNodes().size() );
    }
}