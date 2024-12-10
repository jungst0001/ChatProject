package data;

import org.json.simple.JSONObject;

// data class
public class Message {
    private String nickname;
    private String message; // JSON
    private Long time;

    public Message(String nickname, String message) {
        this.nickname = nickname;
        this.message = message;
    }

    public JSONObject toJSON() {
        JSONObject root = new JSONObject();
        root.put("nickname", nickname);
        root.put("message", message);
        root.put("time", time);

        return root;
    }
}
