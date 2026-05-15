/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: A modal screen overlay that triggers when a win condition is met. 
 * Prevents further game interaction while providing the user with end-of-session options.
 */
package xyuguyn00.view.scene;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import xyuguyn00.view.render.ViewConstants;

public class GameOverOverlay extends VBox {
    public GameOverOverlay(String winner, Runnable onRetry, Runnable onMainMenu) {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(ViewConstants.PADDING_MEDIUM); 
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        Text title = new Text("Game Over");
        title.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_HUGE));
        title.setFill(Color.WHITE);

        Text subtitle = new Text(winner + " Wins!");
        subtitle.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_LARGE));
        subtitle.setFill(Color.GOLD);

        Button retryBtn = new Button("Retry");
        retryBtn.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_MEDIUM));
        retryBtn.setPrefWidth(ViewConstants.BUTTON_WIDTH_STANDARD);
        retryBtn.setOnAction(e -> onRetry.run());

        Button menuBtn = new Button("Main Menu");
        menuBtn.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_MEDIUM));
        menuBtn.setPrefWidth(ViewConstants.BUTTON_WIDTH_STANDARD);
        menuBtn.setOnAction(e -> onMainMenu.run());

        this.getChildren().addAll(title, subtitle, retryBtn, menuBtn);
    }
}