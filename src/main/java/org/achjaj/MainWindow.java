package org.achjaj;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import org.achjaj.project.DataTab;
import org.achjaj.project.Project;
import org.achjaj.project.Workspace;
import org.achjaj.term.TermTab;
import org.achjaj.term.TtyConnectors;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {
    private Project project;

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
    @FXML
    private TreeView<String> projectExplorer;

    @FXML
    private void openProject(ActionEvent event) {
        var file = new DirectoryChooser().showDialog(null);
        if (file != null) {
            try {
                loadProject(file.toPath());
            } catch (IOException e) {
                //TODO: dialog
                e.printStackTrace();
            }
        }
    }

    private Workspace.Callback onWorkspaceUpdate = names -> {
        if (names != null)
            workspaceView.setItems(FXCollections.observableArrayList(names));
        else
            workspaceView.getItems().clear();
    };

    private void loadProject(Path projectFolder) throws IOException {
        project = new Project(projectFolder);
        project.getWorkspace().setOnUpdate(onWorkspaceUpdate);
        project.getWorkspace().update();

        project.setOnFileCreatedGUIAction(this::addToExplorer);
        project.setOnFileDeletedGUIAction(this::removeFromExplorer);

        fillExplorer(projectFolder);

        //TODO: julia REPL cd() to project root
    }

    private void addToTreeItem(Path file, TreeItem<String> root) {
        var item = new TreeItem<>(file.getFileName().toString());
        if (Files.isDirectory(file)) {
            item.getChildren().add(new TreeItem<>());
            item.expandedProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue) {
                    item.getChildren().clear();

                    try {
                        Files.list(file).forEach(path -> addToTreeItem(path, item));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    item.getChildren().clear();
                    item.getChildren().add(new TreeItem<>());
                }
            });
        }

        root.getChildren().add(item);
    }

    private void removeFromExplorer(Path file) {
        projectExplorer.getRoot().getChildren().removeIf(item -> item.getValue().equals(file.getFileName().toString()));
    }

    private void addToExplorer(Path file) {
        addToTreeItem(file, projectExplorer.getRoot());
    }

    private void fillExplorer(Path projectFolder) throws IOException {
        var root = new TreeItem<>(projectFolder.getFileName().toString());
        root.setExpanded(true);
        projectExplorer.setRoot(root);

        Files.list(projectFolder).forEachOrdered(this::addToExplorer);
    }

    public void onWindowClose() {
        termTabs.getTabs().stream().map(tab -> (TermTab) tab).forEach(TermTab::closeTerm);
        project.getWatcher().stop();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var bundle  = TtyConnectors.REPLConnector();
        termTabs.getTabs().add(new TermTab(bundle, "label"));

        workspaceView.setOnMouseClicked(event -> {
            var name = workspaceView.getSelectionModel().getSelectedItem();
            var juliaVariable = project.getWorkspace().getVariable(name);

            mainTabs.getTabs().add(new DataTab(juliaVariable));
            mainTabs.getSelectionModel().selectLast();
        });

        try {
            loadProject(Path.of("/home/jakub"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
