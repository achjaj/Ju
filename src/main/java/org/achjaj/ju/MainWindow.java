package org.achjaj.ju;

import com.jediterm.terminal.TtyConnector;
import com.pty4j.PtyProcess;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.stage.DirectoryChooser;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.achjaj.ju.editor.EditorTab;
import org.achjaj.ju.project.DataTab;
import org.achjaj.ju.project.Project;
import org.achjaj.ju.project.Workspace;
import org.achjaj.ju.term.TermTab;
import org.achjaj.ju.term.TtyConnectors;

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
    private TreeView<Path> projectExplorer;

    @FXML
    private void openProject() {
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

    @FXML
    private void addTerminal(ActionEvent event) {
        var label = ((MenuItem)event.getTarget()).getText();
        Pair<TtyConnector, PtyProcess> bundle;

        if (label.equals("System"))
            bundle = TtyConnectors.cmdConnector();
        else
            bundle = TtyConnectors.REPLConnector();

        termTabs.getTabs().add(new TermTab(bundle, label));
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

    private void addToTreeItem(Path file, TreeItem<Path> root) {
        var item = new TreeItem<>(file);

        if (Files.isDirectory(file)) {
            item.getChildren().add(new TreeItem<>());
        }

        root.getChildren().add(item);
    }

    private void removeFromExplorer(Path file) {
        projectExplorer.getRoot().getChildren().removeIf(item -> item.getValue().equals(file));
    }

    private void addToExplorer(Path file) {
        addToTreeItem(file, projectExplorer.getRoot());
    }

    private void fillExplorer(Path projectFolder) throws IOException {
        var root = new TreeItem<>(projectFolder);
        root.setExpanded(true);
        projectExplorer.setRoot(root);

        Files.list(projectFolder).forEachOrdered(this::addToExplorer);
    }

    private void setupTreeItem(TreeItem<Path> item) {
        item.expandedProperty().addListener((o1, o2, expanded) -> {
            if (expanded) {
                item.getChildren().clear();

                try {
                    Files.list(item.getValue()).forEach(path -> addToTreeItem(path, item));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                item.getChildren().clear();
                item.getChildren().add(new TreeItem<>());
            }
        });
    }

    private void openFile(Path file) throws IOException {
        var content = Files.readString(file);
        mainTabs.getTabs().add(new EditorTab(file, content, ""));
    }

    public void onWindowClose() {
        termTabs.getTabs().stream().map(tab -> (TermTab) tab).forEach(TermTab::closeTerm);
        project.getWatcher().stop();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        workspaceView.setOnMouseClicked(event -> {
            var name = workspaceView.getSelectionModel().getSelectedItem();
            var juliaVariable = project.getWorkspace().getVariable(name);

            mainTabs.getTabs().add(new DataTab(juliaVariable));
            mainTabs.getSelectionModel().selectLast();
        });

        projectExplorer.setCellFactory(pathTreeView -> {
            var cell = TextFieldTreeCell.forTreeView(new StringConverter<Path>() {
                @Override
                public String toString(Path path) {
                    return path.getFileName().toString();
                }

                @Override
                public Path fromString(String s) {
                    return Path.of(s);
                }
            }).call(pathTreeView);

            cell.treeItemProperty().addListener((observableValue, pathTreeItem, t1) ->  {
                if (t1 != null) {
                    cell.setItem(t1.getValue().getFileName());
                    setupTreeItem(t1);
                }
            });

            return cell;
        });

        projectExplorer.setOnMouseClicked(event -> {
            var file = projectExplorer.getSelectionModel().getSelectedItem().getValue();
            if (file != null && Files.isRegularFile(file)) {
                try {
                    openFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    projectExplorer.getSelectionModel().clearSelection();
                }
            }
        });

        try {
            loadProject(Path.of(System.getProperty("user.home")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
