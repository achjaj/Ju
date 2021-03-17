package org.achjaj.ju.project;

import ch.systemsx.cisd.hdf5.HDF5Factory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Workspace {
    private final Path wsFile;
    private Callback onUpdate;

    public Workspace(Path project) {
        this.wsFile = project.resolve("workspace.h5").toAbsolutePath();
    }

    public void update() {
        List<String> names = null;

        if (Files.isRegularFile(wsFile)) {
            var reader = HDF5Factory.openForReading(wsFile.toFile());
            names = reader.getGroupMembers("/");
            reader.close();
        }

        onUpdate.fire(names);
    }

    public void setOnUpdate(Callback onUpdate) {
        this.onUpdate = onUpdate;
    }

    public JuliaVariable getVariable(String varName) {
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
        void fire(List<String> names);
    }
}
