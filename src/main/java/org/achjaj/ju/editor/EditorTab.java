package org.achjaj.ju.editor;

import javafx.scene.control.Tab;
import org.eclipse.lsp4j.TextDocumentItem;
import org.fxmisc.richtext.CodeArea;

import java.nio.file.Path;

public class EditorTab extends Tab {
    private final Path path;
    private final TextDocumentItem document;
    private final CodeArea editor;

    public EditorTab(Path path, String content, String language) {
        this.path = path;
        document = new TextDocumentItem(path.toUri().toString(), language, 0, content);

        editor = new CodeArea();
        editor.replaceText(content);
        editor.setOnInputMethodTextChanged(inputMethodEvent -> document.setText(editor.getText()));

        this.setText(path.getFileName().toString());
        this.setContent(editor);
    }
}
