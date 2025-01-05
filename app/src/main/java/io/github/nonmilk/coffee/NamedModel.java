package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.nonmilk.coffee.grinder.Model;

public final class NamedModel implements Named {

    private final Model model;
    private String name;

    public NamedModel(final Model m, final String name) {
        model = Objects.requireNonNull(m);
        rename(name);
    }

    public Model unwrap() {
        return model;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void rename(final String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String toString() {
        return name;
    }
}