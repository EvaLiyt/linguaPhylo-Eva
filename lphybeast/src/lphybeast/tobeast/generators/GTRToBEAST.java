package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;
import lphy.evolution.substitutionmodel.GTR;
import lphy.graphicalModel.Value;

public class GTRToBEAST implements GeneratorToBEAST<GTR> {
    @Override
    public BEASTInterface generatorToBEAST(GTR gtr, BEASTInterface value, BEASTContext context) {

        substmodels.nucleotide.GTR beastGTR = new substmodels.nucleotide.GTR();

        Value<Double[]> rates = gtr.getRates();

        beastGTR.setInputValue("rates", context.getBEASTObject(rates));
        beastGTR.setInputValue("frequencies", BEASTContext.createBEASTFrequencies((RealParameter) context.getBEASTObject(gtr.getFreq())));
        beastGTR.initAndValidate();
        return beastGTR;
    }

    @Override
    public Class<GTR> getGeneratorClass() { return GTR.class; }
}