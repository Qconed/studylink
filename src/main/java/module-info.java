module dev.studylink.studylink {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;
    
    opens dev.studylink.studylink to javafx.fxml;
    exports dev.studylink.studylink;
    exports dev.studylink.studylink.business;
    exports dev.studylink.studylink.dao;
}
