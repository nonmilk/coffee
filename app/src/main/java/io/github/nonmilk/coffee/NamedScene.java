package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.nonmilk.coffee.grinder.render.Scene;

public final class NamedScene implements Named {

    private final Scene scene;
    private String name;

    public enum Status {

        DEFAULT(""),
        ACTIVE(" (active)");

        private String s;

        private Status(final String s) {
            this.s = Objects.requireNonNull(s);
        }

        @Override
        public String toString() {
            return s;
        }
    }

    public NamedScene(final Scene s, final String name) {
        scene = Objects.requireNonNull(s);
        rename(name);
    }

    public Scene unwrap() {
        return scene;
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
