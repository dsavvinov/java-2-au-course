package ru.spbau.mit;

import java.util.function.Supplier;

class LazyValueImpl<R> implements LazyValue<R> {
    private final Supplier<R> supplier;
    private volatile boolean isCalculated;
    private volatile boolean beingCalculated;
    private R result;
    private RuntimeException exception;

    LazyValueImpl(Supplier<R> supplier) {
        this.supplier = supplier;
        this.isCalculated = false;
        this.beingCalculated = false;
    }

    @Override
    public R get() throws RecursiveComputationException, InterruptedException {
        synchronized (this) {
            if (!isCalculated) {
                if (beingCalculated) {
                    while (!isCalculated) {
                        wait();
                    }
                } else {
                    beingCalculated = true;
                    try {
                        result = supplier.get();
                    } catch (RuntimeException runtimeException) {
                        exception = runtimeException;
                    }
                    isCalculated = true;
                    notifyAll();
                }
            } else {
                while (!isCalculated) {
                    wait();
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
        return result;
    }

    @Override
    public boolean isReady() {
        return isCalculated;
    }
}
