import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;
    public void startServer(ServerSocket serverSocket){
        try {
            while(!serverSocket.isClosed()){
                Socket socket=serverSocket.accept();
                System.out.println("New Client added...");
                ClientHandler clientHandler=new ClientHandler(socket);
                Thread thread=new Thread(clientHandler);
                thread.start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (socket.isConnected()) {
                            Scanner scanner = new Scanner(System.in);
                            String messageFromServer = scanner.nextLine();
                            String[] parts = messageFromServer.split(":", 3);
                            int length=parts.length;
                            if (length == 3) {
                                String messageType = parts[0];
                                String receiver = parts[1];
                                String content = parts[2];
                                clientHandler.userMessage(receiver,content);
                                length=0;
                            }else {
                                clientHandler.serverMessage(messageFromServer);
                            }


                        }
                    }
                }).start();
            }
        }catch (IOException e){
            System.out.println(e);
        }
    }

    public Server(ServerSocket serverSocket){
        this.serverSocket=serverSocket;
    }
    public void stopServerSocket(){
        try {
            if(serverSocket!=null){
                serverSocket.close();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String []args){
        try {
            ServerSocket socket=new ServerSocket(12345);
            Server server=new Server(socket);
            server.startServer(socket);
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
