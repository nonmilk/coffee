package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.nonmilk.coffee.grinder.Renderer;
import io.github.shimeoki.jfx.rasterization.Point2i;
import io.github.shimeoki.jfx.rasterization.Vector2i;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public final class Viewer {

    public static final int DEFAULT_FPS = 60;

    @FXML
    private Canvas view;

    @FXML
    private VBox viewPane;

    private Renderer renderer;
    private Camerer camerer;

    private final Timeline timeline = new Timeline();
    private double fps;

    private boolean drag = false;

    private GraphicsContext ctx;

    private final Point2i start = new Vector2i(-1, -1);
    private final Point2i end = new Vector2i(-1, -1);

    private Selection selection = new Selection();

    {
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    @FXML
    private void initialize() {
        renderer = new Renderer(view.getGraphicsContext2D());

        initView();
        initSelection();
        ctx = view.getGraphicsContext2D();

        setFPS(DEFAULT_FPS);
    }

    // doesn't look good, but what else should i do
    private void initView() {
        viewPane.widthProperty().addListener((ov, oldValue, newValue) -> {
            view.setWidth(newValue.doubleValue());

            if (camerer == null) {
                return;
            }

            camerer.update((float) view.getWidth(), (float) view.getHeight());
        });

        viewPane.heightProperty().addListener((ov, oldValue, newValue) -> {
            view.setHeight(newValue.doubleValue());

            if (camerer == null) {
                return;
            }

            camerer.update((float) view.getWidth(), (float) view.getHeight());
        });
    }

    private void initSelection() {
        view.setOnMouseReleased(e -> {
            undrag();
        });

        view.setOnMouseDragged(e -> {
            handleMouse(e);
            camerer.controller().drag(e);
        });

        view.setOnMouseDragReleased(e -> {
            undrag();
        });
    }

    private void resetSelectionPoints() {
        start.setX(-1);
        start.setY(-1);
        end.setX(-1);
        end.setY(-1);
    }

    private void undrag() {
        drag = false;
        resetSelectionPoints();
        camerer.controller().undrag();
    }

    private void handleMouse(final MouseEvent e) {
        if (!e.isSecondaryButtonDown()) {
            resetSelectionPoints();
            return;
        }

        if (!drag) {
            start.setX((int) e.getX());
            start.setY((int) e.getY());

            drag = true;
            return;
        }

        end.setX((int) e.getX());
        end.setY((int) e.getY());
    }

    private void renderSelection() {
        if (start.x() <= 0 || start.y() <= 0) {
            return;
        }

        if (end.x() <= 0 || end.y() <= 0) {
            return;
        }

        selection.update(start, end);

        ctx.strokeRect(
                selection.x(),
                selection.y(),
                selection.width(),
                selection.height());
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
            renderSelection();
        });
    }

    public Renderer renderer() {
        return renderer;
    }

    public void setCamerer(final Camerer c) {
        camerer = Objects.requireNonNull(c);
    }
}
