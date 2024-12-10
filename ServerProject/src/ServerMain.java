import connection.ConnectionController;
import service.ChattingService;

// Main Class
public class ServerMain {

    public static void main(String[] args) {
        init();
    }

    private static void init() {
        ConnectionController.getInstance();
        ChattingService.getInstance();
    }

}