package com.example.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        FXMLLoader login_fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene loginScene = new Scene(login_fxmlLoader.load(), 300, 300);
        stage.setResizable(false);
        stage.setTitle("Login screen");
        stage.setScene(loginScene);
        stage.show();
    }

    public static void main(String[] args) {
        try {
            ClientConnection clientConnection = new ClientConnection("localhost", 5000);
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
            stage.setScene(chatScene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}