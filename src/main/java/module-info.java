module dev.studylink.studylink {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires jbcrypt;
    requires java.dotenv;

    opens dev.studylink.studylink to javafx.fxml;
    opens dev.studylink.studylink.ui to javafx.fxml;
    opens dev.studylink.studylink.business to javafx.base;

    exports dev.studylink.studylink;
    exports dev.studylink.studylink.business;
    exports dev.studylink.studylink.dao;
    exports dev.studylink.studylink.ui;
    exports dev.studylink.studylink.exception;
}
