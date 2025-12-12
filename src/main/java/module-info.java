module dev.studylink.studylink {
    requires javafx.controls;
    requires javafx.fxml;


    opens dev.studylink.studylink to javafx.fxml;
    exports dev.studylink.studylink;
}