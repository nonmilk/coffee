package io.github.nonmilk.coffee;

import javafx.fxml.FXML;

public final class Grinder {

    @FXML
    private Viewer viewController;

    @FXML
    private Scener scenesController;

    @FXML
    private Camerer camerasController;

    @FXML
    private void initialize() {
        scenesController.setRenderer(viewController.renderer());
        scenesController.setCamerer(camerasController);
    }
}
