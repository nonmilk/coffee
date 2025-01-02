package io.github.nonmilk.coffee;

import java.util.Objects;

import io.github.nonmilk.coffee.grinder.camera.Camera;
import io.github.nonmilk.coffee.grinder.camera.ClippingBox;
import io.github.nonmilk.coffee.grinder.camera.Orientation;
import io.github.nonmilk.coffee.grinder.camera.PerspectiveCamera;
import io.github.nonmilk.coffee.grinder.camera.view.PerspectiveView;
import io.github.nonmilk.coffee.grinder.math.Vec3f;
import io.github.nonmilk.coffee.grinder.render.Scene;

public final class Camerer {

    // TODO multiple cameras
    private Scene scene;

    // FIXME test code
    public void setScene(final Scene s) {
        scene = Objects.requireNonNull(s);
        scene.cameras().add(camera());
        scene.selectCamera(0);
    }

    // FIXME test code
    private Camera camera() {
        final var orientation = new Orientation(new Vec3f(5, 0, -5), new Vec3f(0, 0, 0));
        final var view = new PerspectiveView((float) ((70f * Math.PI) / 100f), 1.5f);
        final var box = new ClippingBox(0.1f, 10);
        return new PerspectiveCamera(orientation, view, box);
    }
}
