package ru.spbau.mit;

public class MemoizedFunctionImpl<T, R> implements MemoizedFunction<T, R> {
    @Override
    public R apply(T argument) throws RecursiveComputationException, InterruptedException {
        return null;
    }

    @Override
    public boolean isComputedAt(T argument) {
        return false;
    }
}
