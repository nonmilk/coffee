package io.github.nonmilk.coffee;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.nonmilk.coffee.grinder.Model;
import io.github.nonmilk.coffee.grinder.render.ColorTexture;
import io.github.nonmilk.coffee.grinder.render.ImageTexture;
import io.github.nonmilk.coffee.grinder.render.Scene;
import io.github.nonmilk.coffee.grinder.render.Texture;
import io.github.shimeoki.jfx.rasterization.HTMLColorf;
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
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

// TODO handle external changes

public final class Modeler {

    private static final Texture DEFAULT_TEXTURE = new ColorTexture(
            HTMLColorf.BLACK);

    private static final String DEFAULT_NAME = "Model";
    private final StringBuilder nameBuilder = new StringBuilder();

    private String renaming;

    private Stage stage;
    private boolean initialized = false;

    private FileChooser modelChooser;
    private FileChooser textureChooser;

    private final Reader reader = new ModelReader();
    private final Writer writer = new ModelWriter();

    private Scene scene;

    private final Map<Scene, Map<String, NamedModel>> scenes = new HashMap<>();
    private final Map<Scene, NamedModel> active = new HashMap<>();

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
    private Button textureAddBtn;

    @FXML
    private Button textureRemoveBtn;

    @FXML
    private Button activeAddBtn;

    @FXML
    private Button activeRemoveBtn;

    @FXML
    private Button hideBtn;

    @FXML
    private Button unhideBtn;

    @FXML
    private void initialize() {
        view.setItems(list);
    }

    public void init(final Stage s) {
        if (initialized) {
            throw new IllegalStateException("initialized more than once");
        }

        stage = Objects.requireNonNull(s); // jfx why

        initModelChooser();
        initTextureChooser();

        initImport();
        initExport();
        initRename();
        initRemove();

        initAddTexture();
        initRemoveTexture();

        initMarkActive();
        initUnmarkActive();

        initHide();
        initUnhide();

        initialized = true;
    }

    public void setScene(final Scene s) {
        scene = Objects.requireNonNull(s);
        final var models = scenes.get(scene);

        if (models != null) {
            this.models = models;
            updateList();
            return;
        }

        this.models = new HashMap<>();
        scenes.put(scene, this.models);

        String name;
        for (final Model m : scene.models()) {
            name = uniqueName(DEFAULT_NAME);
            this.models.put(name, new NamedModel(m, name));
        }

        updateList();
    }

    private void updateList() {
        list.clear();
        list.addAll(models.values());
        view.refresh();
    }

    private void initModelChooser() {
        final var chooser = new FileChooser();

        chooser.setTitle("Import Obj File");
        chooser.getExtensionFilters().addAll(
                new ExtensionFilter("Obj Files", "*.obj"),
                new ExtensionFilter("All Files", "*.*"));

        this.modelChooser = chooser;
    }

    private void initImport() {
        importBtn.setOnAction(e -> {
            final var file = modelChooser.showOpenDialog(stage);

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

        final var model = new Model(obj, DEFAULT_TEXTURE);

        scene.models().add(model);

        final var name = uniqueName(f.getName());
        models.put(name, new NamedModel(model, name));

        list.add(models.get(name));
        view.refresh();
    }

    private void initExport() {
        exportBtn.setOnAction(e -> {
            final var selected = selected();
            if (selected == null) {
                return;
            }

            modelChooser.setInitialFileName(selected.name());

            final var file = modelChooser.showSaveDialog(stage);

            if (file == null) {
                return;
            }

            exportObj(selected.unwrap().obj(), file);
        });
    }

    private void exportObj(final ObjFile src, final File dst) {
        try {
            writer.write(src, dst);
        } catch (final ShaperError err) {
            // TODO handle
            return;
        }
    }

    private void initRename() {
        final var dialog = new TextInputDialog();
        final var field = dialog.getEditor();

        renameBtn.setOnAction(e -> {
            final var selected = selected();
            if (selected == null) {
                return;
            }

            renaming = selected.name();

            field.setText(renaming);
            dialog.show();
        });

        // FIXME ignore rename on closing with cancel
        dialog.setOnCloseRequest(e -> {
            try {
                rename(renaming, field.getText());
            } catch (IllegalArgumentException err) {
                e.consume();
                // TODO error
            }
        });

        dialog.setOnHidden(e -> {
            view.refresh();
        });
    }

    private void rename(final String oldName, final String newName) {
        final var model = models.get(oldName);
        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        if (models.get(newName) != null) {
            throw new IllegalArgumentException("this name already exists");
        }

        models.remove(oldName);
        model.rename(newName);
        models.put(newName, model);
    }

    private void initRemove() {
        removeBtn.setOnAction(e -> {
            final var selection = view.selectionModelProperty().get();

            final var model = selection.getSelectedItem();
            if (model == null) {
                return;
            }

            remove(model.name());
            list.remove(selection.getSelectedIndex());

            view.refresh();
        });
    }

    private void remove(final String name) {
        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        models.remove(name);
        scene.models().remove(model.unwrap());

        // removing from the view should be handled by the button
    }

    private void initTextureChooser() {
        final var chooser = new FileChooser();

        chooser.setTitle("Import Texture");
        chooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg"),
                new ExtensionFilter("All Files", "*.*"));

        this.textureChooser = chooser;
    }

    private void initAddTexture() {
        textureAddBtn.setOnAction(e -> {
            final var selection = view.selectionModelProperty().get();

            final var model = selection.getSelectedItem();
            if (model == null) {
                return;
            }

            final var file = textureChooser.showOpenDialog(stage);
            if (file == null) {
                return;
            }

            addTexture(model.name(), ImageTexture.fromFile(file));
        });
    }

    private void addTexture(final String name, final Texture t) {
        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        model.unwrap().setTexture(Objects.requireNonNull(t));
    }

    private void initRemoveTexture() {
        textureRemoveBtn.setOnAction(e -> {
            final var selection = view.selectionModelProperty().get();

            final var model = selection.getSelectedItem();
            if (model == null) {
                return;
            }

            removeTexture(model.name());
        });
    }

    private void removeTexture(final String name) {
        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        model.unwrap().setTexture(DEFAULT_TEXTURE);
    }

    private void initMarkActive() {
        activeAddBtn.setOnAction(e -> {
            final var model = selected();
            if (model == null) {
                return;
            }

            markActive(model.name());
        });
    }

    private void markActive(final String name) {
        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        if (model.status() == NamedModel.Status.ACTIVE) {
            return;
        }

        unhide(name);

        final var active = this.active.get(scene);

        if (active != null) {
            active.setStatus(NamedModel.Status.DEFAULT);
        }

        model.setStatus(NamedModel.Status.ACTIVE);
        this.active.put(scene, model);

        view.refresh();
    }

    private void initUnmarkActive() {
        activeRemoveBtn.setOnAction(e -> {
            final var model = selected();
            if (model == null) {
                return;
            }

            unmarkActive(model.name());
        });
    }

    private void unmarkActive(final String name) {
        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        if (model.status() != NamedModel.Status.ACTIVE) {
            return;
        }

        model.setStatus(NamedModel.Status.DEFAULT);

        final var active = this.active.get(scene);

        if (active == null) {
            return;
        }

        if (active != model) {
            return;
        }

        active.setStatus(NamedModel.Status.DEFAULT);
        this.active.put(scene, null);

        view.refresh();
    }

    private void initHide() {
        hideBtn.setOnAction(e -> {
            final var model = selected();
            if (model == null) {
                return;
            }

            hide(model.name());
        });
    }

    private void hide(final String name) {
        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        if (model.status() == NamedModel.Status.HIDDEN) {
            return;
        }

        unmarkActive(name);

        model.setStatus(NamedModel.Status.HIDDEN);
        scene.models().remove(model.unwrap());

        view.refresh();
    }

    private void initUnhide() {
        unhideBtn.setOnAction(e -> {
            final var model = selected();
            if (model == null) {
                return;
            }

            unhide(model.name());
        });
    }

    private void unhide(final String name) {
        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        if (model.status() != NamedModel.Status.HIDDEN) {
            return;
        }

        model.setStatus(NamedModel.Status.DEFAULT);
        scene.models().add(model.unwrap());

        view.refresh();
    }

    private void scale(final String name,
            final float x, final float y, final float z) {

        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        model.unwrap().transformer().setScaling(x, y, z);
    }

    private void translate(final String name,
            final float x, final float y, final float z) {

        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        model.unwrap().transformer().setTranslation(x, y, z);
    }

    private NamedModel selected() {
        return view.selectionModelProperty().get().getSelectedItem();
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
