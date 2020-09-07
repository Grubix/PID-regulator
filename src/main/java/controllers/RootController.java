package controllers;

import arduino.SerialCommunicator;
import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RootController implements Initializable {

    private class Task implements Runnable {
        @Override
        public void run() {
            Platform.runLater(() -> {
                serialCommunicator.read();
                serialCommunicator.write();
                chartController.update();
            });
        }
    }

    public static final long TASK_DELAY = 0;

    public static final long TASK_PERIOD = 4900;

    private Scene scene;

    private ScheduledExecutorService executor;

    private SerialCommunicator serialCommunicator;

    @FXML
    private Menu portMenu;

    @FXML
    private CheckMenuItem itemThemeLight;

    @FXML
    private CheckMenuItem itemThemeDark;

    @FXML
    private MenuItem itemSettingsRestore;

    @FXML
    private HBox mainContainer;

    @FXML
    private ChartController chartController;

    @FXML
    private ControlPanelController controlPanelController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serialCommunicator = new SerialCommunicator();
        chartController.setSerialCommunicator(serialCommunicator);
        controlPanelController.setSerialCommunicator(serialCommunicator);

        portMenu.getItems().get(0).setOnAction(event -> {
            serialCommunicator.disconnect();
            executor.shutdown();

            mainContainer.setDisable(true);
            chartController.reset(null);
            controlPanelController.reset();
            portMenu.getItems().get(0).setDisable(true);
            portMenu.setText("Serial port");
            itemSettingsRestore.setDisable(true);
        });

        //TODO dodac usuwanie portu z listy jezeli sie z nim polaczono
        portMenu.showingProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                portMenu.getItems().remove(2, portMenu.getItems().size());

                for(SerialPort port : SerialPort.getCommPorts()) {
                    MenuItem menuItem = new CheckMenuItem(port.getSystemPortName());
                    menuItem.setOnAction(event -> {
                        try {
                            serialCommunicator.connect(port);
                            executor = Executors.newSingleThreadScheduledExecutor();
                            executor.scheduleAtFixedRate(new Task(), TASK_DELAY, TASK_PERIOD, TimeUnit.MICROSECONDS);

                            mainContainer.setDisable(false);
                            portMenu.getItems().get(0).setDisable(false);
                            portMenu.setText("Serial port | " + port.getSystemPortName());
                            itemSettingsRestore.setDisable(false);

                            controlPanelController.reset();
                            chartController.reset(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    portMenu.getItems().add(menuItem);
                }
            }
        });

        itemThemeLight.setOnAction(event -> {
            if(scene.getStylesheets().size() == 2) {
                scene.getStylesheets().remove(1);
                itemThemeLight.setSelected(true);
                itemThemeDark.setSelected(false);
            } else {
                itemThemeDark.fire();
            }
        });

        itemThemeDark.setOnAction(event -> {
            if(scene.getStylesheets().size() == 1) {
                scene.getStylesheets().add(getClass().getResource("/modena_dark.css").toExternalForm());
                itemThemeLight.setSelected(false);
                itemThemeDark.setSelected(true);
            } else {
                itemThemeLight.fire();
            }
        });

        itemSettingsRestore.setOnAction(event -> controlPanelController.reset());
    }

    public void setMainScene(Scene scene){
        this.scene = scene;
    }

}
