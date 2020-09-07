package controllers;

import arduino.OutputFrame;
import arduino.OutputFrame.Function;
import arduino.SerialCommunicator;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.util.converter.NumberStringConverter;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class ControlPanelController implements Initializable {

    public static final float DEFAULT_KP = 1.5f;
    public static final float DEFAULT_KI = 0.0f;
    public static final float DEFAULT_KD = 0.7f;
    public static final float DEFAULT_X_MIN = -2.5f;
    public static final float DEFAULT_X_MAX = 2.5f;
    public static final float DEFAULT_FILTER_UNCERTAINTY = 7.0f;
    public static final float DEFAULT_FILTER_VARIANCE = 0.3f;
    public static final float DEFAULT_STEP_AMPLITUDE = 22.5f;

    private SerialCommunicator serialCommunicator;

    //PID regulator --------------------------------------------------------------------------------------------------

    @FXML
    private Slider kpSlider;
    @FXML
    private TextField kpField;
    @FXML
    private Button kpButton;

    @FXML
    private Slider kiSlider;
    @FXML
    private TextField kiField;
    @FXML
    private Button kiButton;

    @FXML
    private Slider kdSlider;
    @FXML
    private TextField kdField;
    @FXML
    private Button kdButton;

    @FXML
    private Slider xminSlider;
    @FXML
    private TextField xminField;
    @FXML
    private Button xminButton;

    @FXML
    private Slider xmaxSlider;
    @FXML
    private TextField xmaxField;
    @FXML
    private Button xmaxButton;

    //Kalman filter --------------------------------------------------------------------------------------------------

    @FXML
    private Slider uncertaintySlider;
    @FXML
    private TextField uncertaintyField;
    @FXML
    private Button uncertaintyButton;

    @FXML
    private Slider varianceSlider;
    @FXML
    private TextField varianceField;
    @FXML
    private Button varianceButton;

    //Control system -------------------------------------------------------------------------------------------------

    @FXML
    private Slider inputSlider;
    @FXML
    private TextField inputField;
    @FXML
    private Button inputButton;
    @FXML
    private ToggleButton openLoopButton;
    @FXML
    private ToggleButton manualModeButton;
    @FXML
    private ToggleButton correctionModeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reset();

        NumberStringConverter converter = new NumberStringConverter(Locale.US, "0.00#");
        bind(kpSlider, kpField, kpButton, Function.SET_KP, converter);
        bind(kiSlider, kiField, kiButton, Function.SET_KI, converter);
        bind(kdSlider, kdField, kdButton, Function.SET_KD, converter);
        bind(xminSlider, xminField, xminButton, Function.SET_X_MIN, converter);
        bind(xmaxSlider, xmaxField, xmaxButton, Function.SET_X_MAX, converter);
        bind(uncertaintySlider, uncertaintyField, uncertaintyButton, Function.SET_KALMAN_UNCERTAINTY, converter);
        bind(varianceSlider, varianceField, varianceButton, Function.SET_KALMAN_VARIANCE, converter);
        bind(inputSlider, inputField, inputButton, Function.SET_INPUT, converter);
    }

    private void bind(Slider slider, TextField field, Button button, Function function, NumberStringConverter converter) {
        Bindings.bindBidirectional(field.textProperty(), slider.valueProperty(), converter);
        button.setOnAction(event -> {
            serialCommunicator.push(new OutputFrame(function, (float) slider.getValue()));
        });

        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("[+\\-]?\\d+\\.?\\d*")) {
                if(!field.getStyleClass().contains("text-field-error")) {
                    field.getStyleClass().add("text-field-error");
                }
            } else {
                field.getStyleClass().remove("text-field-error");

            }
        });
    }

    @FXML
    protected synchronized void openLoop(ActionEvent actionEvent) {
        if(openLoopButton.isSelected()) {
            manualModeButton.setDisable(true);
            correctionModeButton.setDisable(true);
            serialCommunicator.push(new OutputFrame(Function.SET_LOOP, 1f)); //open loop
        } else {
            manualModeButton.setDisable(false);
            correctionModeButton.setDisable(false);
            serialCommunicator.push(new OutputFrame(Function.SET_LOOP, 0f)); //close loop
        }
    }

    @FXML
    protected synchronized void manualMode(ActionEvent actionEvent) {
        if(manualModeButton.isSelected()) {
            openLoopButton.setDisable(true);
            correctionModeButton.setDisable(true);
            serialCommunicator.push(new OutputFrame(Function.SET_CONTROL_MODE, 1f)); //manual mode
        } else {
            openLoopButton.setDisable(false);
            correctionModeButton.setDisable(false);
            serialCommunicator.push(new OutputFrame(Function.SET_CONTROL_MODE, 0f)); //automatic mode
        }
    }

    @FXML
    protected synchronized void correctionMode(ActionEvent actionEvent) {
        if(correctionModeButton.isSelected()) {
            openLoopButton.setDisable(true);
            manualModeButton.setDisable(true);
            serialCommunicator.push(new OutputFrame(Function.SET_CORRECTION_MODE, 1f)); //enter correction mode
        } else {
            openLoopButton.setDisable(false);
            manualModeButton.setDisable(false);
            serialCommunicator.push(new OutputFrame(Function.SET_CORRECTION_MODE, 0f)); //exit correction mode
        }
    }

    protected synchronized void reset() {
        if(openLoopButton.isSelected()) {
            openLoopButton.fire();
        }

        if(manualModeButton.isSelected()) {
            manualModeButton.fire();
        }

        if(correctionModeButton.isSelected()) {
            correctionModeButton.fire();
        }

        kpSlider.setValue(DEFAULT_KP);
        kpButton.fire();

        kiSlider.setValue(DEFAULT_KI);
        kiButton.fire();

        kdSlider.setValue(DEFAULT_KD);
        kdButton.fire();

        xminSlider.setValue(DEFAULT_X_MIN);
        xminButton.fire();

        xmaxSlider.setValue(DEFAULT_X_MAX);
        xmaxButton.fire();

        uncertaintySlider.setValue(DEFAULT_FILTER_UNCERTAINTY);
        uncertaintyButton.fire();

        varianceSlider.setValue(DEFAULT_FILTER_VARIANCE);
        varianceButton.fire();

        inputSlider.setValue(DEFAULT_STEP_AMPLITUDE);
        inputButton.fire();
    }

    protected void setSerialCommunicator(SerialCommunicator serialCommunicator) {
        this.serialCommunicator = serialCommunicator;
    }

}
