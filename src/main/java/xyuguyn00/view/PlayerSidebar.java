package xyuguyn00.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import xyuguyn00.common.GameEvent;
import xyuguyn00.game.Game;
import xyuguyn00.tool.GameObserver;

/**
 * The sidebar UI displaying player stats and turn controls.
 */
public class PlayerSidebar extends VBox implements GameObserver {
    private final Game game;
    private final VBox player1Card;
    private final VBox player2Card;
    
    // Hold references to the text fields so we can update them
    private final Text p1GoldText;
    private final Text p2GoldText;

    public PlayerSidebar(Game game) {
        this.game = game;
        this.game.addObserver(this);

        this.setPadding(new Insets(20));
        this.setSpacing(30);
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: #2b2b2b;"); 
        this.setPrefWidth(200);

        // Initialize UI Elements
        p1GoldText = new Text("Gold: 0");
        p1GoldText.setFill(Color.GOLD);
        p1GoldText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        p2GoldText = new Text("Gold: 0");
        p2GoldText.setFill(Color.GOLD);
        p2GoldText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        player1Card = createPlayerCard("Player 1", Color.DARKBLUE, p1GoldText);
        player2Card = createPlayerCard("Player 2", Color.DARKRED, p2GoldText);

        Button endTurnBtn = new Button("End Turn");
        endTurnBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        endTurnBtn.setMaxWidth(Double.MAX_VALUE);
        endTurnBtn.setOnAction(e -> game.endTurn());

        this.getChildren().addAll(player1Card, player2Card, endTurnBtn);
        
        // Set initial visual state
        updateSidebarState();
    }

    private VBox createPlayerCard(String playerName, Color themeColor, Text goldText) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        
        Text nameText = new Text(playerName);
        nameText.setFill(themeColor);
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        card.getChildren().addAll(nameText, goldText);
        return card;
    }

    private void updateSidebarState() {
        String activeBorder = "-fx-border-color: gold; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-color: #3c3c3c; -fx-background-radius: 5;";
        String inactiveBorder = "-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #3c3c3c; -fx-background-radius: 5;";

        // Update active player highlight
        if (game.getCurrentPlayer().equals("Player 1")) {
            player1Card.setStyle(activeBorder);
            player2Card.setStyle(inactiveBorder);
        } else {
            player1Card.setStyle(inactiveBorder);
            player2Card.setStyle(activeBorder);
        }
        
        // Update Gold Counters dynamically from the engine
        p1GoldText.setText("Gold: " + game.getPlayerFunds("Player 1"));
        p2GoldText.setText("Gold: " + game.getPlayerFunds("Player 2"));
    }

    @Override
    public void update(GameEvent event) {
        updateSidebarState();
    }
}