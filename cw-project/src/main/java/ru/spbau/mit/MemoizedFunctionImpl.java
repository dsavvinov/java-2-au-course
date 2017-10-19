package ru.spbau.mit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MemoizedFunctionImpl<T, R> implements MemoizedFunction<T, R> {
//    private static Set calculatedArguments = new HashSet<>();
    private Set<Object> beingCalculatedArguments = new HashSet<>();
    private Map<Object, CalculationResult> results = new HashMap<>();
    private Map<Object, Object> monitors = new HashMap<>();
    private Object nullMonitor = new Object();
    private final Function<T, R> function;

    MemoizedFunctionImpl(Function<T, R> function) {
        this.function = function;
    }

    @Override
    public R apply(T argument) throws RecursiveComputationException, InterruptedException {
        if (!isComputedAt(argument)) {
            do {
                Object monitor = getMonitor(argument);
                synchronized (monitor) {
                    // TODO double-checked locking.
                    boolean toCalculate;
                    synchronized (this) {
                        toCalculate = beingCalculatedArguments.add(argument);
                    }
                    if (toCalculate) {
                        R result = null;
                        RuntimeException exception = null;
                        try {
                            result = function.apply(argument);
                        } catch (RuntimeException runtimeException) {
                            exception = runtimeException;
                        }
                        synchronized (this) {
                            results.put(argument, new CalculationResult<R>(result, exception));
                        }
                        monitor.notifyAll();
                    } else {
                        monitor.wait();
                    }
                }
            } while (false);
        }
        CalculationResult result = getResult(argument);
        if (result.getException() != null) {
            throw result.getException();
        }
        return (R) result.getResult();
    }

    private CalculationResult getResult(T argument) {
        synchronized (this) {
            return results.get(argument);
        }
    }

    @Override
    public boolean isComputedAt(T argument) {
        synchronized (this) {
            return results.containsKey(argument);
        }
    }

    private Object getMonitor(Object argument) {
        if (argument == null) {
            return nullMonitor;
        }
        synchronized (this) {
            if (!monitors.containsKey(argument)) {
                if (monitors.put(argument, argument) != null) {
                    throw new RuntimeException("Invalid State");
                }
            }
            return monitors.get(argument);
        }
    }

    private static class CalculationResult<R> {
        private final R result;
        private final RuntimeException exception;

        private CalculationResult(R result, RuntimeException exception) {
            this.result = result;
            this.exception = exception;
        }

        public R getResult() {
            return result;
        }

        public RuntimeException getException() {
            return exception;
        }
    }
}
