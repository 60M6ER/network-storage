package com.larionov.storage.client;

import com.larionov.storage.core.files.FileViewer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    public ListView<String> lvLocalFiles;

    public TextField tfLog;
    public TextField tfLocalPath;
    public ComboBox<String> cbLocalPath;

    private DataInputStream is;
    private DataOutputStream os;

    private File currentDir;
    private FileViewer fileViewer;

    private byte[] buf;

    private ObservableList<String> paths;

    public void sendMessage(ActionEvent actionEvent) throws IOException {
        String fileName = tfLog.getText();
        File currentFile = currentDir.toPath().resolve(fileName).toFile();
        os.writeUTF("#SEND#FILE#");
        os.writeUTF(fileName);
        os.writeLong(currentFile.length());
        try (FileInputStream is = new FileInputStream(currentFile)) {
            while (true) {
                int read = is.read(buf);
                if (read == - 1) {
                    break;
                }
                os.write(buf, 0, read);
            }
        }
        os.flush();
        tfLog.clear();
    }

    private void read() {
        try {
            while (true) {
                String message = is.readUTF();
                Platform.runLater(() -> tfLog.setText(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // reconnect to server
        }
    }

    private void fillCurrentDirFiles() {
        lvLocalFiles.getItems().clear();
        try {
            String[] files = new String[fileViewer.getListFiles().size()];
            fileViewer.getListFiles().toArray(files);
            lvLocalFiles.getItems().addAll(files);
            tfLocalPath.clear();
            tfLocalPath.appendText(fileViewer.getViewDir());
        } catch (IOException e) {
            tfLog.appendText("Ошибка работы с файлами: " + e.getMessage());
        }
    }

    private void initClickListener() {
        lvLocalFiles.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String fileName = lvLocalFiles.getSelectionModel().getSelectedItem();
                if (fileViewer.resolveFile(fileName)) {
                    fillCurrentDirFiles();
                } else {
                    tfLog.appendText(fileName);
                }
            }
        });
    }

    public void setRootPath(Event event){
        fileViewer.goToPath(cbLocalPath.getValue());
        fillCurrentDirFiles();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            paths = FXCollections.observableArrayList();
            File[] roots = File.listRoots();
            for (int i = 0; i < roots.length; i++) {
                paths.add(roots[i].getPath());
            }
            cbLocalPath.setItems(paths);
            fileViewer = new FileViewer();
            fillCurrentDirFiles();
            initClickListener();

            cbLocalPath.setOnAction(this::setRootPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
