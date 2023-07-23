package com.example.chatapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    public static void setClientConnection(ClientConnection clientConnection) {
        client = clientConnection;
    }
    public void initialize() {
        executorService.scheduleAtFixedRate(this::checkForUserListUpdate, 0, 1, TimeUnit.SECONDS);
    }
    private void checkForUserListUpdate() {
        if (client.isUpdatingUserListNecessary()) {
            updateParticipantList();
        }
    }
    private void updateParticipantList() {
        Platform.runLater(() -> {
            participantsListView.getItems().clear();
            participantsListView.getItems().addAll(client.getUsersLogins());
        });
        client.userListHasBeenUpdated();
    }
}
