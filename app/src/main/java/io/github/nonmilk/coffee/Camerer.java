package io.github.nonmilk.coffee;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.nonmilk.coffee.grinder.camera.Camera;
import io.github.nonmilk.coffee.grinder.camera.ClippingBox;
import io.github.nonmilk.coffee.grinder.camera.Orientation;
import io.github.nonmilk.coffee.grinder.camera.PerspectiveCamera;
import io.github.nonmilk.coffee.grinder.camera.view.PerspectiveView;
import io.github.nonmilk.coffee.grinder.math.Vec3f;
import io.github.nonmilk.coffee.grinder.render.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public final class Camerer {

    private static final String DEFAULT_NAME = "camera";
    private int namePostfix = 1;

    private Scene scene;

    private final Map<Scene, Map<String, NamedCamera>> scenes = new HashMap<>();
    private final Map<Scene, NamedCamera> activeCameras = new HashMap<>();

    private Map<String, NamedCamera> cameras = new HashMap<>();
    private NamedCamera active;

    @FXML
    private ListView<NamedCamera> view;

    private final ObservableList<NamedCamera> list = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        view.setItems(list);
    }

    private void update() {
        view.refresh();
    }

    public void setScene(final Scene s) {
        scene = Objects.requireNonNull(s);

        final var cameras = scenes.get(scene);
        if (cameras != null) {
            this.cameras = cameras;

            list.clear();
            list.addAll(cameras.values());

            select(activeCameras.get(scene).name());
            return;
        }

        this.cameras = new HashMap<>();
        scenes.put(scene, this.cameras);

        final String name = name();

        final var camera = scene.camera();
        if (camera != null) {
            add(camera, name);
        } else {
            add(camera(), name); // FIXME
        }

        list.clear();
        list.addAll(this.cameras.values());

        select(name);
    }

    private void add(final Camera c, final String name) {
        if (cameras.get(name) != null) {
            throw new IllegalArgumentException("this name already exists");
        }

        cameras.put(name, new NamedCamera(c, name));

        // TODO
        update();
    }

    private void remove(final String name) {
        final var camera = cameras.get(name);

        if (camera == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        } else if (camera == active) {
            throw new IllegalArgumentException("cannot remove active camera");
        }

        cameras.remove(name);

        // TODO
        update();
    }

    private void rename(final String oldName, final String newName) {
        final var camera = cameras.get(oldName);
        if (camera == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        if (cameras.get(newName) != null) {
            throw new IllegalArgumentException("this name already exists");
        }

        cameras.remove(oldName);
        camera.rename(newName);
        cameras.put(newName, camera);

        // TODO
        update();
    }

    private void select(final String name) {
        final var camera = cameras.get(name);
        if (camera == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        active = camera;
        activeCameras.put(scene, active);
        scene.setCamera(active());

        // TODO
        update();
    }

    private Camera active() {
        return active.unwrap();
    }

    // FIXME test code
    private Camera camera() {
        final var orientation = new Orientation(new Vec3f(5, 0, -5), new Vec3f(0, 0, 0));
        final var view = new PerspectiveView((float) ((70f * Math.PI) / 100f), 1.5f);
        final var box = new ClippingBox(0.1f, 10);
        return new PerspectiveCamera(orientation, view, box);
    }

    private String name() {
        return String.format("%s %d", DEFAULT_NAME, namePostfix++);
    }
}
