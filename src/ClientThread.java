import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Server server;
    private String login;
    private final String youGetFileKey = "Vj1l7FY^7^6$pUQ^NDWw";
    private final String successfulLoginKey = "^yus764y1kcy1l72T5xU";
    private final String failedLoginKey = "%363D5F7GH*CICkaDxp@";
    public ClientThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            InputStream input  = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(input));
            writer = new PrintWriter(output, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run() {
        login = provideLogin();
        server.SendMessageToAllExceptSender("User " + login + " joined the chat!", socket);
        writer.println(successfulLoginKey);
        System.out.println("Client " + login + " joined the chat!");
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                if(!message.isBlank()){
                    if(message.equals("/online")){
                        sendMessageToUser(server.getListOfUsersOnline(socket));
                    }else if(isThisMessagePrivate(message)){
                        String[] messageParts = message.substring(3).split(" ", 2);
                        String recipient = messageParts[0];
                        String filePath = messageParts[1];
                        if(!server.whisperToUserByLogin(login, recipient, filePath)){
                            writer.println("User " + messageParts[0] + " was not found!");
                        }
                    }else if(isThisMessageWithFile(message)){
                        String[] messageParts = message.substring(6).split(" ", 2);
                        if(!sendFileToUser(messageParts[0], messageParts[1])){
                            System.out.println(messageParts[0]);
                            writer.println("There has been an error");
                        }
                    }else{
                        System.out.println(this.login + ": " +message);
                        server.SendMessageToAllExceptSender(this.login + ": " + message, socket);
                    }
                }
            }
            server.SendMessageToAll("User " + this.login + " left the chat!");
            System.out.println("Client "+ this.login + " left!");
            server.removeClient(this);
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendMessageToUser(String message){
        writer.println(message);
    }
    private String provideLogin() {
        while (true){
            try {
                String messageFromUser = reader.readLine();
                if (messageFromUser != null) {
                    if(messageFromUser.isBlank()){
                        writer.println(failedLoginKey);
                    }else if(!server.checkLoginUniqueness(messageFromUser)){
                        writer.println(failedLoginKey);
                    }else{
                        return messageFromUser;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public boolean sendFileToUser(String recipient, String filePath) {
        ClientThread recipientClient = server.getClientByLogin(recipient);
        if (recipientClient != null) {
            recipientClient.sendMessageToUser("YouGetFile"+ youGetFileKey + " "  + filePath);
            try {
                File file = new File(filePath);
                if (file.exists() && file.isFile()) {
                    long fileSize = file.length();
                    recipientClient.sendMessageToUser(String.valueOf(fileSize));
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[4 * 1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        recipientClient.getSocket().getOutputStream().write(buffer, 0, bytesRead);
                    }
                    fileInputStream.close();
                    return true;
                } else {
                    System.out.println("File not found: " + filePath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private boolean isThisMessagePrivate(String message){
        return message.startsWith("/w");
    }
    private boolean isThisMessageWithFile(String message) {
        return message.startsWith("/file");
    }
    public String getLogin() {
        return login;
    }
    public Socket getSocket() {
        return socket;
    }
}
