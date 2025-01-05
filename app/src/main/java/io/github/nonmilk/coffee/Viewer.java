package io.github.nonmilk.coffee;

import io.github.nonmilk.coffee.grinder.Renderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.util.Duration;

public final class Viewer {

    public static final int DEFAULT_FPS = 60;

    @FXML
    private Canvas view;

    private Renderer renderer;

    private final Timeline timeline = new Timeline();
    private double fps;

    {
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    @FXML
    private void initialize() {
        renderer = new Renderer(view.getGraphicsContext2D());
        setFPS(DEFAULT_FPS);
    }

    public int fps() {
        return (int) fps;
    }

    public void setFPS(final int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("fps <= 0");
        }

        this.fps = fps;
        // update();
    }

    private void update() {
        final var frames = timeline.getKeyFrames();

        timeline.stop();

        frames.clear();
        frames.add(frame());

        timeline.play();
    }

    private KeyFrame frame() {
        return new KeyFrame(Duration.millis(1000 / fps), e -> {
            renderer.render();
        });
    }

    public Renderer renderer() {
        return renderer;
    }
}
