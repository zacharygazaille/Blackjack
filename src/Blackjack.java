import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
            return "./"+cardTheme+"/" + toString() + ".png";
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
    Double playerBalance = 5000.00;
    Double betAmount;
    String betMessage = "";
    boolean addBet = true;
    boolean displayMessage = true;
    boolean displayBet = false;
    int totalSplits = 0;
    int splitHandNum = 0;
    boolean isSplit = false;
    ArrayList<Integer> splitSums = new ArrayList<Integer>();

    //window
    int boardWidth = 900; // change board size
    int boardHeight = 600;

    boolean firstTimeThrough = true;
    int themeChanges = 0;
    String cardTheme = "darkCards";
    Color myGold = new Color(179, 147, 60); // Color gold
    boolean isNextRoundButtonTrue = false;

    int cardWidth = 110; //ratio should be 1/1.4
    int cardHeight = 154;

    JFrame frame = new JFrame("Blackjack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {

                String themeMessage = ""; //change theme font
                if (themeChanges % 2 == 0) {
                    themeMessage = "Dark";
                    g.setColor(myGold);
                    g.setFont(new Font("Arial", Font.BOLD, 15));
                    g.drawString(themeMessage, boardWidth-85, 40);
                    g.setFont(new Font("Arial", Font.BOLD, 25));
                    gamePanel.setBackground(new Color(100, 0, 0));
                    cardTheme = "darkCards";
                } else {
                    themeMessage = "Light";
                    g.setColor(Color.white);
                    g.setFont(new Font("Arial", Font.BOLD, 15));
                    g.drawString(themeMessage, boardWidth-85, 40);
                    g.setFont(new Font("Arial", Font.BOLD, 25));
                    gamePanel.setBackground(new Color(0, 100, 0));
                    cardTheme = "lightCards";
                }

                NumberFormat formatter = new DecimalFormat("#0.00");
                g.drawString("Bet:", 20, 300);
                playerBalance = Math.round(playerBalance * 100) / (double)100;
                g.drawString("Balance: "+String.valueOf(formatter.format(playerBalance)), 20, 255);
                if (displayBet) {
                    g.drawString(String.valueOf(formatter.format(betAmount)), 75, 300);
                }

                if (isSplit == true) {
                    g.drawString("Hand "+String.valueOf(splitHandNum+1), 200, 300);
                }

                g.setFont(new Font("Arial", Font.BOLD, 30)); //change bet message
                g.drawString(betMessage, 250, 260);

                if (startButton.isEnabled()) { // start menu
                    g.setFont(new Font("Arial", Font.BOLD, 140));
                    g.drawString("Blackjack", 110, 185);
                    String[] menuCards = {"/BACK.png", "/A-H.png", "/A-S.png", "/A-D.png", "/A-C.png", "/BACK.png"};
                    for (int i = 0; i < 6; i++) {
                        Image cardImg = new ImageIcon(getClass().getResource("./"+cardTheme+menuCards[i])).getImage();
                        g.drawImage(cardImg, 95 + (cardWidth + 5)*i, 320, cardWidth, cardHeight, null);
                    }

                } else {
                    //draw hidden card
                    Image hiddenCardImg = new ImageIcon(getClass().getResource("./"+cardTheme+"/BACK.png")).getImage();
                    if (!standButton.isEnabled() && splitHandNum == 0) {
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
                    for (int i = splitHandNum; i < playerHand.size(); i++) {
                        Card card = playerHand.get(i);
                        Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                        g.drawImage(cardImg, 20 + (cardWidth + 5)*(i-splitHandNum), 320, cardWidth, cardHeight, null);
                    }
                }      

                if (!themeButton.isEnabled()) { // change themes
                    if (themeChanges % 2 == 0) {
                        themeButton.setEnabled(true);
                        themeChanges++;
                    }
                    else {
                        themeButton.setEnabled(true);
                        themeChanges++;
                    }       
                    gamePanel.repaint();           
                }

                if (!standButton.isEnabled() && splitHandNum == 0) {
                    if (isSplit) {
                        while (reduceDealerAce() < 17) { /////// fixn reduced dealerace, as it is looking at the whole deck
                            emptyDeckShuffle();
                            Card card = deck.remove(deck.size()-1);
                            dealerSum += card.getValue();
                            dealerAceCount += card.isAce() ? 1 : 0;
                            dealerHand.add(card);
                        }
                        isSplit = false;
                        splitSums.add(playerSum);

                        String message = "";
                        for (int i = 0; i < splitSums.size(); i++) {
                            message = message + "Hand " + String.valueOf(totalSplits+1) + ": ";
                            if (splitSums.get(i) > 21) {
                                message += "Loss";
                            } else if (splitSums.get(i) > dealerSum) {
                                message += "Win";
                            } else if (dealerSum > 21) {
                                message += "Win";
                            } else if (splitSums.get(i) < dealerSum) {
                                message += "Loss";
                            } else {
                                message += "Push";
                            }
                            if (totalSplits != 0) {
                                message += ", ";
                            }
                            totalSplits -= 1;
                        }
                        if (displayMessage == true){
                            g.setFont(new Font("Arial", Font.BOLD, 25));
                            g.drawString(message, 250, 260);
                        }
                    } else {
                        dealerSum = reduceDealerAce();
                        playerSum = reducePlayerAce();

                        String message = "";
                        if (playerSum > 21) {
                            message = "You go bust!";
                        }
                        else if (dealerSum > 21) {
                            message = "Dealer goes bust!";
                            if (addBet == true) {
                                playerBalance += 2 * betAmount;
                            }
                        }
                        //both you and dealer <= 21
                        else if (playerSum == dealerSum) {
                            message = "Push!";
                            if (addBet == true) {
                                playerBalance += betAmount;
                            }
                        }
                        else if (playerSum == 21 && playerHand.subList(splitHandNum, playerHand.size()).size() == 2) {
                            message = ("Blackjack!");
                            if (addBet == true) {
                                playerBalance += 2.5 * betAmount;
                            }
                        }
                        else if (dealerSum == 21 && dealerHand.size() == 1) {
                            message = ("Dealer has Blackjack!");
                        }
                        else if (playerSum > dealerSum) {
                            message = ("You win!");
                            if (addBet == true) {
                                playerBalance += 2 * betAmount;
                            }
                        }
                        else if (playerSum < dealerSum) {
                            message = "Dealer wins!";
                        }
                        addBet = false;
                        gamePanel.repaint();

                        if (displayMessage == true){
                            g.setFont(new Font("Arial", Font.BOLD, 30));
                            g.drawString(message, 250, 260);
                        }
                        
                    }

                    if (isNextRoundButtonTrue == false) {
                        gamePanel.add(betField);
                        displayBet = false;
                        JButton nextRoundButton = new JButton("Next Round");
                        nextRoundButton.setBounds(boardWidth - 160, boardHeight - 100, 120, 40); // move buttons
                        gamePanel.add(nextRoundButton);
                        nextRoundButton.setFocusable(false);
                        isNextRoundButtonTrue = true;

                        nextRoundButton.addActionListener(new ActionListener() { 
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    splitSums.clear();
                                    totalSplits = 0;
                                    betAmount = Double.parseDouble(betField.getText());
                                    betAmount = Math.round(betAmount * 100) / (double)100;
                                    if (betAmount < 0.01) {
                                        betMessage = "Bet amount is too small";
                                        displayMessage = false;
                                    } else if (betAmount > playerBalance) {
                                        betMessage = "Bet amount exceeds player balance";
                                        displayMessage = false;
                                    } else {
                                        doubleButton.setVisible(true);
                                        betMessage = "";
                                        playerBalance -= betAmount;
                                        hitButton.setEnabled(true);
                                        standButton.setEnabled(true);
                                        gamePanel.remove(nextRoundButton);
                                        isNextRoundButtonTrue = false;
                                        gamePanel.remove(betField);
                                        displayBet = true;

                                        playerHand.clear();
                                        dealerHand.clear();

                                        //dealer
                                        dealerSum = 0;
                                        dealerAceCount = 0;

                                        emptyDeckShuffle();
                                        hiddenCard = deck.remove(deck.size()-1); //remove card at last index
                                        dealerSum += hiddenCard.getValue();
                                        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

                                        emptyDeckShuffle();
                                        Card card = deck.remove(deck.size()-1);
                                        dealerSum += card.getValue();
                                        dealerAceCount += card.isAce() ? 1 : 0;
                                        dealerHand.add(card);

                                        //player
                                        playerSum = 0;
                                        playerAceCount = 0;
                                        for (int i = 0; i < 2; i++) {
                                            emptyDeckShuffle();
                                            card = deck.remove(deck.size()-1);
                                            playerSum += card.getValue();
                                            playerAceCount += card.isAce() ? 1 : 0;
                                            playerHand.add(card);
                                        }
                                        if (playerHand.get(0).getValue() == playerHand.get(1).getValue()){
                                            splitButton.setVisible(true);
                                        }
                                        addBet = true;
                                        displayMessage = true;
                                        if (playerSum == 21 || dealerSum == 21) {
                                            doubleButton.setVisible(false);
                                            splitButton.setVisible(false);
                                            hitButton.setEnabled(false);
                                            standButton.setEnabled(false);
                                        }
                                    }
                                    gamePanel.repaint();
                                } catch (Exception f) {
                                    betMessage = "Please enter a valid bet";
                                    displayMessage = false;
                                    gamePanel.repaint();
                                }
                            }
                        });

                    }                     
                 } else if (!standButton.isEnabled()) {
                    String splitMessage = "";
                    if (playerSum > 21) {
                        splitMessage = "You go bust!";
                    }

                    if (displayMessage == true){
                        g.setFont(new Font("Arial", Font.BOLD, 30));
                        g.drawString(splitMessage, 250, 260);
                    }

                    splitSums.add(playerSum);
                    JButton nextHandButton = new JButton("Next Hand");
                    nextHandButton.setBounds(boardWidth - 160, boardHeight - 100, 120, 40); // move buttons
                    gamePanel.add(nextHandButton);
                    nextHandButton.setFocusable(false);

                    nextHandButton.addActionListener(new ActionListener() { 
                        public void actionPerformed(ActionEvent e) {
                            playerAceCount = 0;
                            gamePanel.remove(nextHandButton);
                            splitHandNum -= 1;
                            playerSum = 0;
                            for (int i = playerHand.size()-1; i > splitHandNum; i--){
                                playerHand.remove(i);
                            }
                            Card card = deck.remove(deck.size()-1);
                            playerHand.add(card);
                            for (int i = splitHandNum; i < splitHandNum + 2; i++) {
                                Card splitCard = playerHand.get(i);
                                playerAceCount += splitCard.isAce() ? 1 : 0;
                            }
                            playerSum += playerHand.get(playerHand.size()-1).getValue();
                            playerSum += playerHand.get(playerHand.size()-2).getValue();
                            hitButton.setEnabled(true);
                            standButton.setEnabled(true);
                            splitButton.setVisible(false);
                            doubleButton.setVisible(true);
                            gamePanel.repaint();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JButton startButton = new JButton("Start Game");
    JButton hitButton = new JButton("Hit");
    JButton standButton = new JButton("Stand");
    JButton themeButton = new JButton("Theme");
    JTextField betField = new JTextField(20);
    JButton doubleButton = new JButton("Double");
    JButton splitButton = new JButton("Split");
    JButton resetBalance = new JButton("Reset");

    Blackjack() {
        startGame(); //start game

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(null);
        gamePanel.setBackground(new Color(100, 0, 0)); //change background r g b
        frame.add(gamePanel);

        hitButton.setBounds(20, boardHeight - 100, 80, 40); // move buttons
        gamePanel.add(hitButton);
        hitButton.setFocusable(false);
        hitButton.setVisible(false);
        standButton.setBounds(115, boardHeight - 100, 80, 40);
        gamePanel.add(standButton);
        standButton.setFocusable(false);
        standButton.setVisible(false);
        doubleButton.setBounds(210, boardHeight - 100, 80, 40); // move buttons
        gamePanel.add(doubleButton);
        doubleButton.setFocusable(false);
        doubleButton.setVisible(false);
        splitButton.setBounds(305, boardHeight - 100, 80, 40); // move buttons
        gamePanel.add(splitButton);
        splitButton.setFocusable(false);
        splitButton.setVisible(false);
        resetBalance.setBounds(20, 190, 67, 25); // move buttons
        gamePanel.add(resetBalance);
        resetBalance.setFocusable(false);
        resetBalance.setVisible(false);
        themeButton.setBounds(boardWidth - 180, 20, 75, 30);
        gamePanel.add(themeButton);
        themeButton.setFocusable(false);
        startButton.setBounds(boardWidth / 2 - 73, boardHeight - 100, 120, 40);
        gamePanel.add(startButton);
        startButton.setFocusable(false);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    betAmount = Double.parseDouble(betField.getText());
                    betAmount = Math.round(betAmount * 100) / (double)100;

                    if (betAmount < 0.01) {
                        betMessage = "Bet amount is too small";
                    } else if (betAmount > playerBalance) {
                        betMessage = "Bet amount exceeds player balance";
                    } else{
                        overwritePlayerHand(); // remove<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        resetBalance.setVisible(true);
                        betMessage = "";
                        playerBalance -= betAmount;
                        hitButton.setVisible(true);
                        standButton.setVisible(true);
                        doubleButton.setVisible(true);
                        startButton.setEnabled(false);
                        gamePanel.remove(startButton);
                        gamePanel.remove(betField);
                        displayBet = true;
                        if (playerHand.get(0).getValue() == playerHand.get(1).getValue()){
                            splitButton.setVisible(true);
                        }
                    }
                    gamePanel.repaint();
                } catch (Exception f) {
                    betMessage = "Please enter a valid bet";
                    gamePanel.repaint();
                }
                
            }
        });
        
        betField.setFont(new Font("Arial", Font.BOLD, 24));
        betField.setBounds(80, 275, 80, 30);
        gamePanel.add(betField);

        themeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                firstTimeThrough = false;
                themeButton.setEnabled(false);
                gamePanel.repaint();
            }
        });

        resetBalance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playerBalance = 5000.0;
                gamePanel.repaint();
            }
        });

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                emptyDeckShuffle();
                splitButton.setVisible(false);
                doubleButton.setVisible(false);
                Card card = deck.remove(deck.size()-1);
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                if (reducePlayerAce() > 21) { //A + 2 + J --> 1 + 2 + J
                    hitButton.setEnabled(false);
                    standButton.setEnabled(false);
                }

                gamePanel.repaint();
            }
        });

        standButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                splitButton.setVisible(false);
                doubleButton.setVisible(false);
                if (splitHandNum == 0) {
                    while (reduceDealerAce() < 17) {
                        emptyDeckShuffle();
                        Card card = deck.remove(deck.size()-1);
                        dealerSum += card.getValue();
                        dealerAceCount += card.isAce() ? 1 : 0;
                        dealerHand.add(card);
                    }
                }
                hitButton.setEnabled(false);
                standButton.setEnabled(false);
                gamePanel.repaint();
            }
        });

        doubleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doubleButton.setVisible(false);
                splitButton.setVisible(false);
                playerBalance -= betAmount;
                betAmount *= 2;
                Card card = deck.remove(deck.size()-1);
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                if (splitHandNum == 0){
                    while (reduceDealerAce() < 17) {
                        emptyDeckShuffle();
                        Card dealerCard = deck.remove(deck.size()-1);
                        dealerSum += dealerCard.getValue();
                        dealerAceCount += dealerCard.isAce() ? 1 : 0;
                        dealerHand.add(dealerCard);
                    }
                }
                hitButton.setEnabled(false);
                standButton.setEnabled(false);
                gamePanel.repaint();
            }
        });

        splitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isSplit = true;
                totalSplits +=1;
                splitHandNum+=1;
                playerAceCount = 0;
                Card card = deck.remove(deck.size()-1);
                playerHand.add(card);
                for (int i = splitHandNum; i < splitHandNum + 2; i++) {
                    Card splitCard = playerHand.get(i);
                    playerAceCount += splitCard.isAce() ? 1 : 0;
                }
                playerSum = 0;
                playerSum += playerHand.get(splitHandNum).getValue();
                playerSum += playerHand.get(splitHandNum+1).getValue();

                if (playerHand.get(splitHandNum).getValue() != playerHand.get(splitHandNum+1).getValue()) {
                    splitButton.setVisible(false);
                }
                gamePanel.repaint();
            }
        });

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

        emptyDeckShuffle();
        hiddenCard = deck.remove(deck.size()-1); //remove card at last index
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        emptyDeckShuffle();
        Card card = deck.remove(deck.size()-1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        //player
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;
        for (int i = 0; i < 2; i++) {
            emptyDeckShuffle();
            card = deck.remove(deck.size()-1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
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

    public void emptyDeckShuffle() {
        if (deck.size() == 0) {
            buildDeck();
            shuffleDeck();
        }
    }

    public void overwritePlayerHand() { // for testing purposes
        playerHand.clear();
        playerSum = 0;
        playerHand.add(new Card("Q","C"));
        playerHand.add(new Card("J", "H"));
        for (int i = 0; i < 2; i++) {
            playerSum += playerHand.get(i).getValue();
        }
    }

    public void overwriteDealerHand() { // for testing purposes
        dealerHand.clear();
        dealerSum = 0;
        playerHand.add(new Card("A","D"));
        playerHand.add(new Card("J", "S"));
        for (int i = 0; i < 2; i++) {
            playerSum += dealerHand.get(i).getValue();
        }
    }

}

/* Things to improve:
 * add split button
 * change colour of buttons
 * Add a message when the deck is shuffled
 * remove overwrite hand functions
 * make comments nicer
 * 
 * remove playerhandoverwrite
 * 
 * ( * work out bets, if bet is 5, then 5 should be removed from balance for every bet, if double pressed, 5 should be removed
 * but bet should only display 10 for that hand and revert to 5 after
 * fix error where bet still being displayed at end of round under betfield after a split (gamepanel.repaint)
 * make it so double or split can't be performed if it will send balance into the negatives.
 * make the split loss/win display start new line every 3 hands
 * 
 * FIX ERROR where on a split, if you bust on the last hand, the dealer won't hand doesn't go to 17
 * 
 * add modularity
 */