package dto;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// DTO class
public class MessageDto {
    private String nickname;
    private String message;

    public MessageDto(String nickname, String message) {
        this.nickname = nickname;
        this.message = message;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public static MessageDto toDto(String received) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject root = (JSONObject) parser.parse(received);

        return new MessageDto((String) root.get("nickname"), (String) root.get("message"));
    }

    public JSONObject toJson() {
        JSONObject root = new JSONObject();
        root.put("nickname", nickname);
        root.put("message", message);

        return root;
    }
}
