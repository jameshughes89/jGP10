package jgp.language;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class OperatorRegistry {

    private final Map<String, Operator> operatorsBySymbol;

    public OperatorRegistry(Operator... operators) {
        this(List.of(operators));
    }

    public OperatorRegistry(Collection<? extends Operator> operators) {
        Objects.requireNonNull(operators, "operators must not be null");
        if (operators.isEmpty()) {
            throw new IllegalArgumentException("At least one operator must be provided");
        }
        Map<String, Operator> map = new LinkedHashMap<>();
        for (Operator operator : operators) {
            Objects.requireNonNull(operator, "operator must not be null");
            String symbol = operator.symbol();
            if (map.containsKey(symbol)) {
                throw new IllegalArgumentException("Duplicate operator symbol: " + symbol);
            }
            map.put(symbol, operator);
        }

        this.operatorsBySymbol = Collections.unmodifiableMap(map);
    }

    public boolean containsSymbol(String symbol) {
        return operatorsBySymbol.containsKey(symbol);
    }

    public Collection<Operator> operators() {
        return operatorsBySymbol.values();
    }

    public int size() {
        return operatorsBySymbol.size();
    }
}
