package connection;

import data.ConnectionData;
import dto.MessageDto;
import dto.ReceivedMessageDto;
import dto.SendingMessageDto;
import dto.ServerMessageDto;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import service.ChattingService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionController {
    private static ConnectionController controller;

    private ServerSocket serverSocket;
    private ChattingService chattingService = ChattingService.getInstance();

    public static ConnectionController getInstance() {
        if (controller == null) controller = new ConnectionController();
        return controller;
    }

    public ConnectionController() {
        System.out.println("서버 시작");
        
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress("localhost", ConnectionData.CHAT_PORT));

                while (true) {
                    Socket socket = serverSocket.accept();
//                InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();

                    controller.createClient(socket);
                }

            } catch (IOException e) {
                System.out.println("Socket 통신 중 문제가 발생하였습니다.");
                System.exit(0);
            } finally {
                if (!serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {

                    }
                }
            }
        });
        
        serverThread.start();
    }

    public void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {

        }
    }

    public void createClient(Socket socket) {
        Client client = new Client(socket);

        chattingService.addClient(client);
        startChatting(client);
    }

    private void startChatting(Client client) {
        Thread clientThread = new Thread(() -> {
            try {
                while (true) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
                    PrintWriter writer = new PrintWriter(client.getSocket().getOutputStream());

                    String data = reader.readLine();

                    MessageDto receivedMessageDto = MessageDto.toDto(data);

                    if (client.getNickname() == null) {
                        client.setNickname(receivedMessageDto.getNickname());
                        ServerMessageDto serverMessageDto = chattingService.addClient(client);
                        sendAllMessage(serverMessageDto);

                    }

                    MessageDto sendingMessageDto = SendingMessageDto.toDto(receivedMessageDto);
                    sendAllMessage(sendingMessageDto);
                }
            } catch (IOException e) {

            } catch (ParseException e) {

            } finally {
                try {
                    ServerMessageDto serverMessageDto = chattingService.removeClient(client);
                    client.getSocket().close();

                    sendAllMessage(serverMessageDto);
                } catch (IOException e) {

                }
            }
        });

        clientThread.start();
    }

    private void sendAllMessage(MessageDto messageDto) {
        chattingService.getClientList().keySet().stream()
                .map(key -> chattingService.getClientList().get(key))
                .forEach(client1 -> {
                    sendMessage(client1, messageDto);
                });
    }

    private void sendMessage(Client client, MessageDto messageDto) throws RuntimeException {
        try {
            PrintWriter writer = new PrintWriter(client.getSocket().getOutputStream());
            writer.println(messageDto.toJson().toString());
            writer.flush();
        } catch (IOException e) {

        } finally {
            try {
                ServerMessageDto serverMessageDto = chattingService.removeClient(client);
                client.getSocket().close();
                sendAllMessage(serverMessageDto);
            } catch (IOException e) {

            }
        }
    }
}
