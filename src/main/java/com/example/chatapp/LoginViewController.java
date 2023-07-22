package com.example.chatapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginViewController {
    static ClientConnection client;
    @FXML
    private TextField loginTextField;
    @FXML
    private Label messageLabel;
    @FXML
    protected void loginIntoChat() {
        client.sendMessage(loginTextField.getText());
        if(client.getIsLogined()){
            HelloApplication.changeSceneAfterLogging();
        } else {
            messageLabel.setText("Please enter another login");
        }
    }
    public static void setClientConnection(ClientConnection clientConnection) {
        client = clientConnection;
    }
}