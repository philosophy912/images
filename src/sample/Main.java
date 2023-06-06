package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class Main extends Application {

    private double width = 1280d;
    private double height = 720d;

    private final double size = 240d;


    private double x1;
    private double y1;
    private double x2;
    private double y2;

    private TextArea textArea;
    private Image image;
    private ImageView imageView;
    private StackPane stackPane;

    private final String lineEnd = "\n";

    private String currentFolder = null;
    private final List<File> fileList = new LinkedList<>();
    private int index = 0;

//    private int zoom = 2;


    private Canvas createCanvas(double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {
            x1 = e.getX();
            y1 = e.getY();
            gc.clearRect(0, 0, width, height);

        });
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent e) -> {
            x2 = e.getX();
            y2 = e.getY();
            double w = x2 - x1;
            double h = y2 - y1;
            if (null != image) {
                gc.clearRect(0, 0, width, height);
                gc.strokeRoundRect(x1, y1, w, h, 0, 0);
            }
        });
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent e) -> {
            x2 = e.getX();
            y2 = e.getY();
            double w = x2 - x1;
            double h = y2 - y1;
            String first = (int) x1 + "-" + (int) y1 + "-" + (int) w + "-" + (int) h;
            String second = "[  " + (int) (x1 + w / 2) + "-" + (int) (y1 + h / 2) + "  ]";
            if (null != image) {
                gc.clearRect(0, 0, width, height);
                gc.strokeRoundRect(x1, y1, w, h, 0, 0);
                textArea.appendText(first + "    " + second + lineEnd);
            }
        });
        return canvas;
    }

    private void showImage(Stage primaryStage, String url) {
        image = new Image(url);
        width = image.getWidth();
        height = image.getHeight();
        System.out.println("width is " + width + " height is " + height);
        // 这里需要限制，不要超过屏幕分辨率
        // 被限制的宽高
        double maxWidth = 1000d;
        if (width > maxWidth) {
            width = maxWidth;
        }
        double maxHeight = 700d;
        if (height > maxHeight) {
            height = maxHeight;
        }
        primaryStage.setWidth(width);
        primaryStage.setHeight(height + size);
        imageView.setImage(image);
        imageView.setPreserveRatio(true);
        stackPane.getChildren().remove(1);
        stackPane.getChildren().add(createCanvas(width, height));
    }

    public MenuBar createMenuBar(Stage primaryStage) {
        // 添加菜单栏
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("操作");
        MenuItem menuItem = new MenuItem("打开");
        MenuItem folderItem = new MenuItem("打开文件夹");
        menu.getItems().add(menuItem);
        menu.getItems().add(folderItem);
        menuBar.getMenus().add(menu);
        menuItem.setOnAction((ActionEvent e) -> {
            FileChooser chooser = new FileChooser();
            if (currentFolder != null) {
                chooser.setInitialDirectory(new File(currentFolder));
            }
            chooser.getExtensionFilters().addAll(
//                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
//                    new FileChooser.ExtensionFilter("BMP", "*.bmp"),
//                    new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File file = chooser.showOpenDialog(primaryStage);
            if (null != file) {
                currentFolder = file.getParent();
                String fileFullName = file.getAbsolutePath();
                String url = "file:" + fileFullName;
                textArea.appendText("file:" + fileFullName + lineEnd);
                showImage(primaryStage, url);
            }
        });
        folderItem.setOnAction((ActionEvent e) -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File folder = chooser.showDialog(primaryStage);
            File[] folderFiles = folder.listFiles();
            if (folderFiles != null) {
                for (File file : folderFiles) {
                    if (file.isFile()) {
                        String fileName = file.getAbsolutePath();
                        if (fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
                            fileList.add(file);
                        }
                    }
                }
            }
            if (fileList.size() > 0) {
                File file = fileList.get(index);
                String fileFullName = file.getAbsolutePath();
                String url = "file:" + fileFullName;
                textArea.appendText("file:" + fileFullName + lineEnd);
                showImage(primaryStage, url);
            }
        });
        return menuBar;
    }


    private HBox createHBox(Stage primaryStage) {
        // 添加信息栏
        HBox hBox = new HBox();
        textArea = new TextArea();
        textArea.setPrefHeight(size);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        Button button = new Button("清除");
        button.setMinWidth(size);
        button.setPrefHeight(size);
        button.setOnAction((ActionEvent e) -> textArea.clear());
        HBox.setHgrow(textArea, Priority.ALWAYS);
        hBox.getChildren().addAll(textArea, button);
        textArea.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.LEFT) {
                index = index - 1;
                if (index < 0) {
                    index = fileList.size() - 1;
                }
            } else if (event.getCode() == KeyCode.RIGHT) {
                index = index + 1;
                if (index > fileList.size() - 1) {
                    index = 0;
                }
            }
            File file = fileList.get(index);
            String fileFullName = file.getAbsolutePath();
            textArea.appendText("file:" + fileFullName + lineEnd);
            String url = "file:" + fileFullName;
            showImage(primaryStage, url);

        });
        return hBox;
    }

    @Override
    public void start(Stage primaryStage) {
        imageView = new ImageView();
        MenuBar menuBar = createMenuBar(primaryStage);
        ScrollPane scrollPane = new ScrollPane();
        stackPane = new StackPane();
        stackPane.getChildren().addAll(imageView, createCanvas(width, height));
        scrollPane.setContent(stackPane);
        HBox hBox = createHBox(primaryStage);
        VBox root = new VBox();
        root.getChildren().addAll(menuBar, scrollPane, hBox);
        Scene scene = new Scene(root, width, height + size);
        Image icon = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("cat.png")));
        primaryStage.getIcons().setAll(icon);
        primaryStage.setTitle("图片位置打开器");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
