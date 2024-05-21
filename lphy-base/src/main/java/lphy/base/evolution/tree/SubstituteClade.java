package lphy.base.evolution.tree;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class SubstituteClade extends DeterministicFunction<TimeTree> {
    Value<TimeTree> baseTree;
    Value<TimeTree> cladeTree;
    Value<Double> time;
    Value<TimeTreeNode> node;
    Value<String> nodeLabel;
    public static final String baseTreeName = "baseTree";
    public static final String cladeTreeName = "cladeTree";
    public static final String nodeName = "node";
    public static final String mutationHappenTimeName = "time";
    public static final String nodeLabelName = "nodeLabel";

    public SubstituteClade(@ParameterInfo(name = baseTreeName, description = "the tree that we are going to add another tree onto.") Value<TimeTree> baseTree,
                           @ParameterInfo(name = cladeTreeName, description = "the tree that we are going to add it on the base tree") Value<TimeTree> cladeTree,
                           @ParameterInfo(name = nodeName, description = "the node with the branch that the branch tree would be add on to.") Value<TimeTreeNode> node,
                           @ParameterInfo(name = mutationHappenTimeName, description = "the mutation happen time that the branch tree would be add onto the base tree") Value<Double> time,
                           @ParameterInfo(name = nodeLabelName, description = "the name of added branch node.") Value<String> nodeLabel) {
        if (baseTree == null) throw new IllegalArgumentException("The base tree cannot be null!");
        if (cladeTree == null) throw new IllegalArgumentException("The clade tree cannot be null!");
        if (time == null) throw new IllegalArgumentException("The happening time cannot be null!");
        if (node == null) throw new IllegalArgumentException("Please specify the node!");
        if (nodeLabel == null) throw new IllegalArgumentException("Please label the root of cladeTree!");
        setParam(baseTreeName, baseTree);
        setParam(cladeTreeName, cladeTree);
        setParam(nodeName, node);
        setParam(mutationHappenTimeName, time);
        setParam(nodeLabelName,nodeLabel);
        this.baseTree = baseTree;
        this.cladeTree = cladeTree;
        this.node = node;
        this.time = time;
        this.nodeLabel = nodeLabel;
    }

    @GeneratorInfo(name = "substituteClade", description = "Substitute a clade in a tree with a given node and time, as well as the label of the clade root node. The original child clade would be replaced by the give tree." )
    @Override
    public Value<TimeTree> apply() {
        // get parameters
        TimeTree baseTree = getBaseTree().value();
        TimeTree cladeTree = getCladeTree().value();
        TimeTreeNode node = getNode().value();
        Double time = getMutationHappenTimeName().value();
        String nodeLabel = getNodeLabel().value();

        // make deep copy of trees
        TimeTree newTree = new TimeTree(baseTree);
        TimeTree newClade = new TimeTree(cladeTree);

        // give the nodes in clade tree new names
        for (TimeTreeNode leaf : newClade.getRoot().getAllLeafNodes()){
            leaf.setId("clade_" + leaf.getId());
        }

        // get nodes in the copy tree
        int parentNodeIndex = node.getParent().getIndex();
        TimeTreeNode newParentNode = newTree.getNodeByIndex(parentNodeIndex);
        int childNodeIndex = node.getIndex();
        TimeTreeNode newChildNode = newTree.getNodeByIndex(childNodeIndex);

        // remove the original node
        newParentNode.removeChild(newChildNode);

        // add branch tree as clade
        TimeTreeNode cladeRoot = newClade.getRoot();
        newParentNode.addChild(cladeRoot);
        cladeRoot.setParent(newParentNode);

        Double branchDuration = node.getBranchDuration();

        // how long the branch should be from parent node to the time
        Double fraction = (time - node.getParent().getAge())/(node.getAge() - node.getParent().getAge()); // kept branch duration in full branch duration
        double keptBranchDuration = fraction * branchDuration;

        // set age and label for clade root
        cladeRoot.setAge(keptBranchDuration + cladeRoot.getAge());
        cladeRoot.setId(nodeLabel);

        // reset the index in the new tree
        List<TimeTreeNode> allNodes = newTree.getNodes();
        for(int i = 0; i < allNodes.size(); i++){
            allNodes.get(i).setIndex(i);
        }

        return new Value<>(newTree, this);
    }

    @Override
    public Map<String, Value> getParams() {
        SortedMap<String, Value> map = new TreeMap<>();
        if (baseTree != null) map.put(baseTreeName, baseTree);
        if (cladeTree != null) map.put(cladeTreeName, cladeTree);
        if (node != null) map.put(nodeName, node);
        if (time != null) map.put(mutationHappenTimeName, time);
        if (nodeLabelName != null) map.put(nodeLabelName, nodeLabel);
        return map;
    }
    public Value<TimeTree> getBaseTree() {
        return getParams().get(baseTreeName);
    }
    public Value<TimeTree> getCladeTree() {
        return getParams().get(cladeTreeName);
    }
    public Value<TimeTreeNode> getNode(){
        return getParams().get(nodeName);
    }
    public Value<Double> getMutationHappenTimeName() {
        return getParams().get(mutationHappenTimeName);
    }
    public Value<String> getNodeLabel(){
        return getParams().get(nodeLabelName);
    }
}