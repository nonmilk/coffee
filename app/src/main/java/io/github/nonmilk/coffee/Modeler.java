package io.github.nonmilk.coffee;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.nonmilk.coffee.grinder.render.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public final class Modeler {

    private Stage stage;
    private boolean initialized = false;

    private FileChooser chooser;

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

    public void init(final Stage s) {
        if (initialized) {
            throw new IllegalStateException("initialized more than once");
        }

        stage = Objects.requireNonNull(s); // jfx why

        // TODO inits
        initChooser();

        initialized = true;
    }

    private void initChooser() {
        final var chooser = new FileChooser();

        chooser.setTitle("Import Obj File");
        chooser.getExtensionFilters().addAll(
                new ExtensionFilter("Obj Files", "*.obj"),
                new ExtensionFilter("All Files", "*.*"));

        this.chooser = chooser;
    }
}
