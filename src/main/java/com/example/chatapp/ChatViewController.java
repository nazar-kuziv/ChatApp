package com.example.chatapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class ChatViewController {
    static ClientConnection client;
    @FXML
    private TextArea chatTextArea;
    @FXML
    private TextField messageTextField;
    @FXML
    private ListView<String> participantsListView;
    @FXML
    private void sendMessage() {
        String message = messageTextField.getText();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            chatTextArea.appendText("- " + message + "\n");
            messageTextField.clear();
        }
    }
    @FXML
    private void initialize() {
        client.updateListOfUsersOnline();
    }
    public static void setClientConnection(ClientConnection clientConnection) {
        client = clientConnection;
    }
    public void updateParticipantList(List<String> usersLogins) {
        Platform.runLater(() -> {
            participantsListView.getItems().clear();
            participantsListView.getItems().addAll(usersLogins);
        });
    }
    public void addNewMessageIntoTextArea(String message) {
        chatTextArea.appendText(message + "\n");
    }
}
