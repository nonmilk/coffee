package io.github.nonmilk.coffee;

import java.util.Objects;

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
    private static final float SCROLL_MULTIPLIER = 0.05f;

    public CameraController() {
    }

    public CameraController(final Camera camera, final Canvas view) {
        setCamera(camera);
        setView(view);
    }

    public void setCamera(Camera camera) {
        Objects.requireNonNull(camera);
        this.camera = camera;
    }

    public void setView(Canvas view) {
        Objects.requireNonNull(view);
        this.view = view;
        initCanvas();
    }

    private void initCanvas() {
        view.setOnMouseReleased(event -> {
            drag = false;
        });

        Vector3 target = camera.orientation().target();
        Vector3 position = camera.orientation().position();

        view.setOnScroll(event -> {
            Vector3 direction = Vec3Math.subtracted(target, position);
            Vector3 pos = Vec3Math.added(position,
                    Vec3Math.multiplied(direction, (int) (event.getDeltaY() * SCROLL_MULTIPLIER)));
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

            double newX = event.getX();
            double newY = event.getY();
            float dx = (float) (newX - oldX);
            float dy = (float) (newY - oldY);
            oldX = newX;
            oldY = newY;

            double r = LengthBetween(target, position);

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

    private void addHorAng(double rad) {
        hAngle += rad * multiplier;
        if (hAngle < -Math.PI) {
            hAngle = Math.PI;
        } else if (hAngle > Math.PI) {
            hAngle = -Math.PI;
        }
    }

    private void addVertAng(double rad) {
        double newVAngle = vAngle + rad * multiplier;
        if (Math.abs(newVAngle) < Math.PI / 2) {
            vAngle = newVAngle;
        }
    }

    private double LengthBetween(Vector3 v1, Vector3 v2) {
        return Math.sqrt(
                (v1.x() - v2.x()) * (v1.x() - v2.x())
                        + (v1.y() - v2.y()) * (v1.y() - v2.y())
                        + (v1.z() - v2.z()) * (v1.z() - v2.z()));
    }
}
