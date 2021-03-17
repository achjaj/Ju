package org.achjaj.ju.lsp;

import org.apache.log4j.LogManager;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client implements LanguageClient {
    private final Process proc;

    private Launcher<LanguageServer> launcher;
    private LanguageServer server;

    public static Client build() throws IOException {
        log("Starting process");
        final var proc = new ProcessBuilder()
            .command("julia", "-e", "using LanguageServer, LanguageServer.SymbolServer; runserver()")
            .redirectErrorStream(false)
            .start();
        
        final var se = new Scanner(proc.getErrorStream());

        // ================= DEBUG ONLY, REMOVE IN PRODUCTION ======================================
        var s = Executors.newScheduledThreadPool(1);
        s.scheduleAtFixedRate(() -> {
            while (proc.isAlive()) {
                log(se.nextLine());
            }

            log("server process exited: " + proc.exitValue());
            s.shutdown();
        }, 1, 1, TimeUnit.MICROSECONDS);
        // =========================================================================================

        var client = new Client(proc);
        var launcher = LSPLauncher.createClientLauncher(client, proc.getInputStream(), proc.getOutputStream());
        log("start listening");
        launcher.startListening();
        client.setLauncher(launcher);

        return client;
    }


    public Client(Process proc) {
        this.proc = proc;
    }

    private static void log(String msg) {
        LogManager.getRootLogger().debug(msg);
    }

    public void setLauncher(Launcher<LanguageServer> launcher) {
        this.launcher = launcher;
        this.server = launcher.getRemoteProxy();
    }

    public void hover(String uri, Position position) {
        var params = new HoverParams(new TextDocumentIdentifier(uri), position);

        server.getTextDocumentService().hover(params);
    }

    public void didOpen(TextDocumentItem docItem) {
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(docItem));
    }

    @Override
    public void telemetryEvent(Object o) {
        log("telemetryEvent: " + o.toString());
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
        log("publishDiagnostics: " + publishDiagnosticsParams);
    }

    @Override
    public void showMessage(MessageParams messageParams) {
        log("showMessage: " + messageParams);
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams showMessageRequestParams) {
        log("showMessageRequest: " + showMessageRequestParams);
        return CompletableFuture.supplyAsync(() -> new MessageActionItem("<none>"));
    }

    @Override
    public void logMessage(MessageParams messageParams) {
        log("logMessage: " + messageParams);
    }


}
