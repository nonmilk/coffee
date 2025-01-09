package io.github.nonmilk.coffee;

public final class CoffeeError extends RuntimeException {

    public CoffeeError(final String msg, final Throwable e) {
        super(msg, e);
    }
}
