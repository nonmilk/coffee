package io.github.nonmilk.coffee;

import io.github.nonmilk.coffee.grinder.Renderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public final class Viewer {

    public static final int DEFAULT_FPS = 60;

    @FXML
    private Canvas view;

    @FXML
    private VBox viewPane;

    private Renderer renderer;

    private final Timeline timeline = new Timeline();
    private double fps;

    {
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    @FXML
    private void initialize() {
        renderer = new Renderer(view.getGraphicsContext2D());

        initView();

        setFPS(DEFAULT_FPS);
    }

    private void initView() {
        viewPane.widthProperty().addListener((ov, oldValue, newValue) -> {
            view.setWidth(newValue.doubleValue());
        });

        viewPane.heightProperty().addListener((ov, oldValue, newValue) -> {
            view.setHeight(newValue.doubleValue());
        });
    }

    public int fps() {
        return (int) fps;
    }

    public void setFPS(final int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("fps <= 0");
        }

        this.fps = fps;
        update();
    }

    public Canvas view() {
        return view;
    }

    private void update() {
        final var frames = timeline.getKeyFrames();

        timeline.stop();

        frames.clear();
        frames.add(frame());

        timeline.play();
    }

    private KeyFrame frame() {
        final var ctx = view.getGraphicsContext2D();

        return new KeyFrame(Duration.millis(1000 / fps), e -> {
            ctx.clearRect(0, 0, view.getWidth(), view.getHeight());
            renderer.render();
        });
    }

    public Renderer renderer() {
        return renderer;
    }
}
