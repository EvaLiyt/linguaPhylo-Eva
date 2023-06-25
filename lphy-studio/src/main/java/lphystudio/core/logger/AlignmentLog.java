package lphystudio.core.logger;

import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.logger.AlignmentFileLogger;
import lphy.base.parser.nexus.NexusWriter;
import lphy.core.logger.LoggerUtils;
import lphy.core.logger.RandomValueLogger;
import lphy.core.model.Value;
import lphy.core.parser.LPhyMetaParser;
import lphy.core.system.UserDir;

import javax.swing.*;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Write each alignment to a file during sampling.
 * The viewer is {@link lphystudio.app.graphicalmodelpanel.AlignmentLogPanel}.
 * This is a duplicated version of {@link AlignmentFileLogger},
 * but contains GUI code.
 * @author Walter Xie
 */
public class AlignmentLog extends JTextArea implements RandomValueLogger {

    final LPhyMetaParser parser;

    boolean logAlignment = false;

    public AlignmentLog(LPhyMetaParser parser) {
        this.parser = parser;
    }

    public void clear() {
        setText("");
        setEditable(false);
    }

    public void setLogAlignment(boolean bool) {
        this.logAlignment = bool;
    }

    @Override
    public void start(List<Value<?>> randomValues) {
        //TODO
    }

    public void log(int rep, List<Value<?>> randomValues) {
        List<Value<SimpleAlignment>> alignmentVariables = getAlignmentValues(randomValues);

        if (rep == 0) {
            setText("sample");
            for (Value<SimpleAlignment> al : alignmentVariables) {
                String colNm = parser.isClampedVariable(al) ? al.getId() + "-clamped" : al.getId();
                append("\t" + colNm);
            }
            append("\n");
        }

        append(rep+"");
        for (Value<SimpleAlignment> al : alignmentVariables) {
            append("\t" + al.value().toString());
            if (logAlignment) {
                logAlignment(al, rep);
            }
        }
        append("\n");
    }

    @Override
    public void stop() {

    }

    public String getDescription() {
        return getLoggerName() + " writes the alignments generated from simulations into GUI.";
    }

    private List<Value<SimpleAlignment>> getAlignmentValues(List<Value<?>> variables) {
        List<Value<SimpleAlignment>> values = new ArrayList<>();
        for (Value<?> v : variables) {
            if (v.value() instanceof SimpleAlignment)
                values.add((Value<SimpleAlignment>) v);
            else if (v.value() instanceof SimpleAlignment[] simpleAlignments) {
                // VectorizedRandomVariable value is SimpleAlignment[]
                String id = v.getCanonicalId();
                for (int i = 0; i < simpleAlignments.length; i++) {
                    // new id
                    String newID = parser.isClampedVariable(v) ? id + "-" + i + "-clamped" : id + "-" + i;
                    values.add(new Value<>(newID, simpleAlignments[i]));
                }
            }
        }
        values.sort(Comparator.comparing(Value::getCanonicalId));
        return values;
    }

    private void logAlignment(Value<SimpleAlignment> alignment, int rep) {
        Path dir = UserDir.getAlignmentDir();
        String fileName = alignment.getCanonicalId() + "_" + rep + ".nexus";
        PrintStream stream = null;
        try {
            File file = Paths.get(dir.toString(), fileName).toFile();
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
                // throw new IllegalArgumentException("Directory " + file.getParentFile() + " does not exist !");
            }
            stream = new PrintStream(file);
            // no tree
            NexusWriter.write(alignment.value(), new LinkedList<>(), stream);

            LoggerUtils.log.info("Sample " + rep + " writes alignment " + alignment.getCanonicalId() + " to " + file);
        } catch (Exception e) {
            LoggerUtils.logStackTrace(e);
            e.printStackTrace();
        } finally {
            NexusWriter.close(stream);
        }
    }

}
