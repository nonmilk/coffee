package io.github.nonmilk.coffee;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.alphameo.linear_algebra.vec.Vec3;
import io.github.alphameo.linear_algebra.vec.Vector3;
import io.github.nonmilk.coffee.grinder.camera.Camera;
import io.github.nonmilk.coffee.grinder.camera.ClippingBox;
import io.github.nonmilk.coffee.grinder.camera.Orientation;
import io.github.nonmilk.coffee.grinder.camera.OrthographicCamera;
import io.github.nonmilk.coffee.grinder.camera.PerspectiveCamera;
import io.github.nonmilk.coffee.grinder.camera.view.OrthographicView;
import io.github.nonmilk.coffee.grinder.camera.view.PerspectiveView;
import io.github.nonmilk.coffee.grinder.math.Vec3f;
import io.github.nonmilk.coffee.grinder.render.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

// TODO selected camera edit

public final class Camerer {

    private static final String DEFAULT_NAME = "Camera";
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
    private Button addBtn;

    @FXML
    private Button removeBtn;

    @FXML
    private Button renameBtn;

    @FXML
    private Button selectBtn;

    @FXML
    private TextField positionXField;

    @FXML
    private TextField positionYField;

    @FXML
    private TextField positionZField;

    @FXML
    private TextField targetXField;

    @FXML
    private TextField targetYField;

    @FXML
    private TextField targetZField;

    @FXML
    private StackPane viewPane;

    @FXML
    private RadioButton perspectiveBtn;

    @FXML
    private RadioButton orthographicBtn;

    @FXML
    private HBox orthographicViewPane;

    @FXML
    private TextField widthField;

    @FXML
    private TextField heightField;

    @FXML
    private HBox perspectiveViewPane;

    @FXML
    private TextField arField;

    @FXML
    private TextField fovField;

    @FXML
    private TextField boxNearPlaneField;

    @FXML
    private TextField boxFarPlaneField;

    // TODO refactor
    @FXML
    private void initialize() {
        view.setItems(list);

        selectBtn.setOnAction(e -> {
            final var selection = view.selectionModelProperty().get();
            select(selection.getSelectedItem().name());
        });

        addBtn.setOnAction(e -> {
            final var name = name();

            add(createFromFields(), name);
            list.add(cameras.get(name));
            view.refresh();

            select(name);
        });

        removeBtn.setOnAction(e -> {
            final var selection = view.selectionModelProperty().get();
            remove(selection.getSelectedItem().name());
            list.remove(selection.getSelectedIndex());
            view.refresh();
        });

        renameBtn.setOnAction(e -> {
            final var selection = view.selectionModelProperty().get();
            final var name = selection.getSelectedItem().name();

            final TextInputDialog dialog = new TextInputDialog(name);
            dialog.show();

            // TODO don't make dialog on each rename
            // FIXME ignore rename on closing with cancel
            dialog.setOnCloseRequest(event -> {
                try {
                    rename(name, dialog.getEditor().getText());
                } catch (final IllegalArgumentException err) {
                    event.consume();
                    // TODO error alert
                }
            });

            dialog.setOnHidden(event -> {
                view.refresh();
            });
        });

        final var stack = viewPane.getChildren();

        perspectiveBtn.setOnAction(e -> {
            stack.clear();
            stack.add(perspectiveViewPane);
        });

        orthographicBtn.setOnAction(e -> {
            stack.clear();
            stack.add(orthographicViewPane);
        });
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
        // TODO update visual selection

        select(name);
    }

    // TODO manage view from there
    private void add(final Camera c, final String name) {
        if (cameras.get(name) != null) {
            throw new IllegalArgumentException("this name already exists");
        }

        cameras.put(name, new NamedCamera(c, name));
    }

    // TODO manage view from there
    private void remove(final String name) {
        final var camera = cameras.get(name);

        if (camera == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        } else if (camera == active) {
            throw new IllegalArgumentException("cannot remove active camera");
        }

        cameras.remove(name);
    }

    // TODO manage view from there
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
    }

    // TODO manage view from there
    private void select(final String name) {
        final var camera = cameras.get(name);
        if (camera == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        active = camera;
        activeCameras.put(scene, active);
        scene.setCamera(active());

        view.getSelectionModel().select(active);
        updateFields();
    }

    private Camera active() {
        return active.unwrap();
    }

    // TODO error handling
    private Camera createFromFields() {
        final var posX = Float.parseFloat(positionXField.getText());
        final var posY = Float.parseFloat(positionYField.getText());
        final var posZ = Float.parseFloat(positionZField.getText());
        final Vector3 pos = new Vec3(posX, posY, posZ);

        final var targetX = Float.parseFloat(targetXField.getText());
        final var targetY = Float.parseFloat(targetYField.getText());
        final var targetZ = Float.parseFloat(targetZField.getText());
        final Vector3 target = new Vec3(targetX, targetY, targetZ);

        final var orientation = new Orientation(pos, target);

        final var nearPlane = Float.parseFloat(boxNearPlaneField.getText());
        final var farPlane = Float.parseFloat(boxFarPlaneField.getText());
        final var box = new ClippingBox(nearPlane, farPlane);

        if (orthographicBtn.isSelected()) {
            final var width = Float.parseFloat(widthField.getText());
            final var height = Float.parseFloat(heightField.getText());
            final var view = new OrthographicView(width, height);
            return new OrthographicCamera(orientation, view, box);
        } else if (perspectiveBtn.isSelected()) {
            final var fov = Float.parseFloat(fovField.getText());
            final var ar = Float.parseFloat(arField.getText());
            final var view = new PerspectiveView(fov, ar);
            return new PerspectiveCamera(orientation, view, box);
        }

        return null; // maybe not the best solution
    }

    private void updateFields() {
        final var cam = active();
        final var orientation = cam.orientation();
        final var box = cam.box();
        final var pos = orientation.position();
        final var target = orientation.target();

        positionXField.setText(String.valueOf(pos.x()));
        positionYField.setText(String.valueOf(pos.y()));
        positionZField.setText(String.valueOf(pos.z()));

        targetXField.setText(String.valueOf(target.x()));
        targetYField.setText(String.valueOf(target.y()));
        targetZField.setText(String.valueOf(target.z()));

        if (cam instanceof OrthographicCamera orthographic) {
            final var view = orthographic.view();
            orthographicBtn.fire();

            widthField.setText(String.valueOf(view.width()));
            heightField.setText(String.valueOf(view.height()));

            arField.clear();
            fovField.clear();
        } else if (cam instanceof PerspectiveCamera perspective) {
            final var view = perspective.view();
            perspectiveBtn.fire();

            arField.setText(String.valueOf(view.aspectRatio()));
            fovField.setText(String.valueOf(view.fov()));

            widthField.clear();
            heightField.clear();
        }

        boxNearPlaneField.setText(String.valueOf(box.nearPlane()));
        boxFarPlaneField.setText(String.valueOf(box.farPlane()));
    }

    // FIXME test code
    private Camera camera() {
        final var orientation = new Orientation(new Vec3f(5, 0, -5), new Vec3f(0, 0, 0));
        final var view = new PerspectiveView((float) ((70f * Math.PI) / 100f), 1.5f);
        final var box = new ClippingBox(0.1f, 10);
        return new PerspectiveCamera(orientation, view, box);
    }

    // FIXME don't increment the number on unsuccessful rename
    private String name() {
        return String.format("%s %d", DEFAULT_NAME, namePostfix++);
    }
}