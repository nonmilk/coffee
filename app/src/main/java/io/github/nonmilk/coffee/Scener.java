package io.github.nonmilk.coffee;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.nonmilk.coffee.grinder.Renderer;
import io.github.nonmilk.coffee.grinder.render.Scene;
import javafx.fxml.FXML;

public final class Scener {

    private static final String DEFAULT_NAME = "scene";
    private int namePostfix = 1;

    private Renderer renderer;

    private Camerer camerer;

    private final Map<String, NamedScene> scenes = new HashMap<>();
    private NamedScene active;

    @FXML
    private void initialize() {
    }

    private void add(final Scene s, final String name) {
        if (scenes.get(name) != null) {
            throw new IllegalArgumentException("this name already exists");
        }

        scenes.put(name, new NamedScene(s, name));
    }

    private void remove(final String name) {
        final var scene = scenes.get(name);

        if (scene == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        } else if (scene == active) {
            throw new IllegalArgumentException("cannot remove active scene");
        }

        scenes.remove(name);
    }

    private void rename(final String oldName, final String newName) {
        final var scene = scenes.get(oldName);
        if (scene == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        if (scenes.get(newName) != null) {
            throw new IllegalArgumentException("this name already exists");
        }

        scenes.remove(oldName);
        scene.rename(newName);
        scenes.put(newName, scene);
    }

    private void select(final String name) {
        final var scene = scenes.get(name);
        if (scene == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        active = scene;
    }

    private Scene active() {
        return active.unwrap();
    }

    public void setRenderer(final Renderer renderer) {
        this.renderer = Objects.requireNonNull(renderer);
        updateScenes();
    }

    public void setCamerer(final Camerer camerer) {
        this.camerer = Objects.requireNonNull(camerer);
        updateCamerer();
    }

    private void updateScenes() {
        scenes.clear();
        active = new NamedScene(new Scene(), name());
        scenes.put(active.name(), active);
        renderer.setScene(active.unwrap());
        updateCamerer();
    }

    private void updateCamerer() {
        if (camerer == null) {
            return;
        }

        camerer.setScene(active.unwrap());
    }

    private String name() {
        return String.format("%s %d", DEFAULT_NAME, namePostfix++);
    }
}
