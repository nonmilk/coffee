package io.github.nonmilk.coffee;

import java.util.HashMap;
import java.util.Map;

import io.github.nonmilk.coffee.grinder.render.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public final class Modeler {

    private Scene scene;

    private final Map<Scene, Map<String, NamedModel>> scenes = new HashMap<>();

    private Map<String, NamedModel> models;

    @FXML
    private ListView<NamedModel> view;
    private final ObservableList<NamedModel> list = FXCollections.observableArrayList();

    @FXML
    private Button importBtn;

    @FXML
    private Button exportBtn;

    @FXML
    private Button removeBtn;

    @FXML
    private Button renameBtn;

    @FXML
    private void initialize() {
        view.setItems(list);
    }
}
