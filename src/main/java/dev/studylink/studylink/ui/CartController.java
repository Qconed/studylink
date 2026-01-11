package dev.studylink.studylink.ui;

import dev.studylink.studylink.business.CartItem;
import dev.studylink.studylink.business.SessionFacade;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.CartDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLCartDAO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class CartController {
    
    @FXML
    private VBox cartItemsContainer;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Label itemCountLabel;
    
    @FXML
    private Button checkoutButton;
    
    @FXML
    private VBox emptyCartMessage;
    
    private CartDAO cartDAO;
    private User currentUser;
    
    @FXML
    public void initialize() {
        cartDAO = MySQLCartDAO.getInstance();
        currentUser = SessionFacade.getInstance().getCurrentUser();
        
        loadCart();
    }
    
    private void loadCart() {
        cartItemsContainer.getChildren().clear();
        
        List<CartItem> items = cartDAO.getCartItems(currentUser.getId());
        
        if (items.isEmpty()) {
            emptyCartMessage.setVisible(true);
            emptyCartMessage.setManaged(true);
            checkoutButton.setDisable(true);
            totalLabel.setText("0.00 DH");
            itemCountLabel.setText("0 article(s)");
        } else {
            emptyCartMessage.setVisible(false);
            emptyCartMessage.setManaged(false);
            checkoutButton.setDisable(false);
            
            for (CartItem item : items) {
                VBox itemBox = createCartItemBox(item);
                cartItemsContainer.getChildren().add(itemBox);
            }
            
            updateTotals();
        }
    }
    
    private VBox createCartItemBox(CartItem item) {
        VBox box = new VBox(10);
        box.getStyleClass().add("content-card");
        box.setStyle(box.getStyle() + "; -fx-padding: 20;");
        
        // Header avec titre et bouton supprimer
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(item.getResourceTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 18;");
        deleteBtn.setOnAction(e -> {
            cartDAO.removeFromCart(item.getId());
            loadCart();
        });
        
        header.getChildren().addAll(titleLabel, spacer, deleteBtn);
        
        // Description
        Label descLabel = new Label(item.getResourceDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13;");
        
        // Footer avec prix, quantité et sous-total
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Label priceLabel = new Label(String.format("$%.2f", item.getResourcePrice()));
        priceLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #64748B;");
        
        // Spinner pour la quantité
        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(Pos.CENTER_LEFT);
        Label qtyLabel = new Label("Quantité:");
        qtyLabel.setStyle("-fx-text-fill: #64748B;");
        
        Spinner<Integer> quantitySpinner = new Spinner<>();
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, item.getQuantity());
        quantitySpinner.setValueFactory(valueFactory);
        quantitySpinner.setPrefWidth(80);
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            cartDAO.updateQuantity(item.getId(), newVal);
            loadCart();
        });
        
        quantityBox.getChildren().addAll(qtyLabel, quantitySpinner);
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);
        
        Label subtotalLabel = new Label(String.format("Sous-total: $%.2f", item.getSubtotal()));
        subtotalLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #356EE9;");
        
        footer.getChildren().addAll(priceLabel, quantityBox, spacer2, subtotalLabel);
        
        box.getChildren().addAll(header, descLabel, footer);
        VBox.setMargin(box, new Insets(0, 0, 15, 0));
        
        return box;
    }
    
    private void updateTotals() {
        int count = cartDAO.getCartItemCount(currentUser.getId());
        double total = cartDAO.getCartTotal(currentUser.getId());
        
        itemCountLabel.setText(count + " article(s)");
        totalLabel.setText(String.format("$%.2f", total));
    }
    
    @FXML
    private void onCheckout() {
        // TODO: Implémenter le processus de paiement
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Paiement");
        alert.setHeaderText("Fonctionnalité à venir");
        alert.setContentText("Le système de paiement sera implémenté prochainement.");
        alert.showAndWait();
    }
    
    @FXML
    private void onClearCart() {
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Vider le panier");
        confirm.setHeaderText("Êtes-vous sûr ?");
        confirm.setContentText("Voulez-vous vraiment vider votre panier ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                cartDAO.clearCart(currentUser.getId());
                loadCart();
            }
        });
    }
}
