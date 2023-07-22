package com.example.chatapp;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatViewController {
    static ClientConnection client;
    @FXML
    private TextArea chatTextArea;
    @FXML
    private TextField messageTextField;
    @FXML
    private ListView participantsListView;
    public static void setClientConnection(ClientConnection clientConnection) {
        client = clientConnection;
    }
}