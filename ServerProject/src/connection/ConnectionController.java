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
import java.util.ArrayList;
import java.util.List;

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
            chattingService.getClientList().keySet().stream()
                    .map(key -> chattingService.getClientList().get(key))
                    .forEach(client -> {
                        try {
                            client.getSocket().close();
                        } catch (IOException e) {

                        }
                    });
            serverSocket.close();
        } catch (IOException e) {

        }
    }

    public void createClient(Socket socket) {
        Client client = new Client(socket);
        startChatting(client);
    }

    private void startChatting(Client client) {
        Thread clientThread = new Thread(() -> {
            try {
                while (true) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
                    String data = reader.readLine();

                    if (data == null) {
                        System.out.println(client.getNickname() + "님이 나갔습니다");
                        break;
                    }

                    System.out.println("data: " + data);
                    MessageDto receivedMessageDto = MessageDto.toDto(data);

                    if (client.getNickname() == null) {
                        client.setNickname(receivedMessageDto.getNickname());
                        ServerMessageDto serverMessageDto = chattingService.addClient(client);
                        sendAllMessage(serverMessageDto);

                    }

                    MessageDto sendingMessageDto = SendingMessageDto.toDto(receivedMessageDto);
                    if (sendingMessageDto.getMessage().equals(ConnectionData.GREETING)) continue;
                    sendAllMessage(sendingMessageDto, client);
                }
            } catch (IOException e) {

            } catch (ParseException e) {

            } catch (NullPointerException e) {
                System.out.println(client.getNickname() + "님의 메시지를 읽는 중 문제가 발생했습니다.");
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
        List<Client> errorClient = new ArrayList<>();

        chattingService.getClientList().keySet().stream()
                .map(key -> chattingService.getClientList().get(key))
                .forEach(client -> {
                    try {
                        sendMessage(client, messageDto);
                    } catch (IOException e) {
                        errorClient.add(client);
                    }
                });

        errorClient.stream()
                .forEach(client -> {
                    try {
                        ServerMessageDto serverMessageDto = chattingService.removeClient(client);
                        client.getSocket().close();
                        sendAllMessage(serverMessageDto);
                    } catch (IOException e) {

                    }
                });
    }

    private void sendAllMessage(MessageDto messageDto, Client origin) {
        List<Client> errorClient = new ArrayList<>();

        chattingService.getClientList().keySet().stream()
                .filter(key -> !key.equals(origin.getId()))
                .map(key -> chattingService.getClientList().get(key))
                .forEach(client -> {
                    try {
                        sendMessage(client, messageDto);
                    } catch (IOException e) {
                        errorClient.add(client);
                    }
                });

        errorClient.stream()
                .forEach(client -> {
                    try {
                        ServerMessageDto serverMessageDto = chattingService.removeClient(client);
                        client.getSocket().close();
                        sendAllMessage(serverMessageDto, client);
                    } catch (IOException e) {

                    }
                });
    }

    private void sendMessage(Client client, MessageDto messageDto) throws IOException {
        PrintWriter writer = new PrintWriter(client.getSocket().getOutputStream());
        writer.println(messageDto.toJson().toString());
        writer.flush();
    }
}
