package lphy.graphicalModel;

/**
 * The group of {@link GenerativeDistribution} or {@link Func}
 * play the same or similar roles in the Bayesian phylogenetic frame.
 * @author Walter Xie
 */
public enum GeneratorCategory {
    ALL("All","All categories"),
    SEQU_TYPE("Sequence type","Data type of sequences, e.g. nucleotides, amino acid, binary"),
    TAXA_ALIGNMENT("Taxa & Alignment","Taxa and alignment"),
    RATE_MATRIX("Rate matrix","Instantaneous rate matrix"),
    COAL_TREE("Coalescent tree","Coalescent tree prior"),
    BD_TREE("Birth-death tree","Birth-death tree prior"),
    PROB_DIST("Probability distribution","Prior probability distribution"),
    STOCHASTIC_PROCESS("Stochastic process","Such as continuous-time Markov chain (CTMC) and Brownian motion"),
    MODEL_AVE_SEL("\"True\" model","Model averaging or model selection"),
    NONE("None","Unknown category"); // last element is only for GUI

    private String name;
    private String description;

    GeneratorCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }
}
