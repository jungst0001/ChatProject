package connection;

import data.ConnectionData;
import dto.MessageDto;
import org.json.simple.parser.ParseException;
import service.ChatClientService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionController {
    private static ConnectionController controller;
    private ChatClientService service;

    Socket socket;

    public ConnectionController() {
        service = ChatClientService.getInstance();

        Thread thread = new Thread(() -> {
            try {
                socket = new Socket("localhost", ConnectionData.CHAT_PORT);
                PrintWriter writer = new PrintWriter(socket.getOutputStream());

                receiveMessage();

                writer.println(service.joinMessage().toJson().toString());
                writer.flush();

                while (true) {
                    MessageDto messageDto = service.createMessage();
                    if (messageDto.getMessage().equals("q")) {
                        System.out.println("채팅 종료");
                        break;
                    }

                    writer.println(messageDto.toJson().toString());
                    writer.flush();
                }
            } catch (IOException e) {
                System.out.println("서버와 연결할 수 없습니다.");
            } finally {
                close();
            }
        });
        thread.start();
    }

    private void receiveMessage() {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String data = reader.readLine();

                    if (data == null) continue;

                    MessageDto receivedMessageDto = MessageDto.toDto(data);
                    System.out.println(receivedMessageDto.toMessage());
                }
            } catch (IOException e) {

            } catch (ParseException e) {
                System.out.println("메시지 포맷이 잘못됨");
            }
        });
        thread.start();
    }
    
    private void close() {
        service.close();
        try {
            socket.close();
            socket = null;
        } catch (IOException e) {

        }
    }

    public static ConnectionController getInstance() {
        if (controller == null) controller = new ConnectionController();
        return controller;
    }
}
