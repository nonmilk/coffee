package io.github.nonmilk.coffee;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public final class App extends Application {

    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;

    public App() {
    }

    @Override
    public void start(final Stage stage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler(this::handleError);

        final var loader = new FXMLLoader(Grinder.class.getResource("app.fxml"));

        final Parent p = loader.load();
        final var scene = new Scene(p, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        final Grinder g = loader.getController();
        g.init(stage);

        stage.setTitle("coffee-grinder");
        stage.setScene(scene);

        stage.show();
    }

    // source:
    // https://stackoverflow.com/questions/26361559/general-exception-handling-in-javafx-8
    private void handleError(final Thread t, final Throwable e) {
        final var msg = errorString(e);

        System.err.println(msg);

        if (!Platform.isFxApplicationThread()) {
            return;
        }

        showErrorAlert(msg);
    }

    private void showErrorAlert(final String msg) {
        final var alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.show();
    }

    private static String errorString(final Throwable e) {
        return String.format("Error: %s", e.getMessage());
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
