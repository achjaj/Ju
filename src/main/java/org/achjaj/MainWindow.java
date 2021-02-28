package org.achjaj;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import org.achjaj.project.DataTab;
import org.achjaj.project.ProjectWatcher;
import org.achjaj.project.Workspace;
import org.achjaj.term.TermTab;
import org.achjaj.term.TtyConnectors;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {
    private ProjectWatcher projectWatcher;
    private Workspace workspace;

    public MainWindow() {
        try {
            FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private TabPane termTabs, mainTabs;
    @FXML
    private ListView<String> workspaceView;

    private void loadProject(Path projectFolder) throws IOException {
        workspace = new Workspace(workspaceView, projectFolder);
        workspace.setCallback(variable -> {
            mainTabs.getTabs().add(new DataTab(variable));
            mainTabs.getSelectionModel().selectLast();
        });

        projectWatcher = new ProjectWatcher(projectFolder);
        projectWatcher.setOnDeleted(file -> System.out.println("Deleted: " + file.getFileName()));
        projectWatcher.setOnModified(file -> {
            System.out.println("Create/Modify: " + file.getFileName().toString());

            if (file.getFileName().toString().equals("workspace.h5")) {
                workspace.update();
            }
        });
        projectWatcher.setOnCreated(projectWatcher.getOnModified());
    }

    public void onWindowClose() {
        termTabs.getTabs().stream().map(tab -> (TermTab) tab).forEach(TermTab::closeTerm);
        projectWatcher.stop();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var bundle  = TtyConnectors.REPLConnector();
        termTabs.getTabs().add(new TermTab(bundle, "label"));

        try {
            loadProject(Path.of("testProject"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
