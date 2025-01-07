package io.github.nonmilk.coffee;

import java.util.Objects;

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
    private static final float SCROLL_MULTIPLIER = 0.01f;

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

        final Vector3 target = camera.orientation().target();
        final Vector3 position = camera.orientation().position();

        view.setOnScroll(event -> {
            final Vector3 direction = Vec3Math.subtracted(target, position);
            float absScroll = (float) Math.abs(event.getDeltaY() * SCROLL_MULTIPLIER);
            Vector3 dvec;
            if (event.getDeltaY() < 0) {
                dvec = Vec3Math.divided(direction, absScroll);
            } else {
                dvec = Vec3Math.multiplied(direction, absScroll);
            }
            final Vector3 pos = Vec3Math.added(new Vec3(position), dvec);
            System.out.println(pos.x() + " " + pos.y() + " " + pos.z());
            position.setX(pos.x());
            position.setY(pos.y());
            position.setZ(pos.z());
        });

        view.setOnMouseDragged(event -> {
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

            final double r = Vec3Math.len(Vec3Math.subtracted(target, position));

            System.out.println(
                    camera.hashCode() + " \t" + event.getX() + " " + event.getY() + "| dx=" + dx + " dy= " + dy
                            + " shift: " + event.isShiftDown());

            if (event.isShiftDown()) {
                addHorAng(dx);
                addVertAng(dy);
                position
                        .setX((float) (target.x() + r * Math.cos(hAngle) * Math.cos(vAngle)));
                position
                        .setY((float) (target.y() + r * Math.sin(hAngle) * Math.cos(vAngle)));
                position
                        .setZ((float) (target.z() + r * Math.sin(vAngle)));

                System.out.println("h=" + hAngle + " v=" + vAngle);
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

    private double LengthBetween(final Vector3 v1, final Vector3 v2) {
        return Math.sqrt(
                (v1.x() - v2.x()) * (v1.x() - v2.x())
                        + (v1.y() - v2.y()) * (v1.y() - v2.y())
                        + (v1.z() - v2.z()) * (v1.z() - v2.z()));
    }
}
