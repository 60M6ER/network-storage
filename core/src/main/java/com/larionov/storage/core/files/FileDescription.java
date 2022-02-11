package com.larionov.storage.core.files;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

public class FileDescription implements Serializable {

    private static FileDescription pathToParent;

    public static FileDescription getPathToParent() {
        if (pathToParent == null)
            pathToParent = new FileDescription(true, "..", -1);
        return pathToParent;
    }

    @Getter
    private boolean directory;
    @Getter
    @Setter
    private String name;
    @Getter
    private long size;

    public FileDescription(boolean directory, String name, long size) {
        this.directory = directory;
        this.name = name;
        this.size = size;
    }

    public FileDescription(boolean directory, String name) {
        this.directory = directory;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDescription that = (FileDescription) o;
        return directory == that.directory &&
                size == that.size &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory, name, size);
    }

    @Override
    public String toString() {
        return name;
    }
}
