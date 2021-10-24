import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> listView;
    public TextField input;
    private DataInputStream is;
    private DataOutputStream os;

    public void send(ActionEvent actionEvent) throws Exception {
        String msg = input.getText();
        input.clear();
        os.writeUTF(msg);
        os.flush();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        String msg = is.readUTF();
                        Platform.runLater(() -> listView.getItems().add(msg));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
