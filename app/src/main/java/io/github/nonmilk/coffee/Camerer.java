package io.github.nonmilk.coffee;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private Button addPerspectiveBtn;

    @FXML
    private Button addOrthographicBtn;

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
    private Button orientationApplyBtn;

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
    private Button viewApplyBtn;

    @FXML
    private TextField boxNearPlaneField;

    @FXML
    private TextField boxFarPlaneField;

    @FXML
    private Button boxApplyBtn;

    @FXML
    private void initialize() {
        view.setItems(list);

        cameraController.setCamerer(this);

        arField.disableProperty().set(true);
        widthField.disableProperty().set(true);
        heightField.disableProperty().set(true);

        initStack();
        initAdd();
        initRemove();
        initMarkActive();
        initRename();
        initApplyOrientation();
        initApplyView();
        initApplyBox();
    }

    public void setScene(final Scene s) {
        scene = Objects.requireNonNull(s);

        final var cameras = scenes.get(scene);
        if (cameras != null) {
            this.cameras = cameras;

            list.clear();
            list.addAll(cameras.values());

            markActive(activeCameras.get(scene).name());
            return;
        }

        this.cameras = new HashMap<>();
        scenes.put(scene, this.cameras);

        final String name = uniqueName();

        final var camera = scene.camera();
        if (camera != null) {
            add(camera, name);
        } else {
            add(defaultPerspectiveCamera(), name);
        }

        list.clear();
        list.addAll(this.cameras.values());

        markActive(name);
    }

    public CameraController controller() {
        return cameraController;
    }

    private void initStack() {
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

    private void initAdd() {
        addPerspectiveBtn.setOnAction(e -> {
            final var name = uniqueName();
            add(defaultPerspectiveCamera(), name);
            markActive(name);
        });

        addOrthographicBtn.setOnAction(e -> {
            final var name = uniqueName();
            add(defaultOrthographicCamera(), name);
            markActive(name);
        });
    }

    private void add(final Camera c, final String name) {
        if (cameras.get(name) != null) {
            throw new IllegalArgumentException("this name already exists");
        }

        cameras.put(name, new NamedCamera(c, name));
        list.add(cameras.get(name));
        view.refresh();
    }

    private void initRemove() {
        removeBtn.setOnAction(e -> {
            final var selection = view.selectionModelProperty().get();

            final var cam = selection.getSelectedItem();
            if (cam == null) {
                return;
            }

            remove(cam.name());
            list.remove(selection.getSelectedIndex());
            view.refresh();
        });
    }

    private void remove(final String name) {
        final var camera = cameras.get(name);

        if (camera == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        } else if (camera == active) {
            throw new IllegalArgumentException("cannot remove active camera");
        }

        cameras.remove(name);
        // removal from list should be handled by the button
    }

    private void initRename() {
        final var dialog = new TextInputDialog();
        final var field = dialog.getEditor();

        renameBtn.setOnAction(e -> {
            final var cam = selected();
            if (cam == null) {
                return;
            }

            field.setText(cam.name());

            dialog.showAndWait().ifPresent(response -> {
                try {
                    rename(cam.name(), response);
                } catch (final IllegalArgumentException err) {
                    return;
                }
            });
        });
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

        view.refresh();
    }

    private void initMarkActive() {
        markActiveBtn.setOnAction(e -> {
            final var cam = selected();
            if (cam == null) {
                return;
            }

            markActive(cam.name());
        });
    }

    private void markActive(final String name) {
        final var camera = cameras.get(name);
        if (camera == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        if (active != null) {
            active.setStatus(NamedCamera.Status.DEFAULT);
        }

        active = camera;
        active.setStatus(NamedCamera.Status.ACTIVE);

        activeCameras.put(scene, active);
        scene.setCamera(active());
        controller().setCamera(active.unwrap());

        view.getSelectionModel().select(active);
        updateFields();

        view.refresh();
    }

    private Camera active() {
        return active.unwrap();
    }

    public void updateOrientation() {
        final var cam = active();
        final var orientation = cam.orientation();
        final var pos = orientation.position();
        final var target = orientation.target();

        positionXField.setText(String.valueOf(pos.x()));
        positionYField.setText(String.valueOf(pos.y()));
        positionZField.setText(String.valueOf(pos.z()));

        targetXField.setText(String.valueOf(target.x()));
        targetYField.setText(String.valueOf(target.y()));
        targetZField.setText(String.valueOf(target.z()));
    }

    public void updateView() {
        final var cam = active();

        if (cam instanceof OrthographicCamera orthographic) {
            final var view = orthographic.view();

            perspectiveBtn.disableProperty().set(true);
            orthographicBtn.disableProperty().set(false);
            orthographicBtn.fire();

            widthField.setText(String.valueOf(view.width()));
            heightField.setText(String.valueOf(view.height()));

            arField.clear();
            fovField.clear();
        } else if (cam instanceof PerspectiveCamera perspective) {
            final var view = perspective.view();

            orthographicBtn.disableProperty().set(true);
            perspectiveBtn.disableProperty().set(false);
            perspectiveBtn.fire();

            arField.setText(String.valueOf(view.aspectRatio()));
            fovField.setText(String.valueOf(view.fov()));

            widthField.clear();
            heightField.clear();
        }
    }

    public void updateBox() {
        final var cam = active();
        final var box = cam.box();

        boxNearPlaneField.setText(String.valueOf(box.nearPlane()));
        boxFarPlaneField.setText(String.valueOf(box.farPlane()));
    }

    public void updateFields() {
        updateOrientation();
        updateView();
        updateBox();
    }

    private void initApplyOrientation() {
        orientationApplyBtn.setOnAction(e -> {
            final var cam = selected();
            if (cam == null) {
                return;
            }

            final var name = cam.name();

            setPositionFromFields(name);
            setTargetFromFields(name);

            updateOrientation();
        });
    }

    private void initApplyBox() {
        boxApplyBtn.setOnAction(e -> {
            final var cam = selected();
            if (cam == null) {
                return;
            }

            setBoxFromFields(cam.name());

            updateBox();
        });
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

    private void initApplyView() {
        viewApplyBtn.setOnAction(e -> {
            final var cam = selected();
            if (cam == null) {
                return;
            }

            final var unwrapped = cam.unwrap();
            final var name = cam.name();

            if (unwrapped instanceof PerspectiveCamera) {
                setPerspectiveViewFromFields(name);
            } else if (unwrapped instanceof OrthographicCamera) {
                setOrthographicViewFromFields(name);
            }

            updateView();
        });
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

    private void setBoxFromFields(final String name) {
        final var nearPlaneText = boxNearPlaneField.getText();
        final float nearPlane;
        if (nearPlaneText.isEmpty()) {
            nearPlane = DEFAULT_NEAR_PLANE;
        } else {
            nearPlane = Float.parseFloat(nearPlaneText);
        }

        final var farPlaneText = boxFarPlaneField.getText();
        final float farPlane;
        if (farPlaneText.isEmpty()) {
            farPlane = DEFAULT_FAR_PLANE;
        } else {
            farPlane = Float.parseFloat(farPlaneText);
        }

        setBox(name, nearPlane, farPlane);
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
            // FIXME it works but why
            perspective.view().setAspectRatio(height / width);
        } else if (cam instanceof OrthographicCamera orthographic) {
            orthographic.view().setWidth(width);
            orthographic.view().setHeight(height);
        }

        updateFields();
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

    private PerspectiveView defaultPerspectiveView() {
        return new PerspectiveView(DEFAULT_VIEW_FOV, DEFAULT_VIEW_AR);
    }

    private OrthographicView defaultOrthographicView() {
        return new OrthographicView(DEFAULT_VIEW_WIDTH, DEFAULT_VIEW_HEIGHT);
    }

    private ClippingBox defaultBox() {
        return new ClippingBox(DEFAULT_NEAR_PLANE, DEFAULT_FAR_PLANE);
    }

    private Camera defaultPerspectiveCamera() {
        return new PerspectiveCamera(
                defaultOrientation(), defaultPerspectiveView(), defaultBox());
    }

    private Camera defaultOrthographicCamera() {
        return new OrthographicCamera(
                defaultOrientation(), defaultOrthographicView(), defaultBox());
    }

    private NamedCamera selected() {
        return view.getSelectionModel().getSelectedItem();
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
