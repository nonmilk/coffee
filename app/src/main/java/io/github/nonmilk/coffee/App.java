package io.github.nonmilk.coffee;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class App extends Application {

    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;

    public App() {
    }

    @Override
    public void start(final Stage stage) throws IOException {
        final var loader = new FXMLLoader(Grinder.class.getResource("app.fxml"));

        final Parent p = loader.load();
        final var scene = new Scene(p, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        final Grinder g = loader.getController();
        g.init(stage);

        stage.setTitle("coffee-grinder");
        stage.setScene(scene);

        stage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
