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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class Run extends Application {

    private static double width;
    private static double height;
    private static double error;
    private static int distinctColors = 0;
    private static int k;
    private static String originalPath;
    private static String destPath;
    private static String destFileName;
    private static String extension;
    private static Image originalImg;
    private static Image resultImg;


    public static void setErrorRate() {
        double sum = 0;
        double redOrig, greenOrig, blueOrig, redRes, greenRes, blueRes;
        double redDiff, greenDiff, blueDiff, sumDiff;

        // Get image pixels readers
        PixelReader originalReader = Run.originalImg.getPixelReader();
        PixelReader resultReader = Run.resultImg.getPixelReader();

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                int originalVal = originalReader.getArgb(i, j);
                int resultVal = resultReader.getArgb(i, j);

                redOrig = (originalVal) >> 16 & 0x000000FF;
                greenOrig = (originalVal) >> 8 & 0x000000FF;
                blueOrig = (originalVal) & 0x000000FF;

                redRes = (resultVal) >> 16 & 0x000000FF;
                greenRes = (resultVal) >> 8 & 0x000000FF;
                blueRes = (resultVal) & 0x000000FF;

                redDiff = Math.pow(redOrig - redRes, 2);
                greenDiff = Math.pow(greenOrig - greenRes, 2);
                blueDiff = Math.pow(blueOrig - blueRes, 2);

                sumDiff = redDiff + greenDiff + blueDiff;

                sum += sumDiff;
            }
        }

        Run.error = sum / (Run.width * Run.height);
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
                Label pathLabel, distinctColorsLabel, sizeLabel; // load below
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
                distinctColorsLabel = (Label) startRoot.lookup("#distinctColorsLabel");
                sizeLabel = (Label) startRoot.lookup("#sizeLabel");

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
                    Run.width = shownImg.getWidth();
                    Run.height = shownImg.getHeight();

                    // Set image reference in class
                    Run.originalImg = shownImg;

                    getDistinctColors();
                    distinctColorsLabel.setText(String.valueOf(Run.distinctColors));
                    sizeLabel.setText((int)Run.width + "x" + (int)Run.height);

                } catch(Exception e) {
                    e.printStackTrace();
                }

                vboxContainer.setPrefWidth(0.5 * Run.width);
                vboxContainer.setPrefHeight(0.5 * Run.height);

                // Resize image on container resize
                iView.fitWidthProperty().bind(vboxContainer.widthProperty());
                iView.fitHeightProperty().bind(vboxContainer.heightProperty());
                iView.setImage(shownImg);

                scene = new Scene(originalImgRoot, 0.5 * Run.width, 0.5 * Run.height);
                stage.setTitle("Original Image");
                stage.setScene(scene);

                // Reset path label and distinct colors label

                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        pathLabel.setText("null");
                        distinctColorsLabel.setText("0");
                        sizeLabel.setText("0");

                        // Reset reference and set distinct colors back to 0
                        Run.originalImg = null;
                        Run.distinctColors = 0;
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
                Label errorLabel, runtimeLabel, iterationsLabel;
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
                errorLabel = (Label) startRoot.lookup("#errorLabel");
                runtimeLabel = (Label) startRoot.lookup("#runtimeLabel");
                iterationsLabel = (Label) startRoot.lookup("#iterationsLabel");

                // Set k cluster number from input
                Run.k = Integer.parseInt(kField.getText());

                Image shownImg = null;

                try {
                    // Run KMeans on BufferedImage from input
                    KMeans alg = new KMeans();

                    long startTime = System.currentTimeMillis();

                    BufferedImage resultImg = alg.perform(alg.load(Run.originalPath), Run.k);

                    long endTime = System.currentTimeMillis();

                    alg.save(resultImg, Run.destPath, Run.extension);

                    // Cast to FXImage for displaying
                    shownImg = SwingFXUtils.toFXImage(resultImg, null);

                    // Set result image for error calculation
                    Run.resultImg = shownImg;
                    Run.width = shownImg.getWidth();
                    Run.height = shownImg.getHeight();

                    // Output approximation error
                    Run.setErrorRate();

                    // Build error string

                    StringBuilder errorBuilder = new StringBuilder();

                    errorBuilder.append(Run.error).append(" %");

                    errorLabel.setText(errorBuilder.toString());
                    runtimeLabel.setText(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)) + " seconds");
                    iterationsLabel.setText(String.valueOf(alg.getIterations()));

                } catch(Exception e) {
                    e.printStackTrace();
                }

                vboxContainer.setPrefWidth(0.5 * Run.width);
                vboxContainer.setPrefHeight(0.5 * Run.height);

                // Resize image on container resize
                iView.fitWidthProperty().bind(vboxContainer.widthProperty());
                iView.fitHeightProperty().bind(vboxContainer.heightProperty());
                iView.setImage(shownImg);

                scene = new Scene(resultImgRoot, 0.5 * Run.width, 0.5 * Run.height);
                stage.setTitle("Result Image (K = " + Run.k + ")");
                stage.setScene(scene);

                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        Run.error = 0;
                        errorLabel.setText("0 %");
                        runtimeLabel.setText("0 seconds");
                        iterationsLabel.setText("0");
                    }
                });

                stage.show();
            }
        });

        stage.setTitle("Image Palette Reduction");
        Scene scene = new Scene(startRoot, 640, 360);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // Numara culorile distincte din imagine
    public void getDistinctColors() {
        int prev = 0;
        PixelReader reader = Run.originalImg.getPixelReader();

        for(int j = 0; j < Run.height; j++) {
            for(int i = 0; i < Run.width; i++) {

                if(reader.getArgb(i, j) != prev) {
                    prev = reader.getArgb(i, j);
                    Run.distinctColors++;
                }
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}