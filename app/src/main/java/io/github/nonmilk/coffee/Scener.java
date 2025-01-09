package io.github.nonmilk.coffee;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.nonmilk.coffee.grinder.Renderer;
import io.github.nonmilk.coffee.grinder.render.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;

public final class Scener {

    private static final String DEFAULT_NAME = "Scene";
    private final StringBuilder nameBuilder = new StringBuilder();

    private Renderer renderer;

    private Camerer camerer;
    private Modeler modeler;

    private final Map<String, NamedScene> scenes = new HashMap<>();
    private NamedScene active;

    @FXML
    private ListView<NamedScene> view;
    private final ObservableList<NamedScene> list = FXCollections.observableArrayList();

    @FXML
    private Button addBtn;

    @FXML
    private Button removeBtn;

    @FXML
    private Button renameBtn;

    @FXML
    private Button markActiveBtn;

    @FXML
    private void initialize() {
        view.setItems(list);

        initMarkActive();
        initAdd();
        initRemove();
        initRename();
    }

    private void add(final Scene s, final String name) {
        if (scenes.get(name) != null) {
            throw new IllegalArgumentException("this name already exists");
        }

        scenes.put(name, new NamedScene(s, name));
        list.add(scenes.get(name));
        view.refresh();
    }

    private void remove(final String name) {
        final var scene = scenes.get(name);

        if (scene == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        } else if (scene == active) {
            throw new IllegalArgumentException("cannot remove active scene");
        }

        scenes.remove(name);
        view.refresh();
        // removal from list should be handled by the button
    }

    private void rename(final String oldName, final String newName) {
        final var scene = scenes.get(oldName);
        if (scene == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        if (scenes.get(newName) != null) {
            throw new IllegalArgumentException("this name already exists");
        }

        scenes.remove(oldName);
        scene.rename(newName);
        scenes.put(newName, scene);

        view.refresh();
    }

    private void initMarkActive() {
        markActiveBtn.setOnAction(e -> {
            final var scene = selected();
            if (scene == null) {
                return;
            }

            markActive(scene.name());
        });
    }

    private void initRename() {
        final var dialog = new TextInputDialog();
        final var field = dialog.getEditor();

        renameBtn.setOnAction(e -> {
            final var scene = selected();
            if (scene == null) {
                return;
            }

            field.setText(scene.name());

            dialog.showAndWait().ifPresent(response -> {
                try {
                    rename(scene.name(), response);
                } catch (final IllegalArgumentException err) {
                    return;
                }
            });
        });
    }

    private void initAdd() {
        addBtn.setOnAction(e -> {
            final var name = uniqueName();
            add(new Scene(), name);

            view.selectionModelProperty().get().selectLast();
            markActive(selected().name());
        });
    }

    private void initRemove() {
        removeBtn.setOnAction(e -> {
            final var selection = view.selectionModelProperty().get();

            final var scene = selection.getSelectedItem();
            if (scene == null) {
                return;
            }

            remove(scene.name());
            list.remove(selection.getSelectedIndex());
        });
    }

    private void markActive(final String name) {
        final var scene = scenes.get(name);
        if (scene == null) {
            throw new IllegalArgumentException("this name doesn't exist");
        }

        if (active != null) {
            active.setStatus(NamedScene.Status.DEFAULT);
        }

        active = scene;
        active.setStatus(NamedScene.Status.ACTIVE);
        renderer.setScene(active());

        view.refresh();

        update();
    }

    private Scene active() {
        return active.unwrap();
    }

    private NamedScene selected() {
        return view.selectionModelProperty().get().getSelectedItem();
    }

    public void setRenderer(final Renderer renderer) {
        this.renderer = Objects.requireNonNull(renderer);

        scenes.clear();
        list.clear();

        final String name = uniqueName();

        final var scene = renderer.scene();
        if (scene != null) {
            add(scene, name);
        } else {
            add(new Scene(), name);
        }

        markActive(name);

        update();
    }

    public void setCamerer(final Camerer c) {
        camerer = Objects.requireNonNull(c);
        camerer.setScene(active());
    }

    public void setModeler(final Modeler m) {
        modeler = Objects.requireNonNull(m);
        modeler.setScene(active());
    }

    private void update() {
        final var scene = active();

        if (camerer != null) {
            camerer.setScene(scene);
        }

        if (modeler != null) {
            modeler.setScene(scene);
        }
    }

    private String uniqueName() {
        var name = DEFAULT_NAME;

        if (scenes.get(name) == null) {
            return name;
        }

        nameBuilder.setLength(0);
        nameBuilder.append(name);
        nameBuilder.append(' ');

        int postfix = 1;
        nameBuilder.append(postfix++);
        name = nameBuilder.toString();

        while (scenes.get(name) != null) {
            nameBuilder.setLength(nameBuilder.length() - 1);
            nameBuilder.append(postfix++);
            name = nameBuilder.toString();
        }

        return name;
    }
}
