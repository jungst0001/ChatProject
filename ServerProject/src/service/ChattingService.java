package service;

import connection.Client;
import dto.ServerMessageDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class ChattingService {
    private static ChattingService service;

    private Map<Long, Client> clientList;
    private AtomicLong sequence;

    public ChattingService() {
        clientList = new HashMap<>();
        sequence = new AtomicLong(1L);
    }

    public static ChattingService getInstance() {
        if (service == null) service = new ChattingService();
        return service;
    }

    public Map<Long, Client> getClientList() {
        return clientList;
    }

    public ServerMessageDto addClient(Client client) {
        client.setId(sequence.getAndAdd(1L));
        clientList.put(client.getId(), client);
        String message = client.getNickname() + "님이 입장했습니다.\n" + "채팅자 수: " + clientList.size();
        ServerMessageDto serverMessageDto = new ServerMessageDto(message);

        return serverMessageDto;
    }

    public ServerMessageDto removeClient(Client client) {
        clientList.remove(client.getId());
        String message = client.getNickname() + "님이 퇴장하였습니다.\n" + "채팅자 수: " + clientList.size();
        ServerMessageDto serverMessageDto = new ServerMessageDto(message);

        return serverMessageDto;
    }

    // client들에게 메시지 보내기
}
