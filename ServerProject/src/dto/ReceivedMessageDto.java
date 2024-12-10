package dto;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReceivedMessageDto extends MessageDto {

    public ReceivedMessageDto(String nickname, String message) {
        super(nickname, message);
    }
}
