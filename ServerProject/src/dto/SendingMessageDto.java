package dto;

import org.json.simple.JSONObject;

public class SendingMessageDto extends MessageDto {
    private Long time;

    public SendingMessageDto(String nickname, String message) {
        super(nickname, message);
        time = System.currentTimeMillis();
    }

    public Long getTime() {
        return time;
    }

    @Override
    public JSONObject toJson() {
        JSONObject root = super.toJson();
        root.put("time", time);

        return root;
    }

    public static MessageDto toDto(MessageDto messageDto) {
        return new SendingMessageDto(messageDto.getNickname(), messageDto.getMessage());
    }
}
