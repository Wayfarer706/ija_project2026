package xyuguyn00.view.scene;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import xyuguyn00.view.render.ViewConstants;

public class TopNavigationBar extends HBox {
    public TopNavigationBar(Runnable onReturnToMenu) {
        Button returnBtn = new Button("⬅ Main Menu");
        returnBtn.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_SMALL));
        returnBtn.setStyle("-fx-background-color: #8b0000; -fx-text-fill: white;");
        
        returnBtn.setOnAction(e -> onReturnToMenu.run());

        this.getChildren().add(returnBtn);
        this.setPadding(new Insets(ViewConstants.PADDING_SMALL, ViewConstants.PADDING_MEDIUM, ViewConstants.PADDING_SMALL, ViewConstants.PADDING_MEDIUM));
        this.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #3c3c3c; -fx-border-width: 0 0 2 0;");
    }
}