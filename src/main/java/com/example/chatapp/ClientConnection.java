package com.example.chatapp;

import java.io.*;
import java.net.Socket;


public class ClientConnection extends Thread {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private boolean isLogined = false;
    private boolean wasLoginChecked = false;
    private final String youGetFileKey = "Vj1l7FY^7^6$pUQ^NDWw";
    private final String successfulLoginKey = "^yus764y1kcy1l72T5xU";
    private final String failedLoginKey = "%363D5F7GH*CICkaDxp@";
    public ClientConnection(String address, int port) throws IOException {
        socket = new Socket(address, port);
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        writer = new PrintWriter(output, true);
        reader = new BufferedReader(new InputStreamReader(input));
    }

    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String message;
            while ((message = reader.readLine()) != null) {
                if(message.equals(successfulLoginKey)){
                    isLogined = true;
                    wasLoginChecked = true;
                } else if (message.equals(failedLoginKey)) {
                    isLogined = false;
                    wasLoginChecked = true;
                } else if (isThisMessageWithFile(message)) {
                    sendFileToServer(message);
                } else if (message.startsWith("YouGetFile" + youGetFileKey)) {
                    receiveFileFromServer(dataInputStream, message);
                } else {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendFileToServer(String message) {
        try {
            String[] messageParts = message.substring(6).split(" ", 2);
            String recipient = messageParts[0];
            String filePath = messageParts[1];

            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                long fileSize = file.length();

                writer.println("/file " + recipient + " " + file.getName());
                writer.println(fileSize);
                writer.flush();

                FileInputStream fileInputStream = new FileInputStream(file);

                byte[] buffer = new byte[4 * 1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    socket.getOutputStream().write(buffer, 0, bytesRead);
                }
                fileInputStream.close();
            } else {
                System.out.println("File not found: " + filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void receiveFileFromServer(DataInputStream dataInputStream, String message) {
        try {
            String folderPath = "received/";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                if(!folder.mkdirs()){
                    throw new RuntimeException("Failed to create directory: " + folderPath);
                }
            }
            File file = new File(message.split(" ", 2)[1]);
            String fileName = file.getName();
            FileOutputStream fileOutputStream = new FileOutputStream(folderPath + fileName);
            String sFileSize = dataInputStream.readLine();
            System.out.println("Receiving file size: " + sFileSize);
            long fileSize = Long.parseLong(sFileSize);
            byte[] buffer = new byte[4 * 1024];
            int bytesRead;
            long bytesRemaining = fileSize;

            while (bytesRemaining > 0) {
                bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining));
                if (bytesRead == -1) {
                    break;
                }
                fileOutputStream.write(buffer, 0, bytesRead);
                bytesRemaining -= bytesRead;
            }

            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println("Saved at: " + folderPath + fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
    private boolean isThisMessageWithFile(String message) {
        return message.startsWith("/file");
    }
    protected boolean getIsLogined(){
        while (!wasLoginChecked) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        wasLoginChecked = false;
        return isLogined;
    }
}
