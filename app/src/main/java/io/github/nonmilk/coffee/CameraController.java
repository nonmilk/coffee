package io.github.nonmilk.coffee;

import java.util.Objects;

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

    public CameraController() {
    }

    public CameraController(final Camera camera, final Canvas view) {
        setCamera(camera);
        setView(view);
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
        view.setOnMouseReleased(event -> {
            drag = false;
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
            float dx = (float) (newX - oldX) * multiplier;
            float dy = (float) (newY - oldY) * multiplier;
            oldX = newX;
            oldY = newY;

            Vector3 target = camera.orientation().target();
            Vector3 camPos = camera.orientation().position();
            double r = LengthBetween(target, camPos);

            System.out.println(
                    camera.hashCode() + " \t" + event.getX() + " " + event.getY() + "| dx=" + dx + " dy= " + dy
                            + " shift: " + event.isShiftDown());

            if (event.isShiftDown()) {
                addHorAng(dx);
                addVertAng(dy);
                camPos
                        .setX((float) (target.x() + r * Math.cos(hAngle) * Math.cos(vAngle)));
                camPos
                        .setY((float) (target.y() + r * Math.sin(hAngle) * Math.cos(vAngle)));
                camPos
                        .setZ((float) (target.z() + r * Math.sin(vAngle)));
                System.out.println("h=" + hAngle + " v=" + vAngle);
                return;
            }

            camPos.setX(camPos.x() + dx);
            camPos.setY(camPos.y() + dy);
        });
    }

    private void addHorAng(double rad) {
        hAngle += rad;
        double pi2 = Math.PI * 2;
        if (hAngle > pi2) {
            hAngle -= pi2;
        }
    }

    private void addVertAng(double rad) {
        vAngle += rad;
        double pi2 = Math.PI * 2;
        if (Math.abs(vAngle) > pi2) {
            vAngle -= pi2;
        }
    }

    private double LengthBetween(Vector3 v1, Vector3 v2) {
        return Math.sqrt(
                (v1.x() - v2.x()) * (v1.x() - v2.x())
                        + (v1.y() - v2.y()) * (v1.y() - v2.y())
                        + (v1.z() - v2.z()) * (v1.z() - v2.z()));
    }
}
