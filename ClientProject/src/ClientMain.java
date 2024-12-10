import connection.ConnectionController;
import service.ChatClientService;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        init();
    }

    private static void init() {
        System.out.print("Nickname: ");

        try {
            ChatClientService.getInstance().createNickname();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

        ConnectionController.getInstance();
    }
}