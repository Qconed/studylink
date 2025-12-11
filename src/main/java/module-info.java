module dev.studylink.studylink {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.graphics;


    requires java.sql;
    requires com.zaxxer.hikari;
    
    opens dev.studylink.studylink to javafx.fxml;
    opens dev.studylink.studylink.ui to javafx.fxml;

    exports dev.studylink.studylink;
    exports dev.studylink.studylink.business;
    exports dev.studylink.studylink.dao;
}
