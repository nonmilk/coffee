package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.nonmilk.coffee.grinder.camera.Camera;
import javafx.scene.canvas.Canvas;

public final class CameraController {

    private Camera camera;
    private Canvas view;

    public CameraController() {
    }

    public CameraController(final Camera camera, final Canvas canvas) {
        this.camera = camera;
        this.view = canvas;
    }

    public void setCamera(Camera camera) {
        Objects.requireNonNull(camera);
        System.out.println("Camera(" + camera.hashCode() + ") has been initialized");
        this.camera = camera;
    }

    public void setView(Canvas view) {
        Objects.requireNonNull(view);
        this.view = view;
        initCanvas();
    }

    private void initCanvas() {
        view.setOnMouseDragged(event -> {
            System.out.println(camera.hashCode() + " \t" + event.getX() + " " + event.getY() + " shift: " + event.isShiftDown());
        });
    }
}
