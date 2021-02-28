package org.achjaj.term;

import com.jediterm.pty.PtyMain;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.UIUtil;
import com.pty4j.PtyProcess;
import javafx.util.Pair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class TtyConnectors {

    public static Pair<TtyConnector, PtyProcess> procConnector(String... proc) throws IOException {
        var charset = StandardCharsets.UTF_8;
        var env = new HashMap<>(System.getenv());
        env.put("TERM", "xterm");
        var process = PtyProcess.exec(proc, env, null);

        return new Pair<>(new PtyMain.LoggingPtyProcessTtyConnector(process, charset), process);
    }

    public static Pair<TtyConnector, PtyProcess> REPLConnector() {
        try {
            return procConnector("julia");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Pair<TtyConnector, PtyProcess> cmdConnector() {
        try {
            String cmd = UIUtil.isWindows ? "cmd.exe" : "/bin/bash";
            return procConnector(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
