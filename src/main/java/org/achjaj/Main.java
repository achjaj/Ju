package org.achjaj;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * JavaFX App
 */
public class Main extends Application {
    public static ResourceManager resourceManager;

    @Override
    public void start(Stage stage) throws IOException {
        var loader = new FXMLLoader(resourceManager.getResource("MainWindow.fxml").toUri().toURL());
        Parent root = loader.load();
        stage.setScene(new Scene(root));

        stage.setOnCloseRequest(event -> {
            try {
                ((MainWindow) loader.getController()).onWindowClose();
                resourceManager.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        stage.show();
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        LogManager.getRootLogger().setLevel(Level.OFF);
        LogManager.shutdown();

        resourceManager = new ResourceManager(Main.class.getResource("/MainWindow.fxml").toURI());
        launch();
    }

    private static void write() {
        var matrix = new double[5][2];
        IntStream.range(0, 5).forEach(i ->
            matrix[i] = DoubleStream.generate(ThreadLocalRandom.current()::nextDouble).limit(2).toArray());

        var writer = HDF5Factory.configure("workspace.h5").writer();
        writer.writeDoubleMatrix("/dataframe-A_B", matrix);
        writer.string().setArrayAttr("/dataframe-A_B", "colNames", new String[]{"A", "B"});

        var matrix2 = new int[5][2];
        IntStream.range(0, 5).forEach(i ->
            matrix2[i] = IntStream.generate(ThreadLocalRandom.current()::nextInt).limit(2).toArray());
        writer.writeIntMatrix("/matrix", matrix2);

        writer.close();
    }

}
