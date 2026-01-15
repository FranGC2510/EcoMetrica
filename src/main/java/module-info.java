module org.dam.fcojavier.ecometrica {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires java.sql; // Necesario para JDBC
    requires org.hibernate.orm.core; // Hibernate
    requires jakarta.persistence; // JPA
    requires java.naming; // Necesario para Hibernate en algunos entornos
    requires com.github.librepdf.openpdf; // OpenPDF
    requires jbcrypt;

    opens org.dam.fcojavier.ecometrica to javafx.fxml;
    opens org.dam.fcojavier.ecometrica.controllers to javafx.fxml;
    opens org.dam.fcojavier.ecometrica.entities to org.hibernate.orm.core;
    exports org.dam.fcojavier.ecometrica;
}