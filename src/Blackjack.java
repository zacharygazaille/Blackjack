import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Blackjack {
    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            if ("AJQK".contains(value)) { //A J Q K
                if (value == "A") {
                    return 11;
                }
                return 10;
            }
            return Integer.parseInt(value); //2-10
        }

        public boolean isAce() {
            return value == "A";
        }

        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }
    
    ArrayList<Card> deck;
    Random random = new Random(); // shuffle deck

    //dealer
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    //player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    //window
    int boardWidth = 900; // change board size
    int boardHeight = 600;

    int cardWidth = 110; //ratio should be 1/1.4
    int cardHeight = 154;

    JFrame frame = new JFrame("Blackjack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                //draw hidden card
                Image hiddenCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!standButton.isEnabled()) {
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage(hiddenCardImg, 20, 20, cardWidth, cardHeight, null);

                //draw dealer's hand
                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5)*i, 20, cardWidth, cardHeight, null);
                }

                //draw player's hand
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, 20 + (cardWidth + 5)*i, 320, cardWidth, cardHeight, null);
                }

                if (!standButton.isEnabled()) {
                    dealerSum = reduceDealerAce();
                    playerSum = reducePlayerAce();
                    System.out.println("STAND:");
                    System.out.println(dealerSum);
                    System.out.println(playerSum);

                    String message = "";
                    if (playerSum > 21) {
                        message = "You go bust!";
                    }
                    else if (dealerSum > 21) {
                        message = "Dealer goes bust!";
                    }
                    //both you and dealer <= 21
                    else if (playerSum == dealerSum) {
                        message = "Push!";
                    }
                    else if (playerSum == 21 && playerHand.size() == 2) {
                        message = ("Blackjack!");
                    }
                    else if (dealerSum == 21 && dealerHand.size() == 1) {
                        message = ("Dealer has Blackjack!");
                    }
                    else if (playerSum > dealerSum) {
                        message = ("You win!");
                    }
                    else if (playerSum < dealerSum) {
                        message = "Dealer wins!";
                    }

                    g.setFont(new Font("Arial", Font.BOLD, 30)); //change win / lose font
                    g.setColor(Color.white);
                    g.drawString(message, 50, 260);

                    JButton nextRoundButton = new JButton("Next Round");
                    nextRoundButton.setBounds(boardWidth - 160, boardHeight - 100, 120, 40); // move buttons
                    gamePanel.add(nextRoundButton);
                    nextRoundButton.setFocusable(false);

                    nextRoundButton.addActionListener(new ActionListener() { // find a way to make this restart the game
                        public void actionPerformed(ActionEvent e) {
                            hitButton.setEnabled(true);
                            standButton.setEnabled(true);
                            gamePanel.remove(nextRoundButton);
                            gamePanel.repaint();

                            playerHand.clear();
                            dealerHand.clear();

                            //dealer
                            dealerSum = 0;
                            dealerAceCount = 0;

                            hiddenCard = deck.remove(deck.size()-1); //remove card at last index
                            dealerSum += hiddenCard.getValue();
                            dealerAceCount += hiddenCard.isAce() ? 1 : 0;

                            Card card = deck.remove(deck.size()-1);
                            dealerSum += card.getValue();
                            dealerAceCount += card.isAce() ? 1 : 0;
                            dealerHand.add(card);

                            //player
                            playerSum = 0;
                            playerAceCount = 0;
                            for (int i = 0; i < 2; i++) {
                                card = deck.remove(deck.size()-1);
                                playerSum += card.getValue();
                                playerAceCount += card.isAce() ? 1 : 0;
                                playerHand.add(card);
                            }

                            if (playerSum == 21 || dealerSum == 21) {
                                hitButton.setEnabled(false);
                                standButton.setEnabled(false);
                            }
                        }
                    });

                 }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JButton hitButton = new JButton("Hit");
    JButton standButton = new JButton("Stand");

    Blackjack() {
        startGame(); //start game

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(null);
        gamePanel.setBackground(new Color(0, 102, 51)); //change background r g b
        frame.add(gamePanel);

        hitButton.setBounds(20, boardHeight - 100, 80, 40); // move buttons
        gamePanel.add(hitButton);
        hitButton.setFocusable(false);
        standButton.setBounds(115, boardHeight - 100, 80, 40);
        gamePanel.add(standButton);
        standButton.setFocusable(false);

        if (dealerSum != 21 && playerSum != 21) {
            hitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Card card = deck.remove(deck.size()-1);
                    playerSum += card.getValue();
                    playerAceCount += card.isAce() ? 1 : 0;
                    playerHand.add(card);
                    if (deck.size() == 0) {
                        buildDeck();
                        shuffleDeck();
                    }
                    if (reducePlayerAce() > 21) { //A + 2 + J --> 1 + 2 + J
                        hitButton.setEnabled(false);
                        standButton.setEnabled(false);
                    }

                    gamePanel.repaint();
                }
            });

            standButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    
                    while (dealerSum < 17) {
                        Card card = deck.remove(deck.size()-1);
                        dealerSum += card.getValue();
                        dealerAceCount += card.isAce() ? 1 : 0;
                        dealerHand.add(card);
                    }
                    hitButton.setEnabled(false);
                    standButton.setEnabled(false);
                    gamePanel.repaint();
                }
            });
        }
        else {
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
        }

        gamePanel.repaint();
    }

    public void startGame() {
        //deck
        deck = new ArrayList<Card>();
        buildDeck();
        shuffleDeck();

        //dealer
        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.remove(deck.size()-1); //remove card at last index
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;
        if (deck.size() == 0) {
            buildDeck();
            shuffleDeck();
        }

        Card card = deck.remove(deck.size()-1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);
        if (deck.size() == 0) {
            buildDeck();
            shuffleDeck();
        }

        //player
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;
        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size()-1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
            if (deck.size() == 0) {
                buildDeck();
                shuffleDeck();
            }
        }

    }

    public void buildDeck() {
        int deckNumber = 6; // change the number of decks
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (int i = 0; i < deckNumber; i++) {
            for (int j = 0; j < types.length; j++) {
                for (int k = 0; k < values.length; k++) {
                    Card card = new Card(values[k], types[j]); //Edit here if you want to make the deck two decks long for a total of 104 cards
                    deck.add(card);
                }
            }
        }

        System.out.println("BUILD DECK:");
        System.out.println(deck);
    }

    public void shuffleDeck() {
        for (int i = 0; i< deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }

        System.out.println("AFTER SHUFFLE:");
        System.out.println(deck);
    }

    public int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10;
            playerAceCount -= 1;
        }
        return playerSum;
    }

    public int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }

}

/* Things to improve:
 * add double, split buttons
 * Make it so theres a small wait time / animation between dealer picking card
 * Add bets
 * Add a message when the deck is shuffled
 * Test the change
 */
