module org.achjaj {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires log4j;
    requires jediterm.pty;
    requires pty4j;
    requires jhdf5;

    exports org.achjaj;
    opens org.achjaj;
}
