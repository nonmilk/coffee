package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.alphameo.linear_algebra.Validator;
import io.github.alphameo.linear_algebra.vec.Vec3Math;
import io.github.alphameo.linear_algebra.vec.Vector3;
import io.github.nonmilk.coffee.grinder.camera.Camera;
import io.github.nonmilk.coffee.grinder.math.affine.Rotator;
import io.github.nonmilk.coffee.grinder.math.affine.Transformation;
import io.github.nonmilk.coffee.grinder.math.affine.Translator;
import io.github.nonmilk.coffee.grinder.math.affine.Rotator.Axis;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public final class CameraController {

    private Camera camera;
    private Canvas view;

    private float vAngle;
    private float hAngle;

    private float oldX;
    private float oldY;

    private boolean drag = false;

    private static final float MOUSE_TO_ANGLE_MULTIPLIER = 0.02f;
    private static final float MOUSE_TO_MOVEMENT_MULTIPLIER = 0.01f;
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
        initAngles();
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

    private Vector3 target() {
        return camera.orientation().target();
    }

    private Vector3 position() {
        return camera.orientation().position();
    }

    private void addHorAng(final double rad) {
        hAngle += rad;
        if (hAngle < -Math.PI) {
            hAngle = (float) Math.PI;
        } else if (hAngle > Math.PI) {
            hAngle = -(float) Math.PI;
        }
    }

    private void addVertAng(final float rad) {
        final float newVAngle = vAngle + rad;
        if (Math.abs(newVAngle) < Math.PI / 2) {
            vAngle = newVAngle;
        }
    }

    private void handleOnMouseDrag(final MouseEvent event) {
        if (!event.isPrimaryButtonDown()) {
            return;
        }
        if (!drag) {
            oldX = (float) event.getX();
            oldY = (float) event.getY();
            drag = true;
            return;
        }

        final float newX = (float) event.getX();
        final float newY = (float) event.getY();
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

        final Vector3 direction = Vec3Math.subtracted(target(), position());

        final float scrollMultiplier = Math.signum(scrollValue) * ABS_SCROLL_MULTIPLIER;

        Vec3Math.add(position(), Vec3Math.mult(direction, scrollMultiplier));
    }

    private void handleSphereMovement(final float mouseDX, final float mouseDY) {
        addHorAng(mouseDX * MOUSE_TO_ANGLE_MULTIPLIER);
        addVertAng(-mouseDY * MOUSE_TO_ANGLE_MULTIPLIER);

        final float r = Vec3Math.len(Vec3Math.subtracted(target(), position()));

        position().setX((float) (target().x() + r * Math.cos(hAngle) * Math.cos(vAngle)));
        position().setY((float) (target().y() + r * Math.sin(vAngle)));
        position().setZ((float) (target().z() + r * Math.sin(hAngle) * Math.cos(vAngle)));
    }

    private void handleSimpleMovement(final float mouseDX, final float mouseDY) {
        float ang = (float) Math.atan2(target().z() - position().z(), target().x() - position().x());
        ang -= ((float) Math.PI) / 2;
        float dx = mouseDX * ((float) Math.cos(ang)) * MOUSE_TO_MOVEMENT_MULTIPLIER;
        float dy = mouseDX * ((float) Math.sin(ang)) * MOUSE_TO_MOVEMENT_MULTIPLIER;

        // Horizontal: X movement
        position().setX(position().x() + dx);
        target().setX(target().x() + dx);

        // Horizontal: Y movement
        position().setZ(position().z() + dy);
        target().setZ(target().z() + dy);

        // Vertical: Z movement
        position().setY(position().y() - mouseDY * MOUSE_TO_MOVEMENT_MULTIPLIER);
        target().setY(target().y() - mouseDY * MOUSE_TO_MOVEMENT_MULTIPLIER);
    }

    private void initAngles() {
        final float r = Vec3Math.len(Vec3Math.subtracted(target(), position()));
        if (Validator.equalsEpsilon(r, 0, 1e-5f)) {
            // Position and target matches. So keep default angles.
            return;
        }

        vAngle = (float) Math.asin((position().y() - target().y()) / r);

        final float cosv = (float) Math.cos(vAngle);
        if (Validator.equalsEpsilon(cosv, 0, 1e-5f)) {
            // See the target from above or from below -> horizontal angle is not important.
            // So keep default.
            return;
        }

        hAngle = (float) Math.asin((position().z() - target().z()) / r / cosv);
    }
}
