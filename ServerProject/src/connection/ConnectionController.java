package connection;

import data.ConnectionData;
import dto.MessageDto;
import dto.SendingMessageDto;
import dto.ServerMessageDto;
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

// 연결을 담당하는 컨트롤러
// 네트워크 입출력을 전반적으로 담당함
public class ConnectionController {
    private static ConnectionController controller;

    private ServerSocket serverSocket;
    private ChattingService chattingService = ChattingService.getInstance();

    // 싱글턴 패턴으로 컨트롤러 객체는 하나로 선언
    public static ConnectionController getInstance() {
        if (controller == null) controller = new ConnectionController();
        return controller;
    }

    public ConnectionController() {
        // TODO: Logger로 변환
        System.out.println("서버 시작");
        
        // 서버 스레드 생성 후 서버 소켓을 통해 listen 수행
        // 만약 서버 소켓으로부터 연결이 들어오면 accept 후 클라이언트 객체 생성
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

    // TODO: ServerMain에 수동으로 종료할 때 사용
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

    // 
    // parameter: Socket (클라이언트와 연결된 소켓)
    // 소켓을 기반으로 클라이언트 객체를 생성
    
    private void createClient(Socket socket) {
        Client client = new Client(socket);
        startChatting(client);
    }

    // parameter: Client
    // 각 Client는 독립적 스레드로 동작
    // Client의 socket을 이용하여 receive 부분과 send 부분을 구체화
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

    // parameter: MessageDto
    // 현재 모든 Client들에게 메시지를 전송함
    private void sendAllMessage(MessageDto messageDto) {
        List<Client> errorClient = new ArrayList<>();

        chattingService.getClientList().keySet().stream()
                .map(key -> chattingService.getClientList().get(key))
                .forEach(client -> {
                    try {
                        sendMessage(messageDto, client);
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

    // parameter: MessageDto, Client
    // exludedOrigin을 재외한 모든 Client들에게 메시지를 전송함
    private void sendAllMessage(MessageDto messageDto, Client excludedOrigin) {
        List<Client> errorClient = new ArrayList<>();

        chattingService.getClientList().keySet().stream()
                .filter(key -> !key.equals(excludedOrigin.getId()))
                .map(key -> chattingService.getClientList().get(key))
                .forEach(client -> {
                    try {
                        sendMessage(messageDto, client);
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

    // parameter: MessageDto, Client
    // 단일 client에게 메시지를 전송함
    private void sendMessage(MessageDto messageDto, Client client) throws IOException {
        PrintWriter writer = new PrintWriter(client.getSocket().getOutputStream());
        writer.println(messageDto.toJson().toString());
        writer.flush();
    }
}
