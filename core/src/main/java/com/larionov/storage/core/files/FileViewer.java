package com.larionov.storage.core.files;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileViewer {

    private Path rootDir;
    @Getter
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

    public Path getPathToFile(String name){
        return currentDir.resolve(name);
    }

    public boolean resolveFile(String name){
        if(name.equals(FileDescription.getPathToParent().getName())) {
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

    public void createFolder(String nameFile) throws IOException {
        Path newFile = currentDir.resolve(nameFile);
        Files.createDirectory(newFile);
    }

    public void renameFile(String oldName, String newName) throws IOException{
        Path currentFile = currentDir.resolve(oldName);
        Files.move(currentFile, currentFile.resolveSibling(newName));
    }

    private void delete(Path delPath) throws IOException{
        if (Files.isDirectory(delPath)) {
            List<Path> listFiles = Files.list(delPath).collect(Collectors.toList());
            for (int i = 0; i < listFiles.size(); i++) {
                delete(listFiles.get(i));
            }
        }
        Files.delete(delPath);
    }

    public void deleteFile(String nameFile) throws IOException{
        Path currentFile = currentDir.resolve(nameFile);
        delete(currentFile);
    }

    public List<FileDescription> getListFiles() throws IOException {
        ArrayList<FileDescription> files = new ArrayList<>();
        if (isParent() && rootDir == null ||
                isParent() && rootDir != null && !Files.isSameFile(currentDir, rootDir))
            files.add(FileDescription.getPathToParent());
        Files.list(currentDir).forEach(f -> {
            try {
                files.add(new FileDescription(
                        Files.isDirectory(f),
                        f.getFileName().toString(),
                        Files.isDirectory(f) ? 0 : Files.size(f))
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return files;
    }

    public String getViewDir() {
        if (rootDir != null)
            return currentDir.toString().replace(rootDir.toString(), "home");
        return currentDir.toString();
    }
}
