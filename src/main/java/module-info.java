module org.karthunter {
    requires java.desktop;
    requires java.sql;
    requires javasdk;
    requires okhttp;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
    requires org.junit.platform.launcher;
    exports org.karthunter;
    opens org.karthunter to org.junit.platform.commons, org.junit.jupiter.api, org.junit.jupiter.engine;
}