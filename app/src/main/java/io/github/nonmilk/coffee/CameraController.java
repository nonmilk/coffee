package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.alphameo.linear_algebra.Validator;
import io.github.alphameo.linear_algebra.vec.Vec3Math;
import io.github.alphameo.linear_algebra.vec.Vector3;
import io.github.nonmilk.coffee.grinder.camera.Camera;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public final class CameraController {

    public static final float MIN_SENSITIVITY = 1f;
    public static final float MAX_SENSITIVITY = 11f;
    public static final float DEFAULT_OVERALL_SENSITIVITY = 6f;

    private float scrollSensitivity = DEFAULT_OVERALL_SENSITIVITY;
    private float mouseSensitivity = DEFAULT_OVERALL_SENSITIVITY;

    public static final float DEFAULT_MOUSE_TO_ANGLE_MULTIPLIER = 0.02f;
    public static final float DEFAULT_MOUSE_TO_MOVEMENT_MULTIPLIER = 0.01f;
    public static final float DEFAULT_SCROLL_ABS_MULTIPLIER = 0.05f;

    private float mouseToAngleMultiplier = DEFAULT_MOUSE_TO_ANGLE_MULTIPLIER;
    private float mouseToMovementMultiplier = DEFAULT_MOUSE_TO_MOVEMENT_MULTIPLIER;
    private float scrollAbsMultiplier = DEFAULT_SCROLL_ABS_MULTIPLIER;

    private Camera camera;
    private Canvas view;

    private float vAngle;
    private float hAngle;

    private float oldX;
    private float oldY;

    private boolean drag = false;

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

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public float setMouseSensitivity(final float sensitivity) {
        mouseSensitivity = validateSensitivity(sensitivity);

        final float senseMultiplier = computeSensitivityMultiplier(mouseSensitivity);

        mouseToMovementMultiplier = DEFAULT_MOUSE_TO_MOVEMENT_MULTIPLIER * senseMultiplier;
        mouseToAngleMultiplier = DEFAULT_MOUSE_TO_ANGLE_MULTIPLIER * senseMultiplier;

        // return resulting value because we have no exceptions in validation
        return mouseSensitivity;
    }

    public float getScrollSensitivity() {
        return scrollSensitivity;
    }

    public float setScrollSensitivity(final float sensitivity) {
        scrollSensitivity = validateSensitivity(sensitivity);
        final float senseMultiplier = computeSensitivityMultiplier(scrollSensitivity);

        scrollAbsMultiplier = DEFAULT_SCROLL_ABS_MULTIPLIER * senseMultiplier;

        // return resulting value because we have no exceptions in validation
        return scrollSensitivity;
    }

    public float setOverallSensitivity(final float sensitivity) {
        setMouseSensitivity(sensitivity);
        setScrollSensitivity(sensitivity);

        // return resulting value because we have no exceptions in validation
        return mouseSensitivity;
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

        final float scrollMultiplier = Math.signum(scrollValue) * scrollAbsMultiplier;

        Vec3Math.add(position(), Vec3Math.mult(direction, scrollMultiplier));
    }

    private void handleSphereMovement(final float mouseDX, final float mouseDY) {
        addHorAng(mouseDX * mouseToAngleMultiplier);
        addVertAng(-mouseDY * mouseToAngleMultiplier);

        final float r = Vec3Math.len(Vec3Math.subtracted(target(), position()));

        position().setX((float) (target().x() + r * Math.cos(hAngle) * Math.cos(vAngle)));
        position().setY((float) (target().y() + r * Math.sin(vAngle)));
        position().setZ((float) (target().z() + r * Math.sin(hAngle) * Math.cos(vAngle)));
    }

    private void handleSimpleMovement(final float mouseDX, final float mouseDY) {
        float ang = (float) Math.atan2(target().z() - position().z(), target().x() - position().x());
        ang -= ((float) Math.PI) / 2;
        final float dx = mouseDX * ((float) Math.cos(ang)) * mouseToMovementMultiplier;
        final float dy = mouseDX * ((float) Math.sin(ang)) * mouseToMovementMultiplier;

        // Horizontal: X movement
        position().setX(position().x() + dx);
        target().setX(target().x() + dx);

        // Horizontal: Y movement
        position().setZ(position().z() + dy);
        target().setZ(target().z() + dy);

        // Vertical: Z movement
        position().setY(position().y() - mouseDY * mouseToMovementMultiplier);
        target().setY(target().y() - mouseDY * mouseToMovementMultiplier);
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

    private float validateSensitivity(float sensitivity) {
        if (sensitivity < MIN_SENSITIVITY) {
            sensitivity = MIN_SENSITIVITY;
        }
        if (sensitivity > MAX_SENSITIVITY) {
            sensitivity = MAX_SENSITIVITY;
        }

        return sensitivity;
    }

    private float computeSensitivityMultiplier(final float sensitivity) {
        float midSense = sensitivity - DEFAULT_OVERALL_SENSITIVITY;
        if (midSense == 0) {
            return 1;
        } else if (midSense < 0) {
            return 1 / (Math.abs(midSense) + 1);
        }
        return Math.abs(midSense) + 1;
    }
}
