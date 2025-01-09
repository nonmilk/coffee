package io.github.nonmilk.coffee;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public final class Grinder {

    private boolean initialized = false;

    @FXML
    private Viewer viewController;

    @FXML
    private Scener scenesController;

    @FXML
    private Camerer camerasController;

    @FXML
    private Modeler modelsController;

    @FXML
    private CheckBox wireframeCheck;

    @FXML
    private CheckBox textureCheck;

    @FXML
    private CheckBox lightingCheck;

    @FXML
    private TextField fpsField;

    @FXML
    private Button fpsApplyBtn;

    @FXML
    private TextField mouseSensField;

    @FXML
    private Button mouseSensApplyBtn;

    @FXML
    private TextField scrollSensField;

    @FXML
    private Button scrollSensApplyBtn;

    @FXML
    private void initialize() {
        scenesController.setRenderer(viewController.renderer());
        scenesController.setCamerer(camerasController);
        scenesController.setModeler(modelsController);

        camerasController.controller().setView(viewController.view());

        viewController.setCamerer(camerasController);
        viewController.setModeler(modelsController);
    }

    public void init(final Stage s) {
        if (initialized) {
            throw new IllegalStateException("initialized more than once");
        }

        modelsController.init(s);

        initWireframe();
        initTexture();
        initLighting();
        initFPS();
        initMouseSens();
        initScrollSens();
    }

    public void start() {
        viewController.start();
    }

    public void stop() {
        viewController.stop();
    }

    private void initWireframe() {
        wireframeCheck.setIndeterminate(false);
        final var renderer = viewController.renderer();

        wireframeCheck.setOnAction(e -> {
            if (wireframeCheck.isSelected()) {
                renderer.setDrawWireframe(true);
            } else {
                renderer.setDrawWireframe(false);
            }
        });

        wireframeCheck.fire();
    }

    private void initTexture() {
        textureCheck.setIndeterminate(false);
        final var renderer = viewController.renderer();

        textureCheck.setOnAction(e -> {
            if (textureCheck.isSelected()) {
                renderer.setDrawTexture(true);
            } else {
                renderer.setDrawTexture(false);
            }
        });

        textureCheck.fire();
    }

    private void initLighting() {
        lightingCheck.setIndeterminate(false);
        final var renderer = viewController.renderer();

        lightingCheck.setOnAction(e -> {
            if (lightingCheck.isSelected()) {
                renderer.setDrawLighting(true);
            } else {
                renderer.setDrawLighting(false);
            }
        });

        lightingCheck.fire();
    }

    private void initFPS() {
        fpsApplyBtn.setOnAction(e -> {
            if (fpsField.getText().isEmpty()) {
                fpsField.setText(String.valueOf(Viewer.DEFAULT_FPS));
            }

            final var fps = Integer.parseInt(fpsField.getText());
            viewController.setFPS(fps);

            fpsField.setText(String.valueOf(viewController.fps()));
        });

        fpsApplyBtn.fire();
    }

    private void initMouseSens() {
        final var senser = camerasController.controller();

        mouseSensApplyBtn.setOnAction(e -> {
            if (mouseSensField.getText().isEmpty()) {
                mouseSensField.setText(String.valueOf(
                        CameraController.DEFAULT_OVERALL_SENSITIVITY));
            }

            final var sens = Float.parseFloat(mouseSensField.getText());
            senser.setMouseSensitivity(sens);

            mouseSensField.setText(String.valueOf(senser.getMouseSensitivity()));
        });

        mouseSensApplyBtn.fire();
    }

    private void initScrollSens() {
        final var senser = camerasController.controller();

        scrollSensApplyBtn.setOnAction(e -> {
            if (scrollSensField.getText().isEmpty()) {
                scrollSensField.setText(String.valueOf(
                        CameraController.DEFAULT_OVERALL_SENSITIVITY));
            }

            final var sens = Float.parseFloat(scrollSensField.getText());
            senser.setScrollSensitivity(sens);

            scrollSensField.setText(String.valueOf(senser.getScrollSensitivity()));
        });

        scrollSensApplyBtn.fire();
    }
}
