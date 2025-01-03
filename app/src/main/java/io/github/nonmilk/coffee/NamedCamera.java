package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.nonmilk.coffee.grinder.camera.Camera;

public final class NamedCamera implements Named {

    private final Camera camera;
    private String name;

    public NamedCamera(final Camera c, final String name) {
        camera = Objects.requireNonNull(c);
        rename(name);
    }

    public Camera unwrap() {
        return camera;
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
