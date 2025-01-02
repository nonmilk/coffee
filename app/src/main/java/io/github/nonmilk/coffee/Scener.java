package io.github.nonmilk.coffee;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.nonmilk.coffee.grinder.Renderer;
import io.github.nonmilk.coffee.grinder.render.Scene;
import javafx.fxml.FXML;

public final class Scener {

    private Renderer renderer;

    private final List<Scene> scenes = new ArrayList<>();

    @FXML
    private void initialize() {
    }

    public void setRenderer(final Renderer renderer) {
        this.renderer = Objects.requireNonNull(renderer);
        update();
    }

    private void update() {
        scenes.clear();
        scenes.add(new Scene());
        renderer.setScene(scenes.get(0));
    }
}
