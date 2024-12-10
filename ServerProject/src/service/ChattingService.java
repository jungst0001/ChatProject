package service;

import connection.Client;
import dto.ServerMessageDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

// 채팅을 담당하는 서비스
// Client가 연결될 때 발생하는 로직 및 메시지의 형태를 구현함
public class ChattingService {
    private static ChattingService service;

    private Map<Long, Client> clientList;
    private AtomicLong sequence;

    private ChattingService() {
        clientList = new HashMap<>();
        sequence = new AtomicLong(1L);
    }

    // 싱글턴 패턴으로 서비스 객체는 하나로 선언 
    public static ChattingService getInstance() {
        if (service == null) service = new ChattingService();
        return service;
    }

    public Map<Long, Client> getClientList() {
        return clientList;
    }

    // Client가 연결될 때의 처리, 메시지 DTO 객체를 구현
    public ServerMessageDto addClient(Client client) {
        client.setId(sequence.getAndAdd(1L));
        clientList.put(client.getId(), client);
        System.out.println(client.getNickname() + "님이 입장했습니다.\n" + "채팅자 수: " + clientList.size());
        String message = client.getNickname() + "님이 입장했습니다.\n" + "채팅자 수: " + clientList.size();
        ServerMessageDto serverMessageDto = new ServerMessageDto(message);

        return serverMessageDto;
    }

    // Client가 제거될 때의 처리, 메시지 DTO 객체를 구현
    public ServerMessageDto removeClient(Client client) {
        clientList.remove(client.getId());
        String message = client.getNickname() + "님이 퇴장하였습니다.\n" + "채팅자 수: " + clientList.size();
        ServerMessageDto serverMessageDto = new ServerMessageDto(message);

        return serverMessageDto;
    }
}
