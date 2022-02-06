package com.larionov.storage.core.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileViewer {

    private Path rootDir;
    private Path currentDir;

    public FileViewer() {
        currentDir = Paths.get(System.getProperty("user.home"));
    }

    public FileViewer(Path rootDir) {
        this.rootDir = rootDir;
        this.currentDir = rootDir;
    }

    private boolean isParent(){
        return currentDir.getParent() != null;
    }

    public FileViewer goToParent(){
        if (isParent()) {
            currentDir = currentDir.getParent();
        }
        return this;
    }

    public FileViewer goToPath(Path path){
        currentDir = path;
        return this;
    }

    public FileViewer goToPath(String path){
        return goToPath(Paths.get(path));
    }

    public boolean resolveFile(String name){
        if(name.equals("..")) {
            if (!currentDir.equals(rootDir)) goToParent();
            return true;
        }
        else {
            Path resolve = currentDir.resolve(name);
            if (Files.isDirectory(resolve)){
                currentDir = resolve;
                return true;
            }
            return false;
        }
    }

    public List<String> getListFiles() throws IOException {
        ArrayList<String> files = new ArrayList<>();
        if (isParent())
            files.add("..");
        Files.list(currentDir).forEach(f -> {
            files.add(f.getFileName().toString());
        });
        return files;
    }

    public String getViewDir() {
        return currentDir.toString();
    }
}
