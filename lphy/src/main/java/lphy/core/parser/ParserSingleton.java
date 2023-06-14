package lphy.core.parser;

import lphy.core.model.Value;

import java.util.Arrays;

public class ParserSingleton {
//    private static LPhyMetaParser PARSER = getInstance();
//    public synchronized static LPhyMetaParser getInstance() {
//        if (PARSER == null) {
//            PARSER = new REPL();
//        }
//        return PARSER;
//    }

    private static LPhyListenerImpl dataParser = new LPhyListenerImpl(new REPL(), LPhyMetaParser.Context.data);
    private static LPhyListenerImpl modelParser = new LPhyListenerImpl(new REPL(), LPhyMetaParser.Context.model);

    public static Object parse(String cmd) {
        return parseModelBlock(cmd);
    }

    public static Object parseModelBlock(String cmd) {
        modelParser.clear();
        return modelParser.parse(cmd);
    }

    public static Object parseDataBlock(String cmd) {
        dataParser.clear();
        return dataParser.parse(cmd);
    }

    public static void main(String[] args) {

        Object res = parse("prod = [[1,2,3],[3,2,1]] * [[1,2,3],[3,2,1]];");

        Integer[][]  rV = ((Value<Integer[][]>) res).value();

        for (Integer[] row : rV) {
            System.out.println(Arrays.toString(row));
        }
    }

}
