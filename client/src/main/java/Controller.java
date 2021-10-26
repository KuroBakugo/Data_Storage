import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private static String REPO = "client/repo";
    private static final byte[] buffer = new byte[1024];
    public ListView<String> listView;
    public TextField input;
    private DataInputStream is;
    private DataOutputStream os;

    public void send(ActionEvent actionEvent) throws Exception {
        String msg = input.getText();
        input.clear();
        sendFile(msg);
    }

    private void sendFile(String msg) throws IOException {
        Path file = Paths.get(REPO, msg);
        long size = Files.size(file);
        if (Files.exists(file)) {
            os.writeUTF(msg);
            os.writeLong(size);

            InputStream inputStream = Files.newInputStream(file);
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        }else {
            os.writeUTF(msg);
            os.flush();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            fillFilesInCurrent();
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        String msg = is.readUTF();
                        Platform.runLater(() -> listView.getItems().add(msg));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillFilesInCurrent() throws IOException {
        listView.getItems().clear();
        listView.getItems().addAll(
                Files.list(Paths.get(REPO))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList())
        );
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = listView.getSelectionModel().getSelectedItem();
                input.setText(item);
            }
        });
    }
}
