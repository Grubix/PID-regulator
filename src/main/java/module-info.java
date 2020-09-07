module arduino {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fazecast.jSerialComm;
    requires org.controlsfx.controls;

    opens controllers to javafx.fxml;
    exports arduino;
}