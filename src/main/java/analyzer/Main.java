package analyzer;

import analyzer.common.MessageJtoJ;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class Main extends Application {

    private static Socket clientSocket;
    private static final String version = "5.0";

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
//      primaryStage.getIcons().add(new Image("/icon.png"));
        ((Controller)loader.getController()).initController(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            int port = 0;
            boolean onlyVersion = false;
            for (int z = 0; z < args.length; ++z)
            {
                if (args[z].equals("--port")) {
                    if (z + 1 < args.length)
                        port = Integer.parseInt(args[z + 1]);
                }
                else if (args[z].equals("--version"))
                    onlyVersion = true;
            }
            System.out.println("port is " + port);
            clientSocket = new Socket("localhost", port);

            ois = new ObjectInputStream(clientSocket.getInputStream());
            oos = new ObjectOutputStream(clientSocket.getOutputStream());

            if (onlyVersion) {
                sendMessage(new MessageJtoJ(version, "version"));
                sendMessage(new MessageJtoJ(getClassBuildTime(), "build"));

                ois.close();
                oos.close();
                System.exit(0);
            }

            MessageJtoJ recv = recvMessage();
            if (recv.getCommand().equals("StatDirPath")) {
                StatDirPath = recv.getMessage();
                if (Files.notExists(Paths.get(StatDirPath)))
                    throw new Exception("path not exist: " + StatDirPath);
                char last = StatDirPath.charAt(StatDirPath.length() - 1);
                if (last != '/' && last != '\\')
                    StatDirPath += "/";
                System.out.println("[PPPA]: set path =" + StatDirPath);
            }
        }
        catch(Exception ex) {
            System.out.println(ex.getMessage());
            // comment for use without Visualizer
            System.exit(-1);
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
            if (answer.getCommand().equals("readStat"))
                return answer.getMessage();
            else {
                System.out.println("[PPPA]: c=" + answer.getCommand());
                throw new Exception("wrong answer");
            }
        } catch (Exception ex) {
            System.out.println("[PPPA]: error of readStat: " + ex.getMessage());
        }
        return null;
    }

    private static String getClassBuildTime() {
        Date d = null;
        Class<?> currentClass = new Object() {
        }.getClass().getEnclosingClass();
        URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
        if (resource != null) {
            if (resource.getProtocol().equals("file")) {
                try {
                    d = new Date(new File(resource.toURI()).lastModified());
                } catch (URISyntaxException ignored) {
                }
            } else if (resource.getProtocol().equals("jar")) {
                String path = resource.getPath();
                d = new Date(new File(path.substring(5, path.indexOf("!"))).lastModified());
            } else if (resource.getProtocol().equals("zip")) {
                String path = resource.getPath();
                File jarFileOnDisk = new File(path.substring(0, path.indexOf("!")));
                try (JarFile jf = new JarFile(jarFileOnDisk)) {
                    ZipEntry ze = jf.getEntry(path.substring(path.indexOf("!") + 2));
                    long zeTimeLong = ze.getTime();
                    Date zeTimeDate = new Date(zeTimeLong);
                    d = zeTimeDate;
                } catch (IOException | RuntimeException ignored) {
                }
            }
        }

        if (d != null) {
            String pattern = "MMM dd yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern, Locale.ENGLISH);
            return df.format(d);
        }
        else
            return "NULL";
    }
}
