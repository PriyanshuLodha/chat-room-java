import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String username;
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    public Client(Socket socket,String username){
        try {
            this.socket=socket;
            this.username=username;
            this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        catch (IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }
    public void  sendMessage(){
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner=new Scanner(System.in);
            while (socket.isConnected()){
                String messageToSend=scanner.nextLine();
                if(messageToSend.equals("/list")||messageToSend.equals("/help")||messageToSend.equals("/quit")||messageToSend.startsWith("/write")||messageToSend.startsWith("/read")||messageToSend.startsWith("/append")){
                    bufferedWriter.write(messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    continue;
                }
                String[] parts = messageToSend.split(":", 3);
                int length=parts.length;
                if (length == 3) {
                    bufferedWriter.write(messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    length=0;
                }
                else {
                    bufferedWriter.write(username+": "+messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        }catch (IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }
    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGrupChat;
                while (socket.isConnected()){
                    try {
                        msgFromGrupChat=bufferedReader.readLine();
                        System.out.println(msgFromGrupChat);
                    }catch (IOException e){
                        closeEverything(socket,bufferedReader,bufferedWriter);
                    }
                }
            }
        }).start();
    }
    public void closeEverything(Socket socket,BufferedReader bufferedReader,BufferedWriter bufferedWriter){
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
    public static void main(String []args) throws IOException {
        Scanner scanner=new Scanner(System.in);
        System.out.println("Enter your username for the group chat:");
        String username=scanner.nextLine();
        Socket socket=new Socket("localhost",12345);
        Client client=new Client(socket,username);
        client.listenForMessage();
        client.sendMessage();
    }
}
