package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.nonmilk.coffee.grinder.Model;

public final class NamedModel implements Named {

    private final Model model;
    private Status status;
    private String name;

    public enum Status {

        DEFAULT(""),
        ACTIVE(" (active)"),
        HIDDEN(" (hidden)");

        private String s;

        private Status(final String s) {
            this.s = Objects.requireNonNull(s);
        }

        @Override
        public String toString() {
            return s;
        }
    }

    public NamedModel(final Model m, final String name) {
        model = Objects.requireNonNull(m);
        status = Status.DEFAULT;
        rename(name);
    }

    public Model unwrap() {
        return model;
    }

    public void setStatus(final Status s) {
        status = Objects.requireNonNull(s);
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
        return String.format("%s%s", name, status);
    }
}
