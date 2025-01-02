package io.github.nonmilk.coffee;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class App extends Application {

    public App() {
    }

    @Override
    public void start(final Stage stage) throws IOException {
        final FXMLLoader l = new FXMLLoader(
                Grinder.class.getResource("app.fxml"));

        final Scene scene = new Scene(l.load());

        stage.setTitle("coffee-grinder");
        stage.setScene(scene);

        stage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
