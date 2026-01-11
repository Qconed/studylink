package dev.studylink.studylink.business;

/**
 * Product représente l'interface pour les objets commercialisables / listables (titre, description, prix).
 */
public interface Product {
    String getTitle();
    void setTitle(String title);

    String getDescription();
    void setDescription(String description);

    double getPrice();
    void setPrice(double price);
}

