import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers=new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    public  ClientHandler(Socket socket){
        try {
            this.socket=socket;
            this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username=bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER:"+username+" added to chat room.");
        }catch (IOException e){
            closeClientConnection(socket,bufferedReader,bufferedWriter);

        }
    }
    @Override
    public void run() {
        String messageFromClient;
        while(socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                String[] parts = messageFromClient.split(":", 3);
                int length = parts.length;
                if (messageFromClient.startsWith("/")) {
                    if (messageFromClient.equals("/list")) {
                        File folder = new File("datafiles");
                        File[] files = folder.listFiles();
                        assert files != null;
                        for (File file : files) {
                            singleUserFolderList(file.getName(), username);
                        }
                    } else if (messageFromClient.equals("/help")) {
                        System.out.println("test");
                        singleUserFolderList("/list :- Used to list all the files", username);
                        singleUserFolderList("/write <filename> <content-of file> :- Used to write content in desired file name.", username);
                        singleUserFolderList("/read <filename> :- Used to display content of file", username);
                        singleUserFolderList("/append <filename> :- Used to append our data in desired file", username);
                        singleUserFolderList("/quit :- Used to quit from the application.", username);
                    } else if (messageFromClient.equals("/quit")) {
                        closeClientConnection(socket, bufferedReader, bufferedWriter);
                        System.exit(1);
                    } else if (messageFromClient.startsWith("/write")) {
                        String[] fileParam = messageFromClient.split(" ", 3);
                        singleUserFolderList(fileParam[2], username);
                        String contentToWrite = username+": "+fileParam[2];

                        String filePath = "datafiles/" + fileParam[1];
                        try {
                            byte[] contentBytes = contentToWrite.getBytes(StandardCharsets.UTF_8);
                            Path path = Paths.get(filePath);
                            System.out.println(path);
                            Files.write(path, contentBytes);

                            singleUserFolderList("Content has been written to the file: " + filePath, username);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        continue;
                    } else if (messageFromClient.startsWith("/read")) {
                        String[] fileParam = messageFromClient.split(" ", 2);
                        String filePath = "datafiles/" + fileParam[1];
                        try {
                            Path path = Paths.get(filePath);


                            byte[] fileBytes = Files.readAllBytes(path);
                            String contentRead = new String(fileBytes, StandardCharsets.UTF_8);

                            singleUserFolderList(contentRead, username);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (messageFromClient.startsWith("/append")) {
                        String[] fileParam = messageFromClient.split(" ", 3);
                        String filePath = "datafiles/"+fileParam[1];

                        // Content to append
                        String contentToAppend = "\n"+username+": "+fileParam[2];

                        // Use try-with-resources to automatically close the file writer
                        try {
                            // Convert the string content to bytes using UTF-8 encoding
                            byte[] contentBytes = contentToAppend.getBytes(StandardCharsets.UTF_8);

                            // Create a Path object for the file
                            Path path = Paths.get(filePath);

                            // Write the bytes to the file with APPEND option
                            Files.write(path, contentBytes, StandardOpenOption.APPEND);

                            System.out.println("Content has been appended to the file: " + filePath);
                            singleUserFolderList("Content has been appended to the file: " + filePath,username);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        singleUserFolderList("Please enter right command", username);
                    }

                }
                if (length == 3) {
                    String messageType = parts[0];
                    String receiver = parts[1];
                    String content = parts[2];
                    if(messageType.equals("CHAT")){
                        singleUserMessage(content,receiver);
                    }
                    else {
                        System.out.println(receiver+": "+content);
                    }
                    length=0;
                }
                else {
                    broadcastMessage(messageFromClient);
                }
            }catch (Exception e){
                closeClientConnection(socket,bufferedReader,bufferedWriter);
                break;
            }
        }
    }
    public void broadcastMessage(String message){
        try {
            for(ClientHandler clientHandler:clientHandlers){
                if(!clientHandler.username.equals(this.username)){
                    clientHandler.bufferedWriter.write(message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }
        }catch (IOException e){
            closeClientConnection(socket,bufferedReader,bufferedWriter);
        }
    }
    public void closeClientConnection(Socket socket,BufferedReader bufferedReader,BufferedWriter bufferedWriter){
        try {
            if(socket!=null){
                socket.close();
            }
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void  serverMessage(String message){
        for (ClientHandler clientHandler:clientHandlers){
            try {
                clientHandler.bufferedWriter.write("SERVER: "+message);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    public void userMessage(String receiver,String content){
        for(ClientHandler clientHandler:clientHandlers){
            if(clientHandler.username.equals(receiver)){
                try {
                    clientHandler.bufferedWriter.write("SERVER: "+content);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }catch (IOException e){

                }
            }
        }
    }
    public void singleUserMessage(String content,String username){
        for(ClientHandler clientHandler:clientHandlers){
            if(clientHandler.username.equals(username)){
                try {
                    clientHandler.bufferedWriter.write(username+": "+content);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }catch (IOException e){

                }
            }
        }
    }
    public void singleUserFolderList(String content,String username){
        for(ClientHandler clientHandler:clientHandlers){
            if(clientHandler.username.equals(username)){
                try {
                    clientHandler.bufferedWriter.write(content);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }catch (IOException e){

                }
            }
        }
    }

}
