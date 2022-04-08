package analyzer;

import analyzer.common.MessageJtoJ;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends Application {

    private static Socket clientSocket;
    private static final String version = "1.0";

    public static ObjectInputStream ois;
    public static ObjectOutputStream oos;

    public static String StatDirPath = System.getProperty("user.dir") + "/statistics/";

    public static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent p = loader.load();
        primaryStage.setTitle("Анализатор статистик");
        Scene scene = new Scene(p, 700, 600);
        scene.getStylesheets().add("analyzer/series.css");
        scene.getStylesheets().add("analyzer/bootstrap3.css");
        scene.getStylesheets().add("analyzer/progressBar.css");
        primaryStage.setScene(scene);
//        primaryStage.getIcons().add(new Image("/icon.png"));
        ((Controller)loader.getController()).initController(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            int port = 0;
            for (int z = 0; z < args.length; ++z)
            {
                if (args[z].equals("--port"))
                    if (z + 1 < args.length)
                        port = Integer.parseInt(args[z + 1]);
            }
            System.out.println("port is " + port);
            clientSocket = new Socket("localhost", port);

            ois = new ObjectInputStream(clientSocket.getInputStream());
            oos = new ObjectOutputStream(clientSocket.getOutputStream());

            sendMessage(new MessageJtoJ(version, "version"));
            MessageJtoJ recv = recvMessage();
            if (recv.getCommand().equals("StatDirPath")) {
                StatDirPath = recv.getMessage();
                if (Files.notExists(Paths.get(StatDirPath)))
                    throw new Exception("path not exist: " + StatDirPath);
            }
        }
        catch(Exception ex) {
            System.out.println(ex.getMessage());
            //System.exit(-1);
        }
        launch(args);
    }

    private static void sendMessage(MessageJtoJ toSend) throws Exception
    {
        oos.writeObject(toSend);
    }

    private static MessageJtoJ recvMessage() throws Exception
    {
        return (MessageJtoJ) ois.readObject();
    }

    public static String readStat(String path) throws Exception {
        try {
            Main.sendMessage(new MessageJtoJ(path, "readStat"));
            MessageJtoJ answer = Main.recvMessage();
            if (answer.getCommand() == "readStat")
                return answer.getMessage();
            else
                throw new Exception("wrong answer");
        } catch (Exception ex) {
            System.out.println("error of readStat: " + ex.getMessage());
        }
        return null;
    }
}
