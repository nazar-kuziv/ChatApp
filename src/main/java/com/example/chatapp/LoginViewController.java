package com.example.chatapp;

import javafx.application.Platform;
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
    private void loginIntoChat() {
        client.sendMessage(loginTextField.getText());
    }
    protected void successfulLogin(){
        Platform.runLater(HelloApplication::changeSceneAfterLogging);
    }
    protected void failedLogin(){
        Platform.runLater(() -> messageLabel.setText("Please enter another login"));
    }
    public static void setClientConnection(ClientConnection clientConnection) {
        client = clientConnection;
    }
}