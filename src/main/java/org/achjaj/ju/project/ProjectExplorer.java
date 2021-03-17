package org.achjaj.ju.project;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectExplorer extends TreeView<Path> {
    private final Callback<TreeView<Path>, TreeCell<Path>> cellFactory = pathTreeView -> {
        var cell = TextFieldTreeCell.forTreeView(new PathStringConverter()).call(pathTreeView);

        cell.treeItemProperty().addListener((o1, o2, treeItem) ->  {
            if (treeItem != null) {
                cell.setItem(treeItem.getValue().getFileName());
                setupItem(treeItem);
            }
        });

        return cell;
    };


    public ProjectExplorer() {
        this.setCellFactory(cellFactory);
    }

    public void addToTreeItem(Path file, TreeItem<Path> root) {
        var item = new TreeItem<>(file);

        if (Files.isDirectory(file)) {
            item.getChildren().add(new TreeItem<>());
        }

        root.getChildren().add(item);
    }

    public void removeFromRoot(Path file) {
        this.getRoot().getChildren().removeIf(item -> item.getValue().equals(file));
    }

    public void addToRoot(Path file) {
        addToTreeItem(file, this.getRoot());
    }

    public void fill(Path projectFolder) throws IOException {
        var root = new TreeItem<>(projectFolder);
        root.setExpanded(true);
        this.setRoot(root);

        Files.list(projectFolder).forEachOrdered(this::addToRoot);
    }

    private void setupItem(TreeItem<Path> item) {
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

    private static class PathStringConverter extends StringConverter<Path> {
        @Override
        public String toString(Path path) {
            return path.getFileName().toString();
        }

        @Override
        public Path fromString(String s) {
            return Path.of(s);
        }
    }
}
