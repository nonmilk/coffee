package io.github.nonmilk.coffee;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.nonmilk.coffee.grinder.Renderer;
import io.github.nonmilk.coffee.grinder.render.Scene;
import javafx.fxml.FXML;

public final class Scener {

    private static final String DEFAULT_NAME = "scene";

    private Renderer renderer;

    private Camerer camerer;

    private final Map<Scene, String> scenes = new HashMap<>();
    private Scene active;

    @FXML
    private void initialize() {
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
        active = new Scene();
        scenes.put(active, DEFAULT_NAME);
        renderer.setScene(active);
        updateCamerer();
    }

    private void updateCamerer() {
        if (camerer == null) {
            return;
        }

        camerer.setScene(active);
    }
}
