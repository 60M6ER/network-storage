package com.larionov.storage.client;

import com.larionov.storage.core.files.FileViewer;
import com.larionov.storage.core.net.AuthMessage;
import com.larionov.storage.core.net.AuthorizationTrue;
import com.larionov.storage.core.net.ErrorMessage;
import com.larionov.storage.core.net.FileList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClientController implements Initializable, NetListener {

    public ListView<String> lvLocalFiles;
    public ListView<String> lvServerFiles;

    public TextArea taLog;
    public TextField tfLocalPath;
    public TextField tfServerPath;
    public TextField tfHost;
    public TextField tfPort;
    public TextField tfLogin;
    public TextField pfPassword;
    public ComboBox<String> cbLocalPath;
    public Button bConnect;

    private DataInputStream is;
    private DataOutputStream os;

    private File currentDir;
    private FileViewer fileViewer;

    private byte[] buf;

    private ObservableList<String> paths;

    public void sendMessage(ActionEvent actionEvent) throws IOException {
        String fileName = taLog.getText();
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
        taLog.clear();
    }

    private void read() {
        try {
            while (true) {
                String message = is.readUTF();
                Platform.runLater(() -> taLog.setText(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // reconnect to server
        }
    }

    private void fillCurrentDirFiles(ListView<String> lvFiles, TextField path, List<String> filesList, String viewPath) {
        lvFiles.getItems().clear();
        String[] files = new String[filesList.size()];
        filesList.toArray(files);
        lvFiles.getItems().addAll(files);
        path.clear();
        path.appendText(viewPath);
    }

    private void initClickListener() {
        lvLocalFiles.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String fileName = lvLocalFiles.getSelectionModel().getSelectedItem();
                if (fileViewer.resolveFile(fileName)) {
                    try {
                        fillCurrentDirFiles(lvLocalFiles, tfLocalPath, fileViewer.getListFiles(), fileViewer.getViewDir());
                    } catch (IOException ex) {
                        taLog.appendText("Ошибка работы с файлами: " + ex.getMessage());
                    }
                } else {
                    taLog.appendText(fileName);
                }
            }
        });
    }

    public void setRootPath(Event event){
        fileViewer.goToPath(cbLocalPath.getValue());
        try {
            fillCurrentDirFiles(lvLocalFiles, tfLocalPath, fileViewer.getListFiles(), fileViewer.getViewDir());
        } catch (IOException ex) {
            taLog.appendText("Ошибка работы с файлами: " + ex.getMessage());
        }
    }

    public void clickConnect(Event e) {
        Net netInstance = Net.getInstance();
        if (!netInstance.isConnected()){
            netInstance.addHost(tfHost.getText(), Integer.parseInt(tfPort.getText()));
            taLog.appendText("Start connecting to host\n");
            try {
                netInstance.addListener(this);
                netInstance.connect();
            } catch (Exception e1) {
                taLog.appendText(e1.getMessage() + "\n");
            }
        } else {
            sendAuthorization();
        }
    }

    private void sendAuthorization(){
        taLog.appendText("Start authorization\n");
        AuthMessage message = new AuthMessage(tfLogin.getText(), pfPassword.getText());
        Net netInstance = Net.getInstance();
        netInstance.write(message);
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
            try {
                fillCurrentDirFiles(lvLocalFiles, tfLocalPath, fileViewer.getListFiles(), fileViewer.getViewDir());
            } catch (IOException ex) {
                taLog.appendText("Ошибка работы с файлами: " + ex.getMessage());
            }
            initClickListener();

            bConnect.setOnAction(this::clickConnect);

            cbLocalPath.setOnAction(this::setRootPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionActive() {
        taLog.appendText("Host connected\n");
        sendAuthorization();
    }

    @Override
    public void onError(ErrorMessage message) {
        taLog.appendText("Server return error: " + message.getMessage() + "\n");
    }

    @Override
    public void onAuthorizationTrue(AuthorizationTrue message) {
        taLog.appendText("Successful authorization\n");
        if (!message.getMessage().isEmpty())
            taLog.appendText(message.getMessage() + "\n");
    }

    @Override
    public void onFileList(FileList message) {
        fillCurrentDirFiles(lvServerFiles, tfServerPath, message.getList(), message.getViewPath());
    }
}
