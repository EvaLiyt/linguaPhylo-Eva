package lphy.base.evolution.tree;

import lphy.base.distribution.ParametricDistribution;
import lphy.base.evolution.EvolutionConstants;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.simulator.RandomUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;


public class RandomSample extends ParametricDistribution<TimeTree> {
    Value<TimeTree> tree;
    Value<String[][]> taxaName;
    Value<Double[]> sampleFraction;
    // use the random generator in this class
    protected RandomGenerator random;

    public static final String taxaParamName = EvolutionConstants.taxaParamName;
    public static final String sampleFractionPara = "sampleFraction";
    public static final String treeParamName = "tree";

    public RandomSample(
            @ParameterInfo(name = treeParamName, narrativeName = "full tree", description = "the full tree to extract taxa from.") Value<TimeTree> tree,
            @ParameterInfo(name = taxaParamName, narrativeName = "taxa names", description = "the two taxa names that the function would sample") Value<String[][]> taxaName,
            @ParameterInfo(name = sampleFractionPara, narrativeName = "fraction of sampling", description = "the two fractions that the function sample in the taxa") Value<Double[]> sampleFraction){

        System.out.println("RandomSample constructor!!!");
        if (tree == null) throw new IllegalArgumentException("The original tree cannot be null");
        setParam(treeParamName, tree);
        setParam(taxaParamName, taxaName);
        setParam(sampleFractionPara, sampleFraction);
        this.sampleFraction = sampleFraction;
        this.tree = tree;
        this.taxaName = taxaName;
        this.random = RandomUtils.getRandom();
    }

    @Override
    protected void constructDistribution(RandomGenerator random) {
    }

    @GeneratorInfo(name = "RandomSample", description = "Generate the randomly sampled tree with given two sample fractions" +
            "and two clade taxa names within the given tree.")
    @Override
    public RandomVariable<TimeTree> sample() {
        Value<TimeTree> tree = getParams().get(treeParamName);
        Value<String[][]> taxaName = getParams().get(taxaParamName);
        Value<Double[]> sampleFraction = getParams().get(sampleFractionPara);
        TimeTree originalTree = tree.value();

        System.out.println("RandomSample sample()");

        // obtain tumour and normal taxa names
        String[] tumourName = taxaName.value()[0];
        String[] normalName = taxaName.value()[1];

        // get the leaf names
        String[] tumourLeafList = getLeafList(originalTree, tumourName);
        String[] normalLeafList = getLeafList(originalTree, normalName);

        // obtain tumour and normal sample fractions
        double tumourFraction = sampleFraction.value()[0];
        double normalFraction = sampleFraction.value()[1];

        // randomly pick the taxa names
        String[] sampledTumour = getSampleResult(tumourFraction, tumourLeafList);
        String[] sampledNormal = getSampleResult(normalFraction, normalLeafList);

        // merge the name arrays
        String[] sampledNames = combineTwoArray(sampledTumour, sampledNormal);

        // make a deep copy of original tree
        TimeTree newTree = new TimeTree(originalTree);

        // remove unsampled taxa and reset parents
        getSampledTree(newTree, sampledNames);

        return new RandomVariable<>(null, newTree, this);
    }

    public static String[] getLeafList(TimeTree originalTree, String[] tumourName) {
        List<String> tumourLeafList = new ArrayList<>();
        TimeTreeNode[] allNodes = originalTree.getNodes().toArray(new TimeTreeNode[0]);
        // check which is a leaf
        for (int i = 0; i<allNodes.length; i++){
            // if the node is tumour node, and the node is a leaf, add to the list
            if (Arrays.asList(tumourName).contains(allNodes[i].getId()) && allNodes[i].isLeaf()){
                tumourLeafList.add(allNodes[i].getId());
            }
        }
        return tumourLeafList.toArray(new String[0]);
    }

    public static void getSampledTree(TimeTree newTree, String[] sampledNames) {
        List<String> sampledNamesList = Arrays.asList(sampledNames);
        TimeTreeNode rootNode = newTree.getRoot();
        List<TimeTreeNode> leafNodes = rootNode.getAllLeafNodes();
        for (TimeTreeNode node: leafNodes) {
            TimeTreeNode parentNode = node.getParent();
            if (!sampledNamesList.contains(node.getId())){
                String nodeName = node.getId();
                TimeTreeNode child1 = parentNode.getLeft();
                TimeTreeNode child2 = parentNode.getRight();
                parentNode.removeChild(node);
                if (parentNode.getChildCount() == 1 && !parentNode.isRoot()){
                    if (Objects.equals(child1.getId(), nodeName)){
                        TimeTreeNode grandparentNode = parentNode.getParent();
                        child2.setParent(grandparentNode);
                        grandparentNode.removeChild(parentNode);
                        grandparentNode.addChild(child2);
                    } else {
                        TimeTreeNode grandparentNode = parentNode.getParent();
                        child1.setParent(grandparentNode);
                        grandparentNode.removeChild(parentNode);
                        grandparentNode.addChild(child1);
                    }
                } else if (parentNode.getChildCount() == 1 && parentNode.isRoot()) {
                    if (Objects.equals(child1.getId(), nodeName)){
                        newTree.setRoot(child2, true);
                    } else {
                        newTree.setRoot(child1, true);
                    }
                }
            }
        }

        newTree.setRoot(newTree.getRoot(), true);

        String newick = newTree.toNewick(true);
        System.out.println("newTree newick: " + newick);
        // set the indices for all nodes
        TimeTreeNode[] allNodes = newTree.getNodes().toArray(new TimeTreeNode[0]);
        for (int i = 0; i<allNodes.length;i++){
            allNodes[i].setIndex(i);
        }
    }

    public static String[] combineTwoArray(String[] array1, String[] array2) {
        String[] sampledNames = new String[array1.length + array2.length];

        // do copying
        System.arraycopy(array1, 0, sampledNames, 0, array1.length);
        System.arraycopy(array2, 0, sampledNames, array1.length, array2.length);

        return sampledNames;
    }

    public String[] getSampleResult(double fraction, String[] name) {
        // calculate the num of taxa names to get
        int sampleNumber = (int)Math.round(fraction * name.length);
        // create a list to write result in
        List<String> sampleResult = new ArrayList<>();
        while (sampleResult.size() < sampleNumber){
            int index;
            do{
                index = random.nextInt(name.length); // get a random index
            } while (sampleResult.contains(name[index])); // check the index is a new one

            sampleResult.add(name[index]); // add the name to the result list
        }

        return sampleResult.toArray(new String[0]);
    }

    @Override
    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        if (sampleFraction != null) map.put(sampleFractionPara, sampleFraction);
        if (tree != null) map.put(treeParamName, tree);
        if (taxaName != null) map.put(taxaParamName, taxaName);
        return map;
    }
}
