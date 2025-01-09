package io.github.nonmilk.coffee;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
    private void initialize() {
        scenesController.setRenderer(viewController.renderer());
        scenesController.setCamerer(camerasController);
        scenesController.setModeler(modelsController);

        camerasController.controller().setView(viewController.view());

        viewController.setCamerer(camerasController);
    }

    public void init(final Stage s) {
        if (initialized) {
            throw new IllegalStateException("initialized more than once");
        }

        modelsController.init(s);

        initWireframe();
        initTexture();
        initLighting();
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
    }
}
