package org.achjaj.ju.project;

public class JuliaVariable {
    private final long[] dimensions;
    private final Object data;
    private final String[] colNames;
    private final String name;

    public JuliaVariable(long[] dimensions, Object data, String[] colNames, String name) {
        this.dimensions = dimensions;
        this.data = data;
        this.colNames = colNames;
        this.name = name;
    }

    public long[] getDimensions() {
        return dimensions;
    }

    public Object getData() {
        return data;
    }

    public String[] getColNames() {
        return colNames;
    }

    public String getName() {
        return name;
    }
}
