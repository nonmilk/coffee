package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.alphameo.linear_algebra.Validator;
import io.github.alphameo.linear_algebra.vec.Vec3Math;
import io.github.alphameo.linear_algebra.vec.Vector3;
import io.github.nonmilk.coffee.grinder.camera.Camera;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public final class CameraController {

    private Camera camera;
    private Canvas view;

    private double vAngle;
    private double hAngle;

    private double oldX;
    private double oldY;

    private boolean drag = false;

    private static final float MOUSE_TO_ANGLE_MULTIPLIER = 0.02f;
    private static final float ABS_SCROLL_MULTIPLIER = 0.05f;

    public CameraController() {
    }

    public CameraController(final Camera camera, final Canvas view) {
        setCamera(camera);
        setView(view);
    }

    public void setCamera(final Camera camera) {
        Objects.requireNonNull(camera);
        this.camera = camera;
    }

    public void setView(final Canvas view) {
        Objects.requireNonNull(view);
        this.view = view;
        initCanvas();
    }

    private void initCanvas() {
        view.setOnMouseReleased(event -> {
            drag = false;
        });

        view.setOnScroll(event -> handleScroll((float) event.getDeltaY()));

        view.setOnMouseDragged(event -> handleOnMouseDrag(event));
    }

    private void addHorAng(final double rad) {
        hAngle += rad;
        if (hAngle < -Math.PI) {
            hAngle = Math.PI;
        } else if (hAngle > Math.PI) {
            hAngle = -Math.PI;
        }
    }

    private void addVertAng(final double rad) {
        final double newVAngle = vAngle + rad;
        if (Math.abs(newVAngle) < Math.PI / 2) {
            vAngle = newVAngle;
        }
    }

    private void handleOnMouseDrag(final MouseEvent event) {
        if (!event.isPrimaryButtonDown()) {
            return;
        }
        if (!drag) {
            oldX = event.getX();
            oldY = event.getY();
            drag = true;
            return;
        }

        final double newX = event.getX();
        final double newY = event.getY();
        final float dx = (float) (newX - oldX);
        final float dy = (float) (newY - oldY);
        oldX = newX;
        oldY = newY;

        if (event.isShiftDown()) {
            handleSphereMovement(dx, dy);
            return;
        }

        handleSimpleMovement(dx, dy);
    }

    private void handleScroll(final float scrollValue) {
        if (Validator.equalsEpsilon(scrollValue, 0, 0.0001f)) {
            return;
        }
        final Vector3 target = camera.orientation().target();
        final Vector3 position = camera.orientation().position();

        final Vector3 direction = Vec3Math.subtracted(target, position);

        final float scrollMultiplier = Math.signum(scrollValue) * ABS_SCROLL_MULTIPLIER;

        Vec3Math.add(position, Vec3Math.mult(direction, scrollMultiplier));
    }

    private void handleSphereMovement(final float mouseDX, final float mouseDY) {
        final Vector3 target = camera.orientation().target();
        final Vector3 position = camera.orientation().position();

        addHorAng(mouseDX * MOUSE_TO_ANGLE_MULTIPLIER);
        addVertAng(mouseDY * MOUSE_TO_ANGLE_MULTIPLIER);

        final double r = Vec3Math.len(Vec3Math.subtracted(target, position));

        position.setX((float) (target.x() + r * Math.cos(hAngle) * Math.cos(vAngle)));
        position.setY((float) (target.y() + r * Math.sin(hAngle) * Math.cos(vAngle)));
        position.setZ((float) (target.z() + r * Math.sin(vAngle)));
    }

    private void handleSimpleMovement(final float mouseDX, final float mouseDY) {
        final Vector3 target = camera.orientation().target();
        final Vector3 position = camera.orientation().position();

        position.setX(position.x() + mouseDX);
        position.setY(position.y() + mouseDY);
        target.setX(target.x() + mouseDX);
        target.setY(target.y() + mouseDY);
    }
}
