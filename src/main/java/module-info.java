module dev.studylink.studylink {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens dev.studylink.studylink.ui to javafx.fxml;
    exports dev.studylink.studylink;
}