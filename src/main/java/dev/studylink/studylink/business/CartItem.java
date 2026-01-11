package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class CartItem {
    private int id;
    private int userId;
    private int resourceId;
    private String resourceTitle;
    private String resourceDescription;
    private double resourcePrice;
    private int quantity;
    private LocalDateTime addedAt;

    // Constructor pour créer un nouvel item
    public CartItem(int userId, int resourceId, int quantity) {
        this.userId = userId;
        this.resourceId = resourceId;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }

    // Constructor pour les items depuis la DB avec toutes les infos de la ressource
    public CartItem(int id, int userId, int resourceId, String resourceTitle, 
                    String resourceDescription, double resourcePrice, int quantity, 
                    LocalDateTime addedAt) {
        this.id = id;
        this.userId = userId;
        this.resourceId = resourceId;
        this.resourceTitle = resourceTitle;
        this.resourceDescription = resourceDescription;
        this.resourcePrice = resourcePrice;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getResourceId() { return resourceId; }
    public String getResourceTitle() { return resourceTitle; }
    public String getResourceDescription() { return resourceDescription; }
    public double getResourcePrice() { return resourcePrice; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getAddedAt() { return addedAt; }
    
    public double getSubtotal() {
        return resourcePrice * quantity;
    }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
