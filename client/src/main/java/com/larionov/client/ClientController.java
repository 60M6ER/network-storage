package com.larionov.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ClientController implements Initializable {

    public ListView<String> lvLocalFiles;

    public TextField tfLog;
    public TextField tfLocalPath;

    private DataInputStream is;
    private DataOutputStream os;

    private File currentDir;

    private byte[] buf;

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
        lvLocalFiles.getItems().add("..");
        lvLocalFiles.getItems().addAll(currentDir.list());
        tfLocalPath.clear();
        try {
            tfLocalPath.appendText(currentDir.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initClickListener() {
        lvLocalFiles.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String fileName = lvLocalFiles.getSelectionModel().getSelectedItem();
                System.out.println("Выбран файл: " + fileName);
                Path path = currentDir.toPath().resolve(fileName);
                if (Files.isDirectory(path)) {
                    currentDir = path.toFile();
                    fillCurrentDirFiles();
                    tfLog.clear();
                } else {
                    tfLog.setText(fileName);
                }
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            buf = new byte[256];
            currentDir = new File(System.getProperty("user.home"));
            fillCurrentDirFiles();
            initClickListener();
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
