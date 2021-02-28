package org.achjaj.project;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class ProjectWatcher {
    private Path project;
    private WatchService watcher;
    private final WatchKey key;
    private final ScheduledExecutorService loop;

    private Callback onCreated, onDeleted, onModified;

    private final Runnable job = () -> {
        var eventKey = watcher.poll();
        if (eventKey != null) {
            eventKey.pollEvents().forEach(event -> {
                var kind = event.kind();
                var file = ((Path) event.context()).getFileName().toString();

                assert project != null;
                var callback = (kind.equals(ENTRY_CREATE) ? onCreated : (kind.equals(ENTRY_MODIFY) ? onModified : onDeleted));
                if (callback != null)
                    callback.fire(project.resolve(file));
            });
        }
    };

    public ProjectWatcher(Path project) throws IOException {
        this.project = project;
        watcher = FileSystems.getDefault().newWatchService();

        key = this.project.register(watcher,
                ENTRY_CREATE,
                ENTRY_DELETE,
                ENTRY_MODIFY);

        loop = Executors.newScheduledThreadPool(1);
        loop.scheduleAtFixedRate(job, 1, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        loop.shutdownNow();
    }

    public void setOnCreated(Callback onCreated) {
        this.onCreated = onCreated;
    }

    public void setOnDeleted(Callback onDeleted) {
        this.onDeleted = onDeleted;
    }

    public void setOnModified(Callback onModified) {
        this.onModified = onModified;
    }

    public Callback getOnCreated() {
        return onCreated;
    }

    public Callback getOnDeleted() {
        return onDeleted;
    }

    public Callback getOnModified() {
        return onModified;
    }

    public interface Callback {
        void fire(Path file);
    }
}
