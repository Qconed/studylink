package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class CartItem {
    private int id;
    private int userId;
    private Integer resourceId;  // Nullable
    private Integer sessionId;   // Nullable
    private String itemType;     // "RESOURCE" or "SESSION"
    private String itemTitle;
    private String itemDescription;
    private double itemPrice;
    private int quantity;
    private LocalDateTime addedAt;

    // Constructor pour créer un nouvel item resource
    public CartItem(int userId, int resourceId, int quantity) {
        this.userId = userId;
        this.resourceId = resourceId;
        this.sessionId = null;
        this.itemType = "RESOURCE";
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }
    
    // Constructor pour créer un nouvel item session
    public CartItem(int userId, int sessionId, int quantity, boolean isSession) {
        this.userId = userId;
        this.resourceId = null;
        this.sessionId = sessionId;
        this.itemType = "SESSION";
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
    }

    // Constructor pour les items depuis la DB avec toutes les infos
    public CartItem(int id, int userId, Integer resourceId, Integer sessionId, String itemType,
                    String itemTitle, String itemDescription, double itemPrice, int quantity, 
                    LocalDateTime addedAt) {
        this.id = id;
        this.userId = userId;
        this.resourceId = resourceId;
        this.sessionId = sessionId;
        this.itemType = itemType;
        this.itemTitle = itemTitle;
        this.itemDescription = itemDescription;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public Integer getResourceId() { return resourceId; }
    public Integer getSessionId() { return sessionId; }
    public String getItemType() { return itemType; }
    public String getItemTitle() { return itemTitle; }
    public String getItemDescription() { return itemDescription; }
    public double getItemPrice() { return itemPrice; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getAddedAt() { return addedAt; }
    
    // Legacy getters for backward compatibility
    public String getResourceTitle() { return itemTitle; }
    public String getResourceDescription() { return itemDescription; }
    public double getResourcePrice() { return itemPrice; }
    
    public boolean isResource() { return "RESOURCE".equals(itemType); }
    public boolean isSession() { return "SESSION".equals(itemType); }
    
    public double getSubtotal() {
        return itemPrice * quantity;
    }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
