package controllers;

import arduino.InputFrame;
import arduino.PIDRegulator;
import arduino.SerialCommunicator;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import org.controlsfx.control.CheckComboBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ChartController implements Initializable {

    private static class Series {

        private final XYChart.Series<Number, Number> series;

        private final List<XYChart.Data<Number, Number>> data;

        private boolean hidden;

        private int lowerRange;

        private int upperRange;

        private Series(String name, int lowerRange, int upperRange) {
            series = new XYChart.Series<>();
            data = new ArrayList<>();
            series.setName(name);
            this.lowerRange = lowerRange;
            this.upperRange = upperRange;
        }

        private void show() {
            hidden = false;
            series.getData().addAll(data);
            data.clear();
        }

        private void hide() {
            hidden = true;
            data.addAll(series.getData());
            series.getData().clear();
        }

        private void add(XYChart.Data<Number, Number> data) {
            if(hidden) {
                this.data.add(data);
            } else {
                series.getData().add(data);
            }
        }

        private void clearFrame() {
            if(hidden) {
                if(data.size() >= 1) {
                    this.data.subList(0, data.size() - 2).clear();
                }
            } else {
                if(series.getData().size() >= 1) {
                    series.getData().subList(0, series.getData().size() - 2).clear();
                }
            }
        }

        private void clear() {
            data.clear();
            series.getData().clear();
        }

        @Override
        public String toString() {
            return series.getName();
        }

        static int getLowerBound(List<Series> seriesList) {
            if(seriesList.size() == 0) {
                return DEFAULT_Y_AXIS_LOWER_BOUND;
            } else if(seriesList.size() == 1) {
                if(seriesList.get(0).hidden) {
                    return seriesList.get(0).lowerRange;
                } else {
                    return DEFAULT_Y_AXIS_LOWER_BOUND;
                }
            }

            int lowerRange = DEFAULT_Y_AXIS_LOWER_BOUND;

            if(!seriesList.get(0).hidden) {
                lowerRange = seriesList.get(0).lowerRange;
            }

            for(int i=1, currentRange; i<seriesList.size() ; i++) {
                if(!seriesList.get(i).hidden) {
                    currentRange = seriesList.get(i).lowerRange;
                    if(currentRange < lowerRange) {
                        lowerRange = currentRange;
                    }
                }
            }

            return lowerRange;
        }

        static int getUpperBound(List<Series> seriesList) {
            if(seriesList.size() == 0) {
                return DEFAULT_Y_AXIS_UPPER_BOUND;
            } else if(seriesList.size() == 1) {
                if(seriesList.get(0).hidden) {
                    return seriesList.get(0).upperRange;
                } else {
                    return DEFAULT_Y_AXIS_UPPER_BOUND;
                }
            }

            int upperRange = DEFAULT_Y_AXIS_UPPER_BOUND;

            if(!seriesList.get(0).hidden) {
                upperRange = seriesList.get(0).upperRange;
            }

            for(int i=1, currentUpperRange; i<seriesList.size() ; i++) {
                if(!seriesList.get(i).hidden) {
                    currentUpperRange = seriesList.get(i).upperRange;
                    if(currentUpperRange > upperRange) {
                        upperRange = currentUpperRange;
                    }
                }
            }

            return upperRange;
        }

    }

    private static final int X_AXIS_RANGE_MIN = 1000;
    private static final int X_AXIS_RANGE_MAX = 6000;

    public static final int DEFAULT_Y_AXIS_LOWER_BOUND = 0;
    public static final int DEFAULT_Y_AXIS_UPPER_BOUND = 45;

    private SerialCommunicator serialCommunicator;

    private int xAxisRange;

    private int currentSample;

    private int currentDataSize;

    private boolean stopped;

    private int stoppedXAxisLowerBound;

    private int stoppedXAxisUpperBound;

    private int stoppedYAxisLowerBound;

    private int stoppedYAxisUpperBound;

    private final List<Series> chartSeries = new ArrayList<>();

    @FXML
    private CheckComboBox<Series> visibleSeriesCheckBox;

    @FXML
    private TextField rangeField;

    @FXML
    private ToggleButton stopButton;

    @FXML
    private LineChart<Number, Number> chart;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private NumberAxis xAxis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xAxisRange = 3000;
        currentSample = 0;
        currentDataSize = 0;
        reset(null);

        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(xAxisRange);
        xAxis.setTickUnit(xAxisRange / 10.0);
        xAxis.setMinorTickVisible(false);
        xAxis.setLabel("Sample");

        yAxis.setForceZeroInRange(false);
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(DEFAULT_Y_AXIS_LOWER_BOUND);
        yAxis.setUpperBound(DEFAULT_Y_AXIS_UPPER_BOUND);
        yAxis.setMinorTickCount(2);

        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        chart.setOnScroll(event -> {
            event.consume();

            double delta = event.getDeltaY();
            if(!stopped || delta == 0) {
                return;
            }

            Region region = (Region) chart.lookup("Region");

            if(PIDRegulator.MOUSE_PRESSED_KEYS.contains(MouseButton.SECONDARY) ||
                    PIDRegulator.KEYBOARD_PRESSED_KEYS.contains(KeyCode.CONTROL)) {
                double cursorY = 1 - (event.getY() - (region.getLayoutY() + chart.getPadding().getTop())) / region.getHeight();
                double zoomY = delta * yAxis.getTickUnit() / 10;

                double yLowerBound = (yAxis.getLowerBound() + cursorY * zoomY / 2);
                double yUpperBound = (yAxis.getUpperBound() - (1 - cursorY) * zoomY / 2);

                if(yLowerBound < stoppedYAxisLowerBound) {
                    yLowerBound = stoppedYAxisLowerBound;
                }

                if(yUpperBound > stoppedYAxisUpperBound) {
                    yUpperBound = stoppedYAxisUpperBound;
                }

                if(yLowerBound > yUpperBound) {
                    return;
                }

                yAxis.setLowerBound(yLowerBound);
                yAxis.setUpperBound(yUpperBound);
                yAxis.setTickUnit(((yAxis.getUpperBound() - yAxis.getLowerBound()) / 20));
            } else {
                double cursorX = (event.getX() - (region.getLayoutX() + chart.getPadding().getLeft())) / region.getWidth();
                double zoomX = delta * xAxis.getTickUnit() / 10;

                int xLowerBound = (int) (xAxis.getLowerBound() + cursorX * zoomX / 2);
                int xUpperBound = (int) (xAxis.getUpperBound() - (1 - cursorX) * zoomX / 2);

                if(xLowerBound < stoppedXAxisLowerBound) {
                    xLowerBound = stoppedXAxisLowerBound;
                }

                if(xUpperBound > stoppedXAxisUpperBound) {
                    xUpperBound = stoppedXAxisUpperBound;
                }

                if(xLowerBound > xUpperBound || xLowerBound + 10 > xUpperBound) {
                    return;
                }

                xAxis.setLowerBound(xLowerBound);
                xAxis.setUpperBound(xUpperBound);
                xAxis.setTickUnit((int) ((xAxis.getUpperBound() - xAxis.getLowerBound()) / 10));
            }
        });
        chart.getStylesheets().add(getClass().getResource("/chart_series.css").toExternalForm());

        chartSeries.add(new Series("distance", 0, 45));
        chartSeries.add(new Series("filtered distance", 0, 45));
        chartSeries.add(new Series("error", -45, 45));
        chartSeries.add(new Series("α angle", -45, 45));
        visibleSeriesCheckBox.getItems().addAll(chartSeries);

        visibleSeriesCheckBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<Series>) c -> {
            chart.getData().clear();
            chartSeries.forEach(Series::hide);
            visibleSeriesCheckBox.getCheckModel().getCheckedItems().forEach(Series::show);
            chartSeries.forEach(s -> chart.getData().add(s.series));
            yAxis.setLowerBound(Series.getLowerBound(chartSeries));
            yAxis.setUpperBound(Series.getUpperBound(chartSeries));
        });
        visibleSeriesCheckBox.getCheckModel().checkIndices(0, 1);
    }

    protected synchronized void update() {
        if(!serialCommunicator.hasNextFrame()) {
            return;
        } else {
            if(stopped) {
                //pop frame from input stack
                serialCommunicator.getNextFrame();
                return;
            }
        }

        InputFrame frame = serialCommunicator.getNextFrame();

        if(currentDataSize - 1 == xAxisRange) {
            for(Series series : chartSeries) {
                series.clearFrame();
            }

            currentDataSize = 0;
            currentSample--;

            xAxis.setLowerBound(currentSample);
            xAxis.setUpperBound(currentSample + xAxisRange);
        } else {
            for(int i=0 ; i<chartSeries.size() ; i++) {
                chartSeries.get(i).add(new XYChart.Data<>(currentSample, frame.getData(i)));
            }

            currentSample++;
            currentDataSize++;
        }
    }

    @FXML
    protected synchronized void setRange(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER) {
            String rangeFieldText = rangeField.getText();

            if(rangeFieldText.matches("^-?\\d+$")) {
                int val = Integer.parseInt(rangeFieldText);

                if (val >= X_AXIS_RANGE_MIN && val <= X_AXIS_RANGE_MAX) {
                    xAxisRange = val;
                } else if (val < X_AXIS_RANGE_MIN) {
                    xAxisRange = X_AXIS_RANGE_MIN;
                } else {
                    xAxisRange = X_AXIS_RANGE_MAX;
                }

                reset(null);
            }

            rangeField.setText("" + xAxisRange);
        }
    }

    @FXML
    protected synchronized void reset(ActionEvent actionEvent) {
        if(stopped) {
            return;
        }

        chartSeries.forEach(Series::clear);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(xAxisRange);
        xAxis.setTickUnit((int) (xAxisRange / 10.0));
        yAxis.setLowerBound(Series.getLowerBound(chartSeries));
        yAxis.setUpperBound(Series.getUpperBound(chartSeries));
        yAxis.setTickUnit((yAxis.getUpperBound() - yAxis.getLowerBound()) / 20.0);
        rangeField.setText(xAxisRange + "");

        currentSample = 0;
        currentDataSize = 0;
    }

    @FXML
    protected synchronized void stop(ActionEvent actionEvent) {
        if(stopButton.isSelected()) {
            stopped = true;
            stoppedXAxisLowerBound = (int) xAxis.getLowerBound();
            stoppedXAxisUpperBound = (int) xAxis.getUpperBound();
            stoppedYAxisLowerBound = (int) yAxis.getLowerBound();
            stoppedYAxisUpperBound = (int) yAxis.getUpperBound();
        } else {
            stopped = false;
            reset(null);
        }
    }

    protected void setSerialCommunicator(SerialCommunicator serialCommunicator) {
        this.serialCommunicator = serialCommunicator;
    }

}
