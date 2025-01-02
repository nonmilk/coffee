package io.github.nonmilk.coffee;

import javafx.fxml.FXML;

public final class Grinder {

    @FXML
    private Viewer viewController;

    @FXML
    private Scener sceneController;

    @FXML
    private Camerer cameraController;

    @FXML
    private void initialize() {
        sceneController.setRenderer(viewController.renderer());
        sceneController.setCamerer(cameraController);
    }
}
