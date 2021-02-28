package org.achjaj.project;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.nio.file.Files;
import java.nio.file.Path;

public class Workspace {
    private ListView<String> view;
    private final Path wsFile;
    private Callback callback;

    private final EventHandler<MouseEvent> clickAction = event -> {
        var varName = view.getSelectionModel().getSelectedItem();

        callback.fire(getVariable(varName));
    };

    public Workspace(ListView<String> view, Path project) {
        this.view = view;
        this.wsFile = project.resolve("workspace.h5").toAbsolutePath();

        view.setOnMouseClicked(clickAction);

        update();
    }

    public void update() {
        if (Files.isRegularFile(wsFile)) {
            var reader = HDF5Factory.openForReading(wsFile.toFile());
            var vars = reader.getGroupMembers("/");
            reader.close();

            Platform.runLater(() ->
                view.setItems(FXCollections.observableArrayList(vars)));

        } else {
            view.setItems(null);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private JuliaVariable getVariable(String varName) {
        var reader = HDF5Factory.openForReading(wsFile.toFile());
        var info = reader.getDataSetInformation("/" + varName);

        String[] colNames = reader.object().hasAttribute("/" + varName, "colNames") ? reader.string().getArrayAttr("/" + varName, "colNames") : null;

        Object data;
        if (!info.getTypeInformation().getDataClass().name().toLowerCase().equals("string")) {
            if (info.getRank() == 1)
                data = reader.readDoubleArray("/" + varName);
            else if (info.getRank() == 2)
                data = reader.readDoubleMatrix("/" + varName);
            else
                data = null;
        } else
            data = new String(reader.readAsByteArray("/" + varName));

        var variable = new JuliaVariable(info.getDimensions(), data, colNames, varName);
        reader.close();

        return variable;
    }

    public interface Callback {
        void fire(JuliaVariable data);
    }
}
