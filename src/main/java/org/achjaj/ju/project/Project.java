package org.achjaj.ju.project;

import java.io.IOException;
import java.nio.file.Path;

public class Project {
    private ProjectWatcher watcher;
    private Workspace workspace;
    private Path path;

    private FileCallback onFileDeletedGUIAction,
                         onFileModifiedGUIAction,
                         onFileCreatedGUIAction;

    private ProjectWatcher.Callback onFileDeletedAction = file -> {
        workspace.update();

        if (onFileDeletedGUIAction != null)
            onFileDeletedGUIAction.fire(file);
    };

    private ProjectWatcher.Callback onFileModifiedAction = file -> {
        workspace.update();

        if (onFileModifiedGUIAction != null)
            onFileModifiedGUIAction.fire(file);
    };

    private ProjectWatcher.Callback onFileCreatedAction = file -> {
        workspace.update();

        if (onFileCreatedGUIAction != null)
            onFileCreatedGUIAction.fire(file);
    };

    public Project(Path projectFolder) throws IOException {
        workspace = new Workspace(projectFolder);
        watcher = new ProjectWatcher(projectFolder);

        watcher.setOnCreated(onFileCreatedAction);
        watcher.setOnDeleted(onFileDeletedAction);
        watcher.setOnModified(onFileModifiedAction);

        path = projectFolder;
    }

    public void setOnFileDeletedGUIAction(FileCallback onFileDeletedGUIAction) {
        this.onFileDeletedGUIAction = onFileDeletedGUIAction;
    }

    public void setOnFileModifiedGUIAction(FileCallback onFileModifiedGUIAction) {
        this.onFileModifiedGUIAction = onFileModifiedGUIAction;
    }

    public void setOnFileCreatedGUIAction(FileCallback onFileCreatedGUIAction) {
        this.onFileCreatedGUIAction = onFileCreatedGUIAction;
    }

    public ProjectWatcher getWatcher() {
        return watcher;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public Path getPath() {
        return path;
    }

    public interface FileCallback {
        void fire(Path file);
    }
}
