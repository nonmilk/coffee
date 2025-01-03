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

    private void rename(final String oldName, final String newName) {
        final var scene = scenes.get(oldName);
        if (scene == null) {
            throw new IllegalArgumentException("scene with this name doesn't exist");
        }

        if (scenes.get(newName) != null) {
            throw new IllegalArgumentException("this name already exists");
        }

        scenes.remove(oldName);
        scene.rename(newName);
        scenes.put(newName, scene);
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
