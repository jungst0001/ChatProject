package connection;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client{
    private Long id;
    private Socket socket;
    private String nickname;

    public Client(Socket socket) {
        this.socket = socket;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getNickname() {
        return nickname;
    }
}
