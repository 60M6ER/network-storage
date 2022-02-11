package com.larionov.storage.client;

import com.larionov.storage.core.files.FileDescription;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class CellFactory implements Callback<ListView<FileDescription>, ListCell<FileDescription>> {
    @Override
    public ListCell<FileDescription> call(ListView<FileDescription> param) {
        return new ListCell<FileDescription>(){

            private String oldText;
            private TextField textField;

            {
                textField = new TextField();
                textField.setOnAction(event -> {
                    FileDescription item = getItem();
                    item.setName(textField.getText());
                    commitEdit(item);
                });

                textField.focusedProperty().addListener( (obs, oldValue, newValue) -> {
                    if (!newValue) {
                        cancelEdit();
                    }
                });
            }

            @Override
            public void startEdit() {
                super.startEdit();
                oldText = getText();
                textField.setText(getText());
                setGraphic(textField);
                setText(null);
                textField.requestFocus();
            }

            @Override
            public void commitEdit(FileDescription newValue) {
                super.commitEdit(newValue);
                setGraphic(null);
                setText(newValue.getName());
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
//                setGraphic(null);
//                setText(oldText);
            }

            @Override
            protected void updateItem(FileDescription item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null){
                    setText(null);
                    setGraphic(null);
                }
                else {
                    setText(item.getName());
                    Image image = null;
                    if (item.equals(FileDescription.getPathToParent()))
                        image = new Image("pictures\\up.png");
                    else if (item.isDirectory())
                        image = new Image("pictures\\folder.png");
                    if (image != null) {
                        ImageView imageView = new ImageView();
                        imageView.setImage(image);
                        imageView.setFitWidth(15);
                        imageView.setFitHeight(15);
                        setGraphic(imageView);
                    }
                }
            }
        };
    }
}
