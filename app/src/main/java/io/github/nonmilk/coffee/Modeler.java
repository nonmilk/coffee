package io.github.nonmilk.coffee;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.nonmilk.coffee.grinder.Model;
import io.github.nonmilk.coffee.grinder.render.Scene;
import io.github.shimeoki.jshaper.ObjFile;
import io.github.shimeoki.jshaper.ShaperError;
import io.github.shimeoki.jshaper.obj.ModelReader;
import io.github.shimeoki.jshaper.obj.ModelWriter;
import io.github.shimeoki.jshaper.obj.Reader;
import io.github.shimeoki.jshaper.obj.Writer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public final class Modeler {

    private final StringBuilder nameBuilder = new StringBuilder();

    private Stage stage;
    private boolean initialized = false;

    private FileChooser chooser;

    private final Reader reader = new ModelReader();
    private final Writer writer = new ModelWriter();

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
        initImport();

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

    private void initImport() {
        importBtn.setOnAction(e -> {
            final var file = chooser.showOpenDialog(stage);

            if (file == null) {
                return;
            }

            importObj(file);
        });
    }

    private void importObj(final File f) {
        final ObjFile obj;
        try {
            obj = reader.read(f);
        } catch (final ShaperError err) {
            // TODO handle
            return;
        }

        final var model = new Model(obj, null); // TODO texture?

        scene.models().add(model);

        final var name = uniqueName(f.getName());
        models.put(name, new NamedModel(model, name));

        list.add(models.get(name));
        view.refresh();
    }

    private String uniqueName(String name) {
        if (models.get(name) == null) {
            return name;
        }

        nameBuilder.setLength(0);
        nameBuilder.append(name);
        nameBuilder.append(' ');

        int postfix = 1;
        nameBuilder.append(postfix++);
        name = nameBuilder.toString();

        while (models.get(name) != null) {
            nameBuilder.setLength(nameBuilder.length() - 1);
            nameBuilder.append(postfix++);
            name = nameBuilder.toString();
        }

        return name;
    }
}
