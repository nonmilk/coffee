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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

// TODO selected camera edit

public final class Camerer {

    private static final float DEFAULT_POSITION_X = 5;
    private static final float DEFAULT_POSITION_Y = 0;
    private static final float DEFAULT_POSITION_Z = -5;

    private static final float DEFAULT_TARGET_X = 0;
    private static final float DEFAULT_TARGET_Y = 0;
    private static final float DEFAULT_TARGET_Z = 0;

    private static final float DEFAULT_VIEW_FOV = 0.45f * 3.14f;
    private static final float DEFAULT_VIEW_AR = 1.67f;

    private static final float DEFAULT_VIEW_WIDTH = 1280;
    private static final float DEFAULT_VIEW_HEIGHT = 720;

    private static final float DEFAULT_NEAR_PLANE = 0.1f;
    private static final float DEFAULT_FAR_PLANE = 10;

    private static final String DEFAULT_NAME = "Camera";
    private final StringBuilder nameBuilder = new StringBuilder();

    private Scene scene;

    private final Map<Scene, Map<String, NamedCamera>> scenes = new HashMap<>();
    private final Map<Scene, NamedCamera> activeCameras = new HashMap<>();

    private Map<String, NamedCamera> cameras = new HashMap<>();
    private NamedCamera active;

    private final CameraController cameraController = new CameraController();

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
    private Button markActiveBtn;

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
    private Pane orthographicViewPane;

    @FXML
    private TextField widthField;

    @FXML
    private TextField heightField;

    @FXML
    private Pane perspectiveViewPane;

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

        markActiveBtn.setOnAction(e -> {
            final var selection = view.selectionModelProperty().get();
            select(selection.getSelectedItem().name());
        });

        addBtn.setOnAction(e -> {
            final var name = uniqueName();

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

        final String name = uniqueName();

        final var camera = scene.camera();
        if (camera != null) {
            add(camera, name);
        } else {
            add(defaultCamera(), name);
        }

        list.clear();
        list.addAll(this.cameras.values());
        // TODO update visual selection

        select(name);
    }

    public CameraController controller() {
        return cameraController;
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

        this.controller().setCamera(active.unwrap());

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

    private void setPositionFromFields(final String name) {
        final var textX = positionXField.getText();
        final float x;
        if (textX.isEmpty()) {
            x = DEFAULT_POSITION_X;
        } else {
            x = Float.parseFloat(textX);
        }

        final var textY = positionYField.getText();
        final float y;
        if (textY.isEmpty()) {
            y = DEFAULT_POSITION_Y;
        } else {
            y = Float.parseFloat(textY);
        }

        final var textZ = positionZField.getText();
        final float z;
        if (textZ.isEmpty()) {
            z = DEFAULT_POSITION_Z;
        } else {
            z = Float.parseFloat(textZ);
        }

        setPosition(name, x, y, z);
    }

    private void setPosition(final String name,
            final float x, final float y, final float z) {

        final var cam = cameras.get(name);
        if (cam == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        final var pos = cam.unwrap().orientation().position();

        pos.setX(x);
        pos.setY(y);
        pos.setZ(z);
    }

    private void setTargetFromFields(final String name) {
        final var textX = targetXField.getText();
        final float x;
        if (textX.isEmpty()) {
            x = DEFAULT_TARGET_X;
        } else {
            x = Float.parseFloat(textX);
        }

        final var textY = targetYField.getText();
        final float y;
        if (textY.isEmpty()) {
            y = DEFAULT_TARGET_Y;
        } else {
            y = Float.parseFloat(textY);
        }

        final var textZ = targetZField.getText();
        final float z;
        if (textZ.isEmpty()) {
            z = DEFAULT_TARGET_Z;
        } else {
            z = Float.parseFloat(textZ);
        }

        setTarget(name, x, y, z);
    }

    private void setTarget(final String name,
            final float x, final float y, final float z) {

        final var cam = cameras.get(name);
        if (cam == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        final var target = cam.unwrap().orientation().target();

        target.setX(x);
        target.setY(y);
        target.setZ(z);
    }

    private void setPerspectiveViewFromFields(final String name) {
        final var fovText = fovField.getText();
        final float fov;
        if (fovText.isEmpty()) {
            fov = DEFAULT_VIEW_FOV;
        } else {
            fov = Float.parseFloat(fovText);
        }

        final var arText = arField.getText();
        final float ar;
        if (arText.isEmpty()) {
            ar = DEFAULT_VIEW_AR;
        } else {
            ar = Float.parseFloat(arText);
        }

        setPerspectiveView(name, fov, ar);
    }

    private void setPerspectiveView(final String name,
            final float fov, final float ar) {

        final var cam = cameras.get(name);
        if (cam == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        final var unwrapped = cam.unwrap();
        if (!(unwrapped instanceof PerspectiveCamera)) {
            throw new IllegalArgumentException("this camera is not perspective");
        }

        final var perspective = (PerspectiveCamera) unwrapped;
        final var view = perspective.view();

        view.setFOV(fov);
        view.setAspectRatio(ar);
    }

    private void setOrthographicViewFromFields(final String name) {
        final var widthText = widthField.getText();
        final float width;
        if (widthText.isEmpty()) {
            width = DEFAULT_VIEW_WIDTH;
        } else {
            width = Float.parseFloat(widthText);
        }

        final var heightText = heightField.getText();
        final float height;
        if (heightText.isEmpty()) {
            height = DEFAULT_VIEW_HEIGHT;
        } else {
            height = Float.parseFloat(heightText);
        }

        setOrthographicView(name, width, height);
    }

    private void setOrthographicView(final String name,
            final float width, final float height) {

        final var cam = cameras.get(name);
        if (cam == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        final var unwrapped = cam.unwrap();
        if (!(unwrapped instanceof OrthographicCamera)) {
            throw new IllegalArgumentException("this camera is not orthographic");
        }

        final var orthographic = (OrthographicCamera) unwrapped;
        final var view = orthographic.view();

        view.setWidth(width);
        view.setHeight(height);
    }

    private void setBox(final String name,
            final float nearPlane, final float farPlane) {

        final var cam = cameras.get(name);
        if (cam == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        final var box = cam.unwrap().box();

        box.setNearPlane(nearPlane);
        box.setFarPlane(farPlane);
    }

    public void update(final float width, final float height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        final var cam = active();

        if (cam instanceof PerspectiveCamera perspective) {
            // FIXME
            perspective.view().setAspectRatio(height / width);
        } else if (cam instanceof OrthographicCamera orthographic) {
            orthographic.view().setWidth(width);
            orthographic.view().setHeight(height);
        }
    }

    private Orientation defaultOrientation() {
        return new Orientation(
                new Vec3f(
                        DEFAULT_POSITION_X,
                        DEFAULT_POSITION_Y,
                        DEFAULT_POSITION_Z),
                new Vec3f(
                        DEFAULT_TARGET_X,
                        DEFAULT_TARGET_Y,
                        DEFAULT_TARGET_Z));
    }

    private PerspectiveView defaultView() {
        return new PerspectiveView(DEFAULT_VIEW_FOV, DEFAULT_VIEW_AR);
    }

    private ClippingBox defaultBox() {
        return new ClippingBox(DEFAULT_NEAR_PLANE, DEFAULT_FAR_PLANE);
    }

    private Camera defaultCamera() {
        // return new OrthographicCamera(defaultOrientation(), new
        // OrthographicView(1080, 720), defaultBox());
        return new PerspectiveCamera(
                defaultOrientation(), defaultView(), defaultBox());
    }

    private String uniqueName() {
        var name = DEFAULT_NAME;

        if (cameras.get(name) == null) {
            return name;
        }

        nameBuilder.setLength(0);
        nameBuilder.append(name);
        nameBuilder.append(' ');

        int postfix = 1;
        nameBuilder.append(postfix++);
        name = nameBuilder.toString();

        while (cameras.get(name) != null) {
            nameBuilder.setLength(nameBuilder.length() - 1);
            nameBuilder.append(postfix++);
            name = nameBuilder.toString();
        }

        return name;
    }
}
