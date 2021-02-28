package org.achjaj.term;

import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalWidgetListener;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.pty4j.PtyProcess;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.Tab;
import javafx.util.Pair;

public class TermTab extends Tab {
    private final JediTermWidget termWidget;
    private final SwingNode container;
    private final PtyProcess process;

    private final TerminalWidgetListener listener = widget -> {
      if (!widget.getCurrentSession().getTtyConnector().isConnected())
          Platform.runLater(() -> this.getTabPane().getTabs().remove(this));
    };

    public TermTab(Pair<TtyConnector, PtyProcess> ttyBundle, String label) {
        process = ttyBundle.getValue();
        container = new SwingNode();
        termWidget = new JediTermWidget(new DefaultSettingsProvider());

        termWidget.setTtyConnector(ttyBundle.getKey());
        termWidget.addListener(listener);
        termWidget.start();

        container.setContent(termWidget);
        container.setOnMouseClicked(event -> container.requestFocus());

        this.setContent(container);
        this.setText(label);
        this.setOnClosed(event -> this.closeTerm());
    }

    public void closeTerm() {
        termWidget.close();
    }

    public PtyProcess getProcess() {
        return process;
    }
}
