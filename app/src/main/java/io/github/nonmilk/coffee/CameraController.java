package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.alphameo.linear_algebra.Validator;
import io.github.alphameo.linear_algebra.vec.Vec3;
import io.github.alphameo.linear_algebra.vec.Vec3Math;
import io.github.alphameo.linear_algebra.vec.Vector3;
import io.github.nonmilk.coffee.grinder.camera.Camera;
import javafx.scene.canvas.Canvas;

public final class CameraController {

    private Camera camera;
    private Canvas view;

    private double vAngle;
    private double hAngle;

    private double oldX;
    private double oldY;

    private boolean drag = false;

    private static final float multiplier = 0.02f;
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

        view.setOnScroll(event -> {
            if (Validator.equalsEpsilon((float) event.getDeltaY(), 0, 0.0001f)) {
                return;
            }
            final Vector3 target = camera.orientation().target();
            final Vector3 position = camera.orientation().position();

            final Vector3 direction = Vec3Math.subtracted(target, position);

            float scrollMultiplier = Math.signum((float) event.getDeltaY()) * ABS_SCROLL_MULTIPLIER;

            Vec3Math.add(position, Vec3Math.mult(direction, scrollMultiplier));
        });

        view.setOnMouseDragged(event -> {
            if (!drag) {
                oldX = event.getX();
                oldY = event.getY();
                drag = true;
                return;
            }

            final Vector3 target = camera.orientation().target();
            final Vector3 position = camera.orientation().position();
            final double newX = event.getX();
            final double newY = event.getY();
            final float dx = (float) (newX - oldX);
            final float dy = (float) (newY - oldY);
            oldX = newX;
            oldY = newY;

            final double r = Vec3Math.len(Vec3Math.subtracted(target, position));

            System.out.println(
                    camera.hashCode() + " \t" + event.getX() + " " + event.getY() + "| dx=" + dx + " dy= " + dy
                            + " shift: " + event.isShiftDown());

            if (event.isShiftDown()) {
                addHorAng(dx);
                addVertAng(dy);
                position.setX((float) (target.x() + r * Math.cos(hAngle) * Math.cos(vAngle)));
                position.setY((float) (target.y() + r * Math.sin(hAngle) * Math.cos(vAngle)));
                position.setZ((float) (target.z() + r * Math.sin(vAngle)));
                return;
            }

            position.setX(position.x() + dx);
            position.setY(position.y() + dy);
            target.setX(target.x() + dx);
            target.setY(target.y() + dy);
        });
    }

    private void addHorAng(final double rad) {
        hAngle += rad * multiplier;
        if (hAngle < -Math.PI) {
            hAngle = Math.PI;
        } else if (hAngle > Math.PI) {
            hAngle = -Math.PI;
        }
    }

    private void addVertAng(final double rad) {
        final double newVAngle = vAngle + rad * multiplier;
        if (Math.abs(newVAngle) < Math.PI / 2) {
            vAngle = newVAngle;
        }
    }
}
