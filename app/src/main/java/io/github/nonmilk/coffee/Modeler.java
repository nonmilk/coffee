package io.github.nonmilk.coffee;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.github.nonmilk.coffee.grinder.Model;
import io.github.nonmilk.coffee.grinder.math.affine.Rotator.Axis;
import io.github.nonmilk.coffee.grinder.render.ColorTexture;
import io.github.nonmilk.coffee.grinder.render.ImageTexture;
import io.github.nonmilk.coffee.grinder.render.Scene;
import io.github.nonmilk.coffee.grinder.render.Texture;
import io.github.nonmilk.coffee.grinder.transformations.ModelTransformer;
import io.github.shimeoki.jfx.rasterization.HTMLColorf;
import io.github.shimeoki.jshaper.ObjFile;
import io.github.shimeoki.jshaper.ShaperError;
import io.github.shimeoki.jshaper.obj.ModelReader;
import io.github.shimeoki.jshaper.obj.ModelWriter;
import io.github.shimeoki.jshaper.obj.Reader;
import io.github.shimeoki.jshaper.obj.Triplet;
import io.github.shimeoki.jshaper.obj.Writer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

// TODO handle external changes

public final class Modeler {

    private static final File INITIAL_DIRECTORY = new File("../assets/");

    private static final Texture DEFAULT_TEXTURE = new ColorTexture(
            HTMLColorf.BLACK);

    private static final String DEFAULT_NAME = "Model";
    private final StringBuilder nameBuilder = new StringBuilder();

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

    private static final float KEYBOARD_ROTATE_ANGLE_DELTA = (float) Math.toRadians(6);
    private static final float KEYBOARD_TRANSLATE_DELTA = 0.5f;
    private static final float KEYBOARD_SCALE_DELTA = 0.1f;
    private Axis modificationAxis = Axis.X;

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
    private TextField scalingXField;

    @FXML
    private TextField scalingYField;

    @FXML
    private TextField scalingZField;

    @FXML
    private Button scalingApplyBtn;

    @FXML
    private Button scalingResetBtn;

    @FXML
    private TextField translationXField;

    @FXML
    private TextField translationYField;

    @FXML
    private TextField translationZField;

    @FXML
    private Button translationApplyBtn;

    @FXML
    private Button translationResetBtn;

    @FXML
    private TextField rotationXField;

    @FXML
    private TextField rotationYField;

    @FXML
    private TextField rotationZField;

    @FXML
    private Button rotationApplyBtn;

    @FXML
    private Button rotationResetBtn;

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

        initScale();
        initTranslate();
        initRotate();

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
            if (INITIAL_DIRECTORY.isDirectory()) {
                modelChooser.setInitialDirectory(INITIAL_DIRECTORY);
            }

            final var file = modelChooser.showOpenDialog(stage);

            if (file == null) {
                return;
            }

            importObj(file);

            view.selectionModelProperty().get().selectLast();
            markActive(selected().name());
        });
    }

    private void importObj(final File f) {
        final ObjFile obj;
        try {
            obj = reader.read(f);
        } catch (final ShaperError e) {
            throw new CoffeeError(e);
        }

        if (obj.elements().faces().size() == 0) {
            throw new CoffeeError("model has no polygons or is not valid");
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

            if (INITIAL_DIRECTORY.isDirectory()) {
                modelChooser.setInitialDirectory(INITIAL_DIRECTORY);
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
        } catch (final ShaperError e) {
            throw new CoffeeError(e);
        }
    }

    private void initRename() {
        final var dialog = new TextInputDialog();
        final var field = dialog.getEditor();

        renameBtn.setOnAction(e -> {
            final var model = selected();
            if (model == null) {
                return;
            }

            field.setText(model.name());

            dialog.showAndWait().ifPresent(response -> {
                try {
                    rename(model.name(), response);
                } catch (final IllegalArgumentException err) {
                    return;
                }
            });
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

        view.refresh();
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

        unmarkActive(name);

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

            if (INITIAL_DIRECTORY.isDirectory()) {
                textureChooser.setInitialDirectory(INITIAL_DIRECTORY);
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
        updateFields();
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

    private void initScale() {
        scalingApplyBtn.setOnAction(e -> {
            final var model = active.get(scene);
            if (model == null) {
                return;
            }

            scaleFromFields(model.name());
            updateScaling();
        });

        scalingResetBtn.setOnAction(e -> {
            final var model = active.get(scene);
            if (model == null) {
                return;
            }

            resetScale(model.name());
            updateScaling();
        });
    }

    private void scaleFromFields(final String name) {
        final var textX = scalingXField.getText();
        final float x;
        if (textX.isEmpty()) {
            x = 1;
        } else {
            x = Float.parseFloat(textX);
        }

        final var textY = scalingYField.getText();
        final float y;
        if (textY.isEmpty()) {
            y = 1;
        } else {
            y = Float.parseFloat(textY);
        }

        final var textZ = scalingZField.getText();
        final float z;
        if (textZ.isEmpty()) {
            z = 1;
        } else {
            z = Float.parseFloat(textZ);
        }

        scale(name, x, y, z);
    }

    private void scale(final String name,
            final float x, final float y, final float z) {

        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        model.unwrap().transformer().setScaling(x, y, z);
    }

    private void resetScale(final String name) {
        scale(name, 1, 1, 1);
    }

    private void initTranslate() {
        translationApplyBtn.setOnAction(e -> {
            final var model = active.get(scene);
            if (model == null) {
                return;
            }

            translateFromFields(model.name());
            updateTranslation();
        });

        translationResetBtn.setOnAction(e -> {
            final var model = active.get(scene);
            if (model == null) {
                return;
            }

            resetTranslate(model.name());
            updateTranslation();
        });
    }

    private void translateFromFields(final String name) {
        final var textX = translationXField.getText();
        final float x;
        if (textX.isEmpty()) {
            x = 0;
        } else {
            x = Float.parseFloat(textX);
        }

        final var textY = translationYField.getText();
        final float y;
        if (textY.isEmpty()) {
            y = 0;
        } else {
            y = Float.parseFloat(textY);
        }

        final var textZ = translationZField.getText();
        final float z;
        if (textZ.isEmpty()) {
            z = 0;
        } else {
            z = Float.parseFloat(textZ);
        }

        translate(name, x, y, z);
    }

    private void translate(final String name,
            final float x, final float y, final float z) {

        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        model.unwrap().transformer().setTranslation(x, y, z);
    }

    private void resetTranslate(final String name) {
        translate(name, 0, 0, 0);
    }

    private void initRotate() {
        rotationApplyBtn.setOnAction(e -> {
            final var model = active.get(scene);
            if (model == null) {
                return;
            }

            rotateFromFields(model.name());
            updateRotation();
        });

        rotationResetBtn.setOnAction(e -> {
            final var model = active.get(scene);
            if (model == null) {
                return;
            }

            resetRotate(model.name());
            updateRotation();
        });
    }

    private void rotateFromFields(final String name) {
        final var textX = rotationXField.getText();
        final float x;
        if (textX.isEmpty()) {
            x = 0;
        } else {
            x = Float.parseFloat(textX);
        }

        final var textY = rotationYField.getText();
        final float y;
        if (textY.isEmpty()) {
            y = 0;
        } else {
            y = Float.parseFloat(textY);
        }

        final var textZ = rotationZField.getText();
        final float z;
        if (textZ.isEmpty()) {
            z = 0;
        } else {
            z = Float.parseFloat(textZ);
        }

        rotate(name, x, y, z);
    }

    private void rotate(final String name,
            final float x, final float y, final float z) {

        final var model = models.get(name);

        if (model == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        final var transformer = model.unwrap().transformer();

        transformer.setRotationX(x);
        transformer.setRotationY(y);
        transformer.setRotationZ(z);
    }

    private void resetRotate(final String name) {
        rotate(name, 0, 0, 0);
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

    public void removeTriplets(final List<Triplet> triplets) {
        // not efficient, but whatever
        for (final var model : scene.models()) {
            model.removeTriplets(triplets);
        }
    }

    public void handleKeyEvent(final KeyEvent event) {
        switch (event.getCode()) {
            case X -> {
                modificationAxis = Axis.X;
            }
            case Y -> {
                modificationAxis = Axis.Z;
            }
            case Z -> {
                modificationAxis = Axis.Y;
            }
            default -> {
                break;
            }
        }

        final NamedModel model = this.active.get(scene);
        if (model == null) {
            return;
        }

        final ModelTransformer transformer = model.unwrap().transformer();
        switch (event.getCode()) {
            case R -> {
                handleKeyboarRotation(event, transformer);
            }
            case T -> {
                handleKeyboardTranslation(event, transformer);
            }
            case F -> {
                handleKeyboardScaling(event, transformer);
            }
            default -> {
                return;
            }
        }
    }

    private void handleKeyboarRotation(final KeyEvent event, final ModelTransformer transformer) {
        switch (modificationAxis) {
            case X -> {
                if (event.isAltDown()) {
                    transformer.setRotationX(transformer.getRotationX() - KEYBOARD_ROTATE_ANGLE_DELTA);
                } else {
                    transformer.setRotationX(transformer.getRotationX() + KEYBOARD_ROTATE_ANGLE_DELTA);
                }
            }
            case Y -> {
                if (event.isAltDown()) {
                    transformer.setRotationY(transformer.getRotationY() - KEYBOARD_ROTATE_ANGLE_DELTA);
                } else {
                    transformer.setRotationY(transformer.getRotationY() + KEYBOARD_ROTATE_ANGLE_DELTA);
                }
            }
            case Z -> {
                if (event.isAltDown()) {
                    transformer.setRotationZ(transformer.getRotationZ() - KEYBOARD_ROTATE_ANGLE_DELTA);
                } else {
                    transformer.setRotationZ(transformer.getRotationZ() + KEYBOARD_ROTATE_ANGLE_DELTA);
                }
            }
            default -> {
                return;
            }
        }

        updateRotation();
    }

    private void handleKeyboardTranslation(final KeyEvent event, final ModelTransformer transformer) {
        switch (modificationAxis) {
            case X -> {
                if (event.isAltDown()) {
                    transformer.setTranslationX(transformer.getTranslationX() - KEYBOARD_TRANSLATE_DELTA);
                } else {
                    transformer.setTranslationX(transformer.getTranslationX() + KEYBOARD_TRANSLATE_DELTA);
                }
            }
            case Y -> {
                if (event.isAltDown()) {
                    transformer.setTranslationY(transformer.getTranslationY() - KEYBOARD_TRANSLATE_DELTA);
                } else {
                    transformer.setTranslationY(transformer.getTranslationY() + KEYBOARD_TRANSLATE_DELTA);
                }
            }
            case Z -> {
                if (event.isAltDown()) {
                    transformer.setTranslationZ(transformer.getTranslationZ() - KEYBOARD_TRANSLATE_DELTA);
                } else {
                    transformer.setTranslationZ(transformer.getTranslationZ() + KEYBOARD_TRANSLATE_DELTA);
                }
            }
            default -> {
                return;
            }
        }

        updateTranslation();
    }

    private void handleKeyboardScaling(final KeyEvent event, final ModelTransformer transformer) {
        switch (modificationAxis) {
            case X -> {
                if (event.isAltDown()) {
                    transformer.setScalingX(transformer.getScalingX() - KEYBOARD_SCALE_DELTA);
                } else {
                    transformer.setScalingX(transformer.getScalingX() + KEYBOARD_SCALE_DELTA);
                }
            }
            case Y -> {
                if (event.isAltDown()) {
                    transformer.setScalingY(transformer.getScalingY() - KEYBOARD_SCALE_DELTA);
                } else {
                    transformer.setScalingY(transformer.getScalingY() + KEYBOARD_SCALE_DELTA);
                }
            }
            case Z -> {
                if (event.isAltDown()) {
                    transformer.setScalingZ(transformer.getScalingZ() - KEYBOARD_SCALE_DELTA);
                } else {
                    transformer.setScalingZ(transformer.getScalingZ() + KEYBOARD_SCALE_DELTA);
                }
            }
            default -> {
                return;
            }
        }

        updateScaling();
    }

    public void updateScaling() {
        final var model = active.get(scene);
        if (model == null) {
            return;
        }

        final var transformer = model.unwrap().transformer();

        scalingXField.setText(String.valueOf(transformer.getScalingX()));
        scalingYField.setText(String.valueOf(transformer.getScalingY()));
        scalingZField.setText(String.valueOf(transformer.getScalingZ()));
    }

    public void updateTranslation() {
        final var model = active.get(scene);
        if (model == null) {
            return;
        }

        final var transformer = model.unwrap().transformer();

        translationXField.setText(String.valueOf(transformer.getTranslationX()));
        translationYField.setText(String.valueOf(transformer.getTranslationY()));
        translationZField.setText(String.valueOf(transformer.getTranslationZ()));
    }

    public void updateRotation() {
        final var model = active.get(scene);
        if (model == null) {
            return;
        }

        final var transformer = model.unwrap().transformer();

        rotationXField.setText(String.valueOf(transformer.getRotationX()));
        rotationYField.setText(String.valueOf(transformer.getRotationY()));
        rotationZField.setText(String.valueOf(transformer.getRotationZ()));
    }

    public void updateFields() {
        if (active.get(scene) == null) {
            return;
        }

        updateScaling();
        updateTranslation();
        updateRotation();
    }
}
