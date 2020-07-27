package lphy.beast;

import beast.core.*;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.Parameter;
import beast.core.parameter.RealParameter;
import beast.core.util.CompoundDistribution;
import beast.evolution.operators.*;
import beast.evolution.operators.Uniform;
import beast.evolution.substitutionmodel.Frequencies;
import beast.evolution.tree.Tree;
import beast.math.distributions.ParametricDistribution;
import beast.math.distributions.Prior;
import beast.util.XMLProducer;
import lphy.beast.tobeast.AlignmentToBEAST;
import lphy.beast.tobeast.TimeTreeToBEAST;
import lphy.core.LPhyParser;
import lphy.core.distributions.*;
import lphy.graphicalModel.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BEASTContext {

    List<StateNode> state = new ArrayList<>();

    Set<BEASTInterface> elements = new HashSet<>();

    // a map of graphical model nodes to equivalent BEASTInterface objects
    private Map<GraphicalModelNode<?>, BEASTInterface> beastObjects = new HashMap<>();

    // a map of BEASTInterface to graphical model nodes that they represent
    Map<BEASTInterface, GraphicalModelNode<?>> BEASTToLPHYMap = new HashMap<>();

    Map<Class, ValueToBEAST> valueToBEASTMap = new HashMap<>();

    private List<Operator> extraOperators = new ArrayList<>();

    LPhyParser parser;

    public BEASTContext(LPhyParser phyParser) {
        parser = phyParser;

        valueToBEASTMap.put(lphy.evolution.alignment.Alignment.class, new AlignmentToBEAST());
        valueToBEASTMap.put(lphy.evolution.tree.TimeTree.class, new TimeTreeToBEAST());
    }

    public BEASTInterface getBEASTObject(GraphicalModelNode<?> node) {
        return beastObjects.get(node);
    }

    public void addBEASTObject(BEASTInterface newBEASTObject) {
        elements.add(newBEASTObject);
    }

    public void removeBEASTObject(BEASTInterface beastObject) {
        elements.remove(beastObject);
        state.remove(beastObject);
        BEASTToLPHYMap.remove(beastObject);

        GraphicalModelNode matchingKey = null;
        for (GraphicalModelNode key : beastObjects.keySet()) {
            if (getBEASTObject(key) == beastObject) {
                matchingKey = key;
                break;
            }
        }
        if (matchingKey != null) beastObjects.remove(matchingKey);
    }

    public static RealParameter createRealParameter(Double[] value) {
        return new RealParameter(value);
    }

    public static RealParameter createRealParameter(double value) {
        RealParameter parameter = new RealParameter();
        parameter.setInputValue("value", value);
        parameter.initAndValidate();
        return parameter;
    }

    /**
     * Clone the current model to BEAST2
     */
    public void createBEASTObjects() {

        Set<Value<?>> sinks = parser.getSinks();

        for (Value<?> value : sinks) {
            createBEASTObjects(value);
        }
    }

    private void createBEASTObjects(Value<?> value) {

        valueToBEAST(value);

        Generator<?> generator = value.getGenerator();

        if (generator != null) {

            for (Object inputObject : generator.getParams().values()) {
                Value<?> input = (Value<?>) inputObject;
                createBEASTObjects(input);
            }

            generatorToBEAST(value, generator);
        }
    }

    /**
     * This is called after cloneValue has been called on both the generated value and the input values.
     * Side-effect of this method is to create a clone object of the generator and put it in the cloneMap of this BEAST2Context.
     *
     * @param value
     * @param generator
     */
    private void generatorToBEAST(Value value, Generator generator) {
        BEASTInterface beastGenerator = generator.toBEAST(beastObjects.get(value), this);

        if (beastGenerator == null) {
            throw new RuntimeException("Generator " + generator + " not handled in cloneGenerator()");
        } else {
            addToContext(generator, beastGenerator);
        }
    }

    private BEASTInterface valueToBEAST(Value<?> val) {

        BEASTInterface beastValue = null;

        ValueToBEAST toBEAST = valueToBEASTMap.get(val.value().getClass());

        if (toBEAST != null) {
            beastValue = toBEAST.valueToBEAST(val, beastObjects);
        } else if (val.value() instanceof Double || val.value() instanceof Double[] || val.value() instanceof Double[][]) {
            beastValue = createBEASTRealParameter(val);
        } else if (val.value() instanceof Integer || val.value() instanceof Integer[]) {
            beastValue = createBEASTIntegerParameter(val);
        }
        if (beastValue == null) {
            throw new RuntimeException("Unhandled value in valueToBEAST(): " + val);
        }

        addToContext(val, beastValue);
        return beastValue;
    }

    private void addToContext(GraphicalModelNode node, BEASTInterface beastInterface) {
        beastObjects.put(node, beastInterface);
        BEASTToLPHYMap.put(beastInterface, node);
        elements.add(beastInterface);

        if (node instanceof RandomVariable) {
            RandomVariable<?> var = (RandomVariable<?>) node;

            if (var.getOutputs().size() > 0 && !state.contains(beastInterface)) {
                state.add((StateNode) beastInterface);
            }
        }
    }

    public static Frequencies createBEASTFrequencies(RealParameter freqParameter) {
        Frequencies frequencies = new Frequencies();
        frequencies.setInputValue("frequencies", freqParameter);
        frequencies.initAndValidate();
        return frequencies;
    }

    public static Prior createPrior(ParametricDistribution distr, Parameter parameter) {
        Prior prior = new Prior();
        prior.setInputValue("distr", distr);
        prior.setInputValue("x", parameter);
        prior.initAndValidate();
        return prior;
    }

    public RealParameter createBEASTRealParameter(Value value) {

        RealParameter parameter = new RealParameter();
        if (value.value() instanceof Double) {
            parameter.setInputValue("value", Collections.singletonList(value.value()));
            parameter.setInputValue("dimension", 1);

            // check domain
            if (value.getGenerator() instanceof LogNormal || value.getGenerator() instanceof Exp) {
                parameter.setInputValue("lower", 0.0);
            }
            parameter.initAndValidate();
        } else if (value.value() instanceof Double[]) {
            List<Double> values = Arrays.asList((Double[]) value.value());
            parameter.setInputValue("value", values);
            parameter.setInputValue("dimension", values.size());

            // check domain
            if (value.getGenerator() instanceof Dirichlet) {
                parameter.setInputValue("upper", 1.0);
                parameter.setInputValue("lower", 0.0);
            } else if (value.getGenerator() instanceof LogNormal || value.getGenerator() instanceof Exp) {
                parameter.setInputValue("lower", 0.0);
            }

            parameter.initAndValidate();
        } else if (value.value() instanceof Double[][]) {

            Double[][] val = (Double[][]) value.value();

            List<Double> values = new ArrayList<>(val.length * val[0].length);
            for (int i = 0; i < val.length; i++) {
                for (int j = 0; j < val[0].length; j++) {
                    values.add(val[i][j]);
                }
            }
            parameter.setInputValue("value", values);
            parameter.setInputValue("dimension", values.size());
            parameter.setInputValue("minordimension", val[0].length); // TODO check this!
            parameter.initAndValidate();
        } else {
            throw new IllegalArgumentException();
        }
        if (!value.isAnonymous()) parameter.setID(value.getCanonicalId());
        elements.add(parameter);

        return parameter;
    }

    private IntegerParameter createBEASTIntegerParameter(Value<?> value) {
        IntegerParameter parameter = new IntegerParameter();
        if (value.value() instanceof Integer) {
            parameter.setInputValue("value", Collections.singletonList(value.value()));
            parameter.setInputValue("dimension", 1);
            parameter.initAndValidate();
        } else if (value.value() instanceof Integer[]) {
            List<Integer> values = Arrays.asList((Integer[]) value.value());
            parameter.setInputValue("value", values);
            parameter.setInputValue("dimension", values.size());
            parameter.initAndValidate();
        } else {
            throw new IllegalArgumentException();
        }
        if (!value.isAnonymous()) parameter.setID(value.getCanonicalId());
        elements.add(parameter);

        return parameter;
    }

    public List<Operator> createOperators() {

        List<Operator> operators = new ArrayList<>();

        for (StateNode stateNode : state) {
            System.out.println("State node" + stateNode);
            if (stateNode instanceof RealParameter) {
                operators.add(createBEASTOperator((RealParameter) stateNode));
            } else if (stateNode instanceof Tree) {
                operators.add(createTreeScaleOperator((Tree) stateNode));
                operators.add(createExchangeOperator((Tree) stateNode, true));
                operators.add(createExchangeOperator((Tree) stateNode, false));
                operators.add(createSubtreeSlideOperator((Tree) stateNode));
                operators.add(createTreeUniformOperator((Tree) stateNode));
            }
        }

        operators.addAll(extraOperators);

        return operators;
    }

    private List<Logger> createLoggers(int logEvery, String fileName) {
        List<Logger> loggers = new ArrayList<>();

        loggers.add(createScreenLogger(logEvery));
        loggers.add(createLogger(logEvery, fileName + ".log"));
        loggers.add(createTreeLogger(logEvery, fileName + ".trees"));

        return loggers;
    }

    private Logger createLogger(int logEvery, String fileName) {

        List<StateNode> nonTrees = state.stream()
                .filter(stateNode -> !(stateNode instanceof Tree))
                .collect(Collectors.toList());

        Logger logger = new Logger();
        logger.setInputValue("logEvery", logEvery);
        logger.setInputValue("log", nonTrees);
        if (fileName != null) logger.setInputValue("fileName", fileName);
        logger.initAndValidate();
        elements.add(logger);
        return logger;
    }

    private Logger createTreeLogger(int logEvery, String fileName) {

        List<Tree> trees = state.stream()
                .filter(stateNode -> stateNode instanceof Tree)
                .map(stateNode -> (Tree) stateNode)
                .collect(Collectors.toList());

        Logger logger = new Logger();
        logger.setInputValue("logEvery", logEvery);
        logger.setInputValue("log", trees);
        if (fileName != null) logger.setInputValue("fileName", fileName);
        logger.initAndValidate();
        elements.add(logger);
        return logger;
    }

    private Logger createScreenLogger(int logEvery) {
        return createLogger(logEvery, null);
    }

    public static double getOperatorWeight(int size) {
        return Math.pow(size, 0.7);
    }

    private Operator createTreeScaleOperator(Tree tree) {
        ScaleOperator operator = new ScaleOperator();
        operator.setInputValue("tree", tree);
        operator.setInputValue("weight", getOperatorWeight(tree.getInternalNodeCount()));
        operator.initAndValidate();
        elements.add(operator);

        return operator;
    }

    private Operator createTreeUniformOperator(Tree tree) {
        Uniform uniform = new Uniform();
        uniform.setInputValue("tree", tree);
        uniform.setInputValue("weight", getOperatorWeight(tree.getInternalNodeCount()));
        uniform.initAndValidate();
        elements.add(uniform);

        return uniform;
    }

    private Operator createSubtreeSlideOperator(Tree tree) {
        SubtreeSlide subtreeSlide = new SubtreeSlide();
        subtreeSlide.setInputValue("tree", tree);
        subtreeSlide.setInputValue("weight", getOperatorWeight(tree.getInternalNodeCount()));
        subtreeSlide.setInputValue("size", tree.getRoot().getHeight() / 10.0);
        subtreeSlide.initAndValidate();
        elements.add(subtreeSlide);

        return subtreeSlide;
    }

    private Operator createExchangeOperator(Tree tree, boolean isNarrow) {
        Exchange exchange = new Exchange();
        exchange.setInputValue("tree", tree);
        exchange.setInputValue("weight", getOperatorWeight(tree.getInternalNodeCount()));
        exchange.setInputValue("isNarrow", isNarrow);
        exchange.initAndValidate();
        elements.add(exchange);

        return exchange;
    }

    private Operator createBEASTOperator(RealParameter parameter) {
        RandomVariable<?> variable = (RandomVariable<?>) BEASTToLPHYMap.get(parameter);

        Operator operator;
        if (variable.getGenerativeDistribution() instanceof Dirichlet) {
            operator = new DeltaExchangeOperator();
            operator.setInputValue("parameter", parameter);
            operator.setInputValue("weight", getOperatorWeight(parameter.getDimension()-1));
        } else {
            operator = new ScaleOperator();
            operator.setInputValue("parameter", parameter);
            operator.setInputValue("weight", getOperatorWeight(parameter.getDimension()));
            operator.setInputValue("scaleFactor", 0.75);
        }
        operator.initAndValidate();
        elements.add(operator);

        return operator;
    }

    private CompoundDistribution createBEASTPosterior() {

        createBEASTObjects();

        List<Distribution> priorList = new ArrayList<>();

        List<Distribution> likelihoodList = new ArrayList<>();

        for (Map.Entry<GraphicalModelNode<?>, BEASTInterface> entry : beastObjects.entrySet()) {
            if (entry.getValue() instanceof Distribution) {
                GenerativeDistribution g = (GenerativeDistribution) entry.getKey();

                if (generatorOfSink(g)) {
                    likelihoodList.add((Distribution) entry.getValue());
                } else {
                    priorList.add((Distribution) entry.getValue());
                }
            }
        }

        for (BEASTInterface beastInterface : elements) {
            if (beastInterface instanceof Distribution && !likelihoodList.contains(beastInterface) && !priorList.contains(beastInterface)) {
                priorList.add((Distribution) beastInterface);
            }
        }

        System.out.println("Found " + likelihoodList.size() + " likelihoods.");
        System.out.println("Found " + priorList.size() + " priors.");

        CompoundDistribution priors = new CompoundDistribution();
        priors.setInputValue("distribution", priorList);
        priors.initAndValidate();
        priors.setID("prior");
        elements.add(priors);

        CompoundDistribution likelihoods = new CompoundDistribution();
        likelihoods.setInputValue("distribution", likelihoodList);
        likelihoods.initAndValidate();
        likelihoods.setID("likelihood");
        elements.add(likelihoods);

        List<Distribution> posteriorList = new ArrayList<>();
        posteriorList.add(priors);
        posteriorList.add(likelihoods);

        CompoundDistribution posterior = new CompoundDistribution();
        posterior.setInputValue("distribution", posteriorList);
        posterior.initAndValidate();
        posterior.setID("posterior");
        elements.add(posterior);

        return posterior;
    }

    private boolean generatorOfSink(GenerativeDistribution g) {
        for (Value<?> var : parser.getSinks()) {
            if (var.getGenerator() == g) {
                return true;
            }
        }
        return false;
    }

    public MCMC createMCMC(long chainLength, int logEvery, String fileName) {

        CompoundDistribution posterior = createBEASTPosterior();

        MCMC mcmc = new MCMC();
        mcmc.setInputValue("distribution", posterior);
        mcmc.setInputValue("chainLength", chainLength);

        mcmc.setInputValue("operator", createOperators());
        mcmc.setInputValue("logger", createLoggers(logEvery, fileName));

        State state = new State();
        state.setInputValue("stateNode", this.state);
        state.initAndValidate();
        elements.add(state);

        // TODO make sure the stateNode list is being correctly populated
        mcmc.setInputValue("state", state);

        mcmc.initAndValidate();
        return mcmc;
    }

    public void clear() {
        state.clear();
        elements.clear();
        beastObjects.clear();
        extraOperators.clear();
    }

    public void runBEAST(String fileNameStem) {

        MCMC mcmc = createMCMC(1000000, 1000, fileNameStem);

        try {
            mcmc.run();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String toBEASTXML(String fileNameStem) {

        MCMC mcmc = createMCMC(1000000, 1000, fileNameStem);

        String xml = new XMLProducer().toXML(mcmc, elements);

        return xml;
    }

    public void addExtraOperator(Operator operator) {
        extraOperators.add(operator);
    }
}