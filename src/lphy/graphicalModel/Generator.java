package lphy.graphicalModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A generator generates values, either deterministically (DeterministicFunction) or stochastically (GenerativeDistribution).
 * A generator also takes named Parameters which are themselves Values, which may have been generated by a Generator.
 */
public interface Generator<T> extends GraphicalModelNode {

    String getName();

    /**
     * @return
     */
    Value<T> generate();

    default String getParamName(int paramIndex, int constructorIndex) {
        return getParameterInfo(constructorIndex).get(paramIndex).name();
    }

    default String getParamName(int paramIndex) {
        return getParamName(paramIndex, 0);
    }

    static List<ParameterInfo> getParameterInfo(Class<?> c, int constructorIndex) {
        return getParameterInfo(c.getConstructors()[constructorIndex]);
    }

    default List<ParameterInfo> getParameterInfo(int constructorIndex) {
        return getParameterInfo(this.getClass(), constructorIndex);
    }

    static List<ParameterInfo> getParameterInfo(Constructor constructor) {
        ArrayList<ParameterInfo> pInfo = new ArrayList<>();

        Annotation[][] annotations = constructor.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation[] annotations1 = annotations[i];
            for (Annotation annotation : annotations1) {
                if (annotation instanceof ParameterInfo) {
                    pInfo.add((ParameterInfo) annotation);
                }
            }
        }

        return pInfo;
    }

    static List<ParameterInfo> getAllParameterInfo(Class c) {
        ArrayList<ParameterInfo> pInfo = new ArrayList<>();
        for (Constructor constructor : c.getConstructors()) {
            pInfo.addAll(getParameterInfo(constructor));
        }
        return pInfo;
    }

    static String getSignature(Class<?> aClass) {

        List<ParameterInfo> pInfo = Generator.getParameterInfo(aClass, 0);

        StringBuilder builder = new StringBuilder();
        builder.append(getGeneratorName(aClass));
        builder.append("(");
        if (pInfo.size() > 0) {
            builder.append(pInfo.get(0).name());
            for (int i = 1; i < pInfo.size(); i++) {
                builder.append(", ");
                builder.append(pInfo.get(i).name());
            }
        }
        builder.append(")");
        return builder.toString();
    }

    static String getGeneratorName(Class<?> c) {
        GeneratorInfo ginfo = getGeneratorInfo(c);
        if (ginfo != null) return ginfo.name();
        return c.getSimpleName();
    }

    static GeneratorInfo getGeneratorInfo(Class<?> c) {

        Method[] methods = c.getMethods();
        for (Method method : methods) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation instanceof GeneratorInfo) {
                    return (GeneratorInfo) annotation;
                }
            }
        }
        return null;
    }

    @Override
    default List<GraphicalModelNode> getInputs() {
        return new ArrayList<>(getParams().values());
    }

    Map<String, Value> getParams();

    void setParam(String paramName, Value<?> value);

    default void setInput(String paramName, Value<?> value) {
        setParam(paramName, value);
        value.addOutput(this);
    }

    default String getParamName(Value value) {
        Map<String, Value> params = getParams();
        for (String key : params.keySet()) {
            if (params.get(key) == value) return key;
        }
        return null;
    }

    String codeString();

    static String getArgumentCodeString(Map.Entry<String, Value> entry) {
        String prefix = "";
        if (!Utils.isInteger(entry.getKey())) {
            prefix = entry.getKey() + "=";
        }
        if (entry.getValue().isAnonymous()) return prefix + entry.getValue().codeString();
        return prefix + entry.getValue().getId();
    }

    static String getArgumentValue(Map.Entry<String, Value> entry) {
        if (entry.getValue().isAnonymous()) return entry.getValue().codeString();
        return entry.getValue().getId();
    }

    /**
     * @return true if any of the parameters are random variables,
     * or are themselves that result of a function with random parameters as arguments.
     */
    default boolean hasRandomParameters() {
        for (Value<?> v : getParams().values()) {
            if (v.isRandom()) return true;
        }
        return false;
    }
}