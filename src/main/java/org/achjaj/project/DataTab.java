package org.achjaj.project;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class DataTab extends Tab {
    private final TableView table;

    public DataTab(JuliaVariable variable) {
        table = new TableView<>();
        table.setPlaceholder(new Label("Only variables with rank smaller than three can be shown"));

        this.setContent(table);
        this.setText(variable.getName());

        if (variable.getData() != null) {
            var numberCol = new TableColumn("#");
            numberCol.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>) p ->
                new ReadOnlyObjectWrapper(p.getValue().index + ""));
            numberCol.setSortable(false);
            table.getColumns().add(numberCol);


            fillTable(variable);
        }
    }

    private ObservableList parseData(JuliaVariable variable) {
        var rows = FXCollections.observableArrayList();
        final long[] dims = variable.getDimensions();

        IntStream.range(0, (int)variable.getDimensions()[0]).forEach(i -> {
            var row = FXCollections.observableArrayList();
            var obj = Array.get(variable.getData(), i);
            if (obj.getClass().isArray())
                row.addAll(objToList(obj));
            else
                row.add(obj);
            rows.add(new Item(row, i + 1));
        });

        return rows;
    }

    private Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>> getFactory(int i) {
        return param -> new SimpleStringProperty(param.getValue().get(i - 1).toString());
    }

    private List objToList(Object obj) {
        var len = Array.getLength(obj);
        var tmp = new Object[len];

        IntStream.range(0, len).forEach(i -> tmp[i] = Array.get(obj, i));

        return Arrays.asList(tmp);
    }

    private void fillTable(JuliaVariable variable) {
        final long[] dims = variable.getDimensions();
        final int nCols = dims.length == 1 ? 1 : (int)dims[1];
        IntStream.rangeClosed(1, nCols).forEach( i -> {
            String colName = variable.getColNames() == null ? "" + i : variable.getColNames()[i - 1];

            var column = new TableColumn(colName);
            column.setCellValueFactory(getFactory(i));

            table.getColumns().add(column);
        });

        table.setItems(parseData(variable));
    }

    private static class Item {
        private final ObservableList list;
        private int index;

        Item(ObservableList list, int index) {
            this.list = list;
            this.index = index;
        }

        Object get(int i) {
            return list.get(i);
        }
    }
}
