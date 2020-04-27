import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Run extends Application {

    private static double width;
    private static double height;
    private static double error;
    private static int k;
    private static String originalPath;
    private static String destPath;
    private static String destFileName;
    private static String extension;
    private static Image originalImg;
    private static Image resultImg;

    public static void setErrorRate() {
        long originalValue = 0;
        long resultValue = 0;
        double error = 0;

        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                PixelReader originalReader = Run.originalImg.getPixelReader();
                PixelReader resultReader = Run.resultImg.getPixelReader();

                originalValue += Math.abs(originalReader.getArgb(i, j));
                resultValue += Math.abs(resultReader.getArgb(i, j));
            }
        }

        error = (double) (originalValue - resultValue) / originalValue;
        Run.error = Math.abs(error) * 100;
    }

    @Override
    public void start(Stage stage) throws Exception {

        Parent startRoot = FXMLLoader.load(getClass().getResource("resources/start.fxml"));
        Button imgLoaderBtn = (Button) startRoot.lookup("#loaderButton");
        Button exitBtn = (Button) startRoot.lookup("#exitBtn");

        // Exit button handler
        exitBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.exit(0);
            }
        });

        // Set click event listener
        imgLoaderBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                VBox vboxContainer; // Load below
                Scene scene;
                Label pathLabel; // load below
                ImageView iView; // load below
                Parent originalImgRoot = null;
                FileChooser loader = new FileChooser(); // Pop-up dialog for file choosing
                Stage stage = new Stage();

                try {
                    originalImgRoot = FXMLLoader.load(getClass().getResource("resources/original.fxml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                vboxContainer = (VBox) originalImgRoot.lookup("#vboxContainer");
                iView = (ImageView) originalImgRoot.lookup("#imgViewer");
                String imgPath = loader.showOpenDialog(stage.getOwner()).getPath(); // Get file from input

                Run.originalPath = imgPath;

                pathLabel = (Label) startRoot.lookup("#imgPath");
                pathLabel.setText(imgPath);

                Image shownImg = null;

                try {
                    Path path = Paths.get(Run.originalPath);
                    String fileName = path.getFileName().toString();
                    int index = fileName.lastIndexOf(".");

                    Run.destFileName = fileName.substring(0, index);
                    Run.destFileName += "_result";

                    // Get extension and set destination file name
                    Run.extension = fileName.substring(index);
                    Run.destFileName += Run.extension;

                    // Build destination path
                    String parent = path.getParent().toString(); // Get parent folder
                    Run.destPath = parent + "\\" + Run.destFileName;

                    // Create new JavaFX image
                    shownImg = new Image(new FileInputStream(Run.originalPath));

                    // Set image reference for error calculation
                    Run.originalImg = shownImg;

                } catch(Exception e) {
                    e.printStackTrace();
                }

                Run.width = shownImg.getWidth();
                Run.height = shownImg.getHeight();

                vboxContainer.setPrefWidth(0.5 * Run.width);
                vboxContainer.setPrefHeight(0.5 * Run.height);

                // Resize image on container resize
                iView.fitWidthProperty().bind(vboxContainer.widthProperty());
                iView.fitHeightProperty().bind(vboxContainer.heightProperty());
                iView.setImage(shownImg);

                scene = new Scene(originalImgRoot, 0.5 * Run.width, 0.5 * Run.height);
                stage.setTitle("Original Image");
                stage.setScene(scene);

                // Reset path label to default on stage close
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        pathLabel.setText("null");
                    }
                });
                stage.show();
            }
        });

        // 'Run KMeans' button listener
        Button runBtn = (Button) startRoot.lookup("#runButton");

        runBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {

                VBox vboxContainer; // Load below
                Scene scene;
                TextField kField; // Load below
                ImageView iView; // Load below
                Parent resultImgRoot = null;
                Stage stage = new Stage();

                try {
                    resultImgRoot = FXMLLoader.load(getClass().getResource("resources/result.fxml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                vboxContainer = (VBox) resultImgRoot.lookup("#vboxContainer");
                iView = (ImageView) resultImgRoot.lookup("#imgViewer");
                kField = (TextField) startRoot.lookup("#kNumber");

                // Set k cluster number from input
                Run.k = Integer.parseInt(kField.getText());

                Image shownImg = null;

                try {
                    // Run KMeans on BufferedImage from input
                    KMeans alg = new KMeans();
                    BufferedImage resultImg = alg.perform(alg.load(Run.originalPath), Run.k);
                    alg.save(resultImg, Run.destPath, Run.extension);

                    // Cast to FXImage for displaying
                    shownImg = SwingFXUtils.toFXImage(resultImg, null);

                    // Set result image for error calculation
                    Run.resultImg = shownImg;

                    Run.setErrorRate();
                    System.out.println(Run.error + " %");

                } catch(Exception e) {
                    e.printStackTrace();
                }

                Run.width = shownImg.getWidth();
                Run.height = shownImg.getHeight();

                vboxContainer.setPrefWidth(0.5 * Run.width);
                vboxContainer.setPrefHeight(0.5 * Run.height);

                // Resize image on container resize
                iView.fitWidthProperty().bind(vboxContainer.widthProperty());
                iView.fitHeightProperty().bind(vboxContainer.heightProperty());
                iView.setImage(shownImg);

                scene = new Scene(resultImgRoot, 0.5 * Run.width, 0.5 * Run.height);
                stage.setTitle("Result Image");
                stage.setScene(scene);
                stage.show();
            }
        });

        stage.setTitle("Image Palette Reduction");
        Scene scene = new Scene(startRoot, 640, 360);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}