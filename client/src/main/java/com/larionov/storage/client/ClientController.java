package com.larionov.storage.client;

import com.larionov.storage.core.download.FileSendManager;
import com.larionov.storage.core.download.StatusSend;
import com.larionov.storage.core.download.StatusSenderListener;
import com.larionov.storage.core.files.FileDescription;
import com.larionov.storage.core.files.FileViewer;
import com.larionov.storage.core.net.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
@Slf4j
public class ClientController implements Initializable, NetListener, StatusSenderListener {

    private static final int DELAY_PROGRESS_BAR = 5 * 1000;

    public ListView<FileDescription> lvLocalFiles;
    public ListView<FileDescription> lvServerFiles;

    public TextArea taLog;
    public TextField tfLocalPath;
    public TextField tfServerPath;
    public TextField tfHost;
    public TextField tfPort;
    public TextField tfLogin;
    public TextField pfPassword;
    public ComboBox<String> cbLocalPath;
    public Button bConnect;
    public AnchorPane PaneProgress;
    public Label lStatusDownload;
    public ProgressBar pbDownload;


    ContextMenu contextMenu;
    private MenuItem miCreateFolder;
    private MenuItem miRename;
    private MenuItem miDelete;

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

    private void fillCurrentDirFiles(ListView<FileDescription> lvFiles, TextField path, List<FileDescription> filesList, String viewPath) {
        lvFiles.getItems().clear();
        lvFiles.getItems().addAll(filesList);
        path.clear();
        path.appendText(viewPath);
    }

    public void clickListener(MouseEvent e) {
        boolean local = lvLocalFiles.isFocused();
        ListView<FileDescription> listView =
                local ? lvLocalFiles : lvServerFiles;
        if (e.getClickCount() == 2) {
            String fileName = listView.getSelectionModel().getSelectedItem().getName();
            if (local) {
                if (fileViewer.resolveFile(fileName)) {
                    try {
                        fillCurrentDirFiles(lvLocalFiles, tfLocalPath, fileViewer.getListFiles(), fileViewer.getViewDir());
                    } catch (IOException ex) {
                        taLog.appendText("Ошибка работы с файлами: " + ex.getMessage());
                    }
                } else {
                    taLog.appendText(fileName);
                }
            } else {
                if (listView.getSelectionModel().getSelectedItem().isDirectory()){
                    Net.getInstance().write(new OpenFolder(fileName));
                }
            }
        }
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
        Net.getInstance().write(message);
    }

    public void contextMenuClick(ActionEvent e){
        boolean local = lvLocalFiles.isFocused();
        ListView<FileDescription> listView =
                local ? lvLocalFiles : lvServerFiles;

        if (e.getTarget() == miCreateFolder){
            listView.getItems().add(new FileDescription(true, "New folder"));
            listView.scrollTo(listView.getItems().size() - 1);
            listView.setEditable(true);
            listView.layout();
            int size = listView.getItems().size();
            listView.setOnEditCancel(value -> {
                if (size == listView.getItems().size())
                    listView.getItems().remove(listView.getItems().size() - 1);
                listView.setEditable(false);
            });// Удаляем новую папку
            listView.setOnEditCommit(value -> {
                if (!local)
                    Net.getInstance().write(new CreateFolder(value.getNewValue().getName()));
                else if (local){
                    try {
                        fileViewer.createFolder(value.getNewValue().getName());
                        fillCurrentDirFiles(
                                this.lvLocalFiles,
                                tfLocalPath,
                                fileViewer.getListFiles(),
                                fileViewer.getViewDir()
                        );
                    } catch (IOException ioException) {
                        taLog.appendText(ioException.getMessage() + "\n");
                    }
                }
                listView.setEditable(false);
            });
            listView.edit(listView.getItems().size() - 1);
        } else if (e.getTarget() == miRename){
            String oldName = listView.getSelectionModel().getSelectedItem().getName();
            listView.setEditable(true);
            listView.setOnEditCancel(value -> listView.setEditable(false));
            listView.setOnEditCommit(value -> {
                if (!local)
                    Net.getInstance().write(new RenameFile(oldName, value.getNewValue().getName()));
                else if (local){
                    try {
                        fileViewer.renameFile(oldName, value.getNewValue().getName());
                        fillCurrentDirFiles(
                                this.lvLocalFiles,
                                tfLocalPath,
                                fileViewer.getListFiles(),
                                fileViewer.getViewDir()
                        );
                    } catch (IOException ioException) {
                        taLog.appendText(ioException.getMessage() + "\n");
                    }
                }
                listView.setEditable(false);
            });
            listView.edit(listView.getSelectionModel().getSelectedIndex());
        } else if (e.getTarget() == miDelete) {
            if (!local)
                Net.getInstance().write(
                        new DeleteFile(
                                listView.getSelectionModel().getSelectedItem().getName()));
            else if (local) {
                try {
                    fileViewer.deleteFile(
                            listView.getSelectionModel().getSelectedItem().getName()
                    );
                    fillCurrentDirFiles(
                            this.lvLocalFiles,
                            tfLocalPath,
                            fileViewer.getListFiles(),
                            fileViewer.getViewDir()
                    );
                } catch (IOException ioException) {
                    taLog.appendText(ioException.getMessage() + "\n");
                }
            }
        }
    }

    //For testing
    private void fillingStartData(){
        tfHost.appendText("localhost");
        tfPort.appendText("8189");
        tfLogin.appendText("boris");
        pfPassword.appendText("123");
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

            bConnect.setOnAction(this::clickConnect);

            cbLocalPath.setOnAction(this::setRootPath);

            contextMenu = new ContextMenu();
            miCreateFolder = new MenuItem("New folder");
            miRename = new MenuItem("Rename");
            miDelete = new MenuItem("Delete");
            contextMenu.getItems().add(miCreateFolder);
            contextMenu.getItems().add(miRename);
            contextMenu.getItems().add(miDelete);

            contextMenu.setOnAction(this::contextMenuClick);

            ListView<String> bb= new ListView<>();

            lvLocalFiles.setContextMenu(contextMenu);
            lvLocalFiles.setOnMouseClicked(this::clickListener);
            lvLocalFiles.setOnContextMenuRequested(event ->
                    contextMenu.getItems().forEach(menuItem -> menuItem.setDisable(false)));
//            lvLocalFiles.setOnKeyPressed(v -> {
//                if (v.getCode() == KeyCode.ENTER)
//            });
            lvLocalFiles.setCellFactory(new CellFactory());

            lvServerFiles.setContextMenu(contextMenu);
            lvServerFiles.setOnMouseClicked(this::clickListener);
            lvServerFiles.setOnContextMenuRequested(event -> {
                if (Net.getInstance().isConnected())
                    contextMenu.getItems().forEach(menuItem -> menuItem.setDisable(false));
                else
                    contextMenu.getItems().forEach(menuItem -> menuItem.setDisable(true));
            });
            lvServerFiles.setCellFactory(new CellFactory());

            PaneProgress.setVisible(false);
            //TODO: remove before the production
            fillingStartData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideProgressBar() {
        Runnable task = (() -> {
            try {
                Thread.sleep(DELAY_PROGRESS_BAR);
                Platform.runLater(() -> PaneProgress.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        task.run();
    }

    private void updateProgressDownload(FileSendManager sendManager){
        Platform.runLater(() -> {
            StatusSend status = sendManager.getStatus();
            lStatusDownload.setText(status.getMessageStatus());
            pbDownload.setProgress(status.getGlobalProgress());
        });
    }

    @Override
    public void onConnectionActive() {
        taLog.appendText("Host connected\n");
        sendAuthorization();
    }

    @Override
    public void onConnectionInactive() {
        taLog.appendText("Connection refused\n");
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
        contextMenu.getItems().stream().forEach(menuItem -> menuItem.setDisable(false));
    }

    @Override
    public void onFileList(FileList message) {
        Platform.runLater(() -> fillCurrentDirFiles(
                lvServerFiles,
                tfServerPath,
                message.getList(),
                message.getViewPath())
        );
    }

    @Override
    public void startProcess(FileSendManager sendManager) {
        Platform.runLater(() -> {
            taLog.appendText("Start transfer file: " + sendManager.getCurFileName());
            PaneProgress.setVisible(true);
            lStatusDownload.setText("Calculating time");
            pbDownload.setProgress(0.0);
        });
    }

    @Override
    public void startSendFiles(FileSendManager sendManager) {
        updateProgressDownload(sendManager);
    }

    @Override
    public void sendStatus(FileSendManager sendManager) {
        updateProgressDownload(sendManager);
    }

    @Override
    public void finishedDownload(FileSendManager sendManager) {
        Platform.runLater(() -> {
            taLog.appendText("File transfer finished" + "\n");
            lStatusDownload.setText("Finished transfer files");
            pbDownload.setProgress(100.0);
        });
        hideProgressBar();
    }

    @Override
    public void anExceptionOccurred(Exception e, FileSendManager fileSendManager) {
        Platform.runLater(() -> taLog.appendText("File transfer failed: " + e.getMessage() + "\n"));
        hideProgressBar();
    }
}
