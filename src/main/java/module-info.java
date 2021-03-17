module org.achjaj.ju {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires log4j;
    requires jediterm.pty;
    requires pty4j;
    requires jhdf5;
    requires org.eclipse.lsp4j;
    requires org.eclipse.lsp4j.jsonrpc;
    requires java.sql;
    requires org.fxmisc.richtext;

    exports org.achjaj.ju;
    opens org.achjaj.ju;
}
