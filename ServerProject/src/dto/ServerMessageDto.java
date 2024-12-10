package dto;

import org.json.simple.JSONObject;

public class ServerMessageDto extends MessageDto {

    public ServerMessageDto(String message) {
        super("[SERVER]", message);
    }
}
