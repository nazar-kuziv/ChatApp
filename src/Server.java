import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final ServerSocket serverSocket;
    private final List<ClientThread> clients = new ArrayList<>();
    private final String listOfUsersOnlineKey = "KE6aG20#N*k1M3Y5m!X1";
    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen() throws IOException {
        while(true) {
            Socket newClienSocket = serverSocket.accept();
            ClientThread thread = new ClientThread(newClienSocket, this);
            clients.add(thread);
            thread.start();
        }
    }

    public void SendMessageToAllExceptSender(String message, Socket userSocket) {
        for (ClientThread client: clients) {
            if(client.getSocket() != userSocket && client.getLogin() != null){
                client.sendMessageToUser(message);
            }
        }
    }
    public void SendMessageToAll(String message) {
        for (ClientThread client: clients) {
            if (client.getLogin() != null){
                client.sendMessageToUser(message);
            }
        }
    }
    public boolean checkLoginUniqueness(String checkedLogin){
        for (ClientThread client: clients) {
           if(client.getLogin() != null && client.getLogin().equals(checkedLogin)){
               return false;
           }
        }
        return true;
    }

    public String getListOfUsersOnline(Socket userSocket){
        StringBuilder listOfUsersOnline = new StringBuilder(listOfUsersOnlineKey);
        for (ClientThread client: clients) {
            if(client.getLogin() != null && client.getSocket() != userSocket){
                listOfUsersOnline.append(" ").append(client.getLogin());
            }
        }
        return listOfUsersOnline.toString();
    }

    public void removeClient(ClientThread client){
        clients.remove(client);
    }

    public boolean whisperToUserByLogin(String senderLogin, String recipientLogin, String message) {
        for (ClientThread client: clients) {
            if(client.getLogin() != null && client.getLogin().equals(recipientLogin)){
                client.sendMessageToUser(senderLogin + " whispers: " + message);
                return true;
            }
        }
        return false;
    }

    public ClientThread getClientByLogin(String login){
        for (ClientThread client: clients) {
            if(client.getLogin() != null && client.getLogin().equals(login)){
                return client;
            }
        }
        return null;
    }

}