package io.github.nonmilk.coffee;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;

public final class App extends Application {

    public App() {
    }

    @Override
    public void start(final Stage stage) throws IOException {
        stage.show(); // black screen
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
