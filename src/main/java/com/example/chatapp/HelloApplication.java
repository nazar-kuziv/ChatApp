package com.example.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class HelloApplication extends Application {
    private static Stage stage;
    private static ClientConnection clientConnection;
    private static ChatViewController chatViewController;
    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        FXMLLoader login_fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene loginScene = new Scene(login_fxmlLoader.load(), 300, 300);
        stage.setResizable(false);
        stage.setTitle("Login screen");
        stage.setScene(loginScene);
        clientConnection.setLoginViewController(login_fxmlLoader.getController());
        primaryStage.setOnCloseRequest(event -> {
            clientConnection.disconnect();
            System.exit(0);
        });
        stage.show();
    }

    public static void main(String[] args) {
        try {
            clientConnection = new ClientConnection("localhost", 5000);
            clientConnection.start();
            LoginViewController.setClientConnection(clientConnection);
            ChatViewController.setClientConnection(clientConnection);
        } catch (IOException e) {
            e.printStackTrace();
        }
        launch();
    }
    public static void changeSceneAfterLogging(){
        try {
            FXMLLoader chat_fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chat-view.fxml"));
            Scene chatScene = new Scene(chat_fxmlLoader.load(), 800, 600);
            stage.setResizable(false);
            stage.setTitle("Chat App");
            chatViewController = chat_fxmlLoader.getController();
            clientConnection.setChatViewController(chatViewController);
            stage.setScene(chatScene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static File showFileChooserDialog() {
        FileChooser fileChooser = new FileChooser();
        return fileChooser.showOpenDialog(stage);
    }
    public static String showUserToSendDialog() {
        TextInputDialog dialog = new TextInputDialog("User Login");
        dialog.setTitle("Recipient selection");
        dialog.setHeaderText("Recipient selection");
        dialog.setContentText("Enter the login of the user you want to send to:");
        String result = dialog.showAndWait().orElse(null);
        if (!chatViewController.getListOfUsersOnline().contains(result)) {
            result = null;
            Alert alert = new Alert(Alert.AlertType.ERROR, "User with such login was not found.", ButtonType.OK);
            alert.showAndWait();
        }
        return result;
    }
}