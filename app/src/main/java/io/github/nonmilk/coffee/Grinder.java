package io.github.nonmilk.coffee;

import javafx.fxml.FXML;
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
    }
}
