package service;

import data.ConnectionData;
import dto.MessageDto;

import java.util.Scanner;

public class ChatClientService {
    private static ChatClientService service;

    private String nickname;
    private Scanner sc;

    public ChatClientService() {
        sc = new Scanner(System.in);
    }

    public static ChatClientService getInstance() {
        if (service == null) service = new ChatClientService();
        return service;
    }

    public void startChat() {
    }

    public void createNickname() {
        nickname = sc.nextLine();

        if (nickname.equals("[SERVER]")) throw new RuntimeException("[SERVER] 닉네임은 사용할 수 없습니다.");
    }

    public String getNickname() {
        return nickname;
    }

    public void close() {
        sc.close();
    }

    public MessageDto joinMessage() {
        return new MessageDto(nickname, ConnectionData.GREETING);
    }

    public MessageDto createMessage() {
        String message = sc.nextLine();

        return new MessageDto(nickname, message);
    }
}
