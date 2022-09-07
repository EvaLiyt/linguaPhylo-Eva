package lphystudio.app.narrative;

import lphy.core.LPhyParser;
import lphy.parser.LPhyParserAction;
import lphy.util.Symbols;
import lphystudio.core.codecolorizer.ColorizerStyles;
import lphystudio.core.codecolorizer.DataModelCodeColorizer;
import lphystudio.core.codecolorizer.TextElement;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

import javax.swing.*;
import javax.swing.text.Style;
import java.util.ArrayList;
import java.util.List;

public class DataModelToLaTeX extends DataModelCodeColorizer {

    // CURRENT MODEL STATE

    static String randomVarColor = "green";
    static String constantColor = "magenta";
    static String keywordColor = "black";
    static String argumentNameColor = "gray";
    static String functionColor = "magenta!80!black";
    static String distributionColor = "blue";

    List<String> elements = new ArrayList<>();

    LaTeXNarrative narrative = new LaTeXNarrative();

    public DataModelToLaTeX(LPhyParser parser, JTextPane pane) {
        super(parser, pane);
    }

    public class DataModelASTVisitor extends DataModelCodeColorizer.DataModelASTVisitor {

        public DataModelASTVisitor() {
        }

        public void addTextElement(TextElement element) {

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < element.getSize(); i++) {
                String text = element.getText(i);
                Style style = element.getStyle(i);

                switch (style.getName()) {
                    case ColorizerStyles.function:
                        builder.append("\\textcolor{");
                        builder.append(functionColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.distribution:
                        builder.append("\\textcolor{");
                        builder.append(distributionColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.argumentName:
                        builder.append("\\textcolor{");
                        builder.append(argumentNameColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.constant:
                        builder.append("\\textcolor{");
                        builder.append(constantColor);
                        builder.append("}{");
                        break;
                    case ColorizerStyles.randomVariable:
                        builder.append("\\textcolor{");
                        builder.append(randomVarColor);
                        builder.append("}{");
                }

                text = text.replace("{", "\\{");
                text = text.replace("}", "\\}");

                text = Symbols.getCanonical(text, "\\(\\", "\\)");

                builder.append(narrative.code(text));
                switch (style.getName()) {
                    case ColorizerStyles.function:
                    case ColorizerStyles.distribution:
                    case ColorizerStyles.argumentName:
                    case ColorizerStyles.constant:
                    case ColorizerStyles.randomVariable:
                        builder.append("}");
                }


            }
            elements.add(builder.toString());
        }
    }

    public String getLatex() {
        StringBuilder latex = new StringBuilder();
        latex.append("\\begin{alltt}\n");
        for (String element : elements) {
            latex.append(element);
        }
        latex.append("\\end{alltt}\n");
        return latex.toString();
    }

    public Object parse(String CASentence) {

        System.out.println("Parsing " + CASentence);

        // Traverse parse tree
        AbstractParseTreeVisitor visitor = new DataModelASTVisitor();

        // containing either or both a data and model block;
        return LPhyParserAction.parse(CASentence, visitor, true);
    }
}
