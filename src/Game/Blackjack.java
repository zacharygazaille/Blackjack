package Game;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.*;

public class Blackjack {
    
    Deck deck = new Deck();
    Logic logic = new Logic();
    Hand playerHand = new Hand();
    Hand dealerHand = new Hand();
    Card hiddenCard;
    Theme theme = new Theme();

    //window
    int boardWidth = 900; // change board size
    int boardHeight = 600;

    //create JFrame
    JFrame frame = new JFrame("Blackjack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {

                //check and change theme
                theme.checkTheme();
                g.setColor(theme.buttonColor);                
                gamePanel.setBackground(theme.backgroundColor);

                //display bet and balance
                g.setFont(theme.font1);
                NumberFormat formatter = new DecimalFormat("#0.00");
                g.drawString("Bet:", 20, 300);
                logic.playerBalance = Math.round(logic.playerBalance * 100) / (double)100;
                g.drawString("Balance: "+String.valueOf(formatter.format(logic.playerBalance)), 20, 255);
                if (logic.displayBet) {
                    g.drawString(String.valueOf(formatter.format(logic.betDisplay)), 75, 300);
                }

                //display split hand number
                if (logic.isSplit == true) {
                    g.drawString("Hand "+String.valueOf(logic.splitHandNum+1), boardWidth - 270, boardHeight - 70);
                }

                //change bet and shuffle message
                g.setFont(theme.font2);
                g.drawString(logic.betMessage, 290, 260);
                g.setFont(theme.font3);
                g.drawString(deck.shuffleMessage, 112, 209);

                //start menu
                if (startButton.isEnabled()) {
                    g.setFont(theme.titleFont);
                    g.drawString("Blackjack", 110, 185);
                    for (int i = 0; i < 6; i++) {
                        Image cardImg = new ImageIcon(getClass().getResource("./"+theme.cardTheme+hiddenCard.menuCards[i])).getImage();
                        g.drawImage(cardImg, 95 + (hiddenCard.cardWidth + 5)*i, 320, hiddenCard.cardWidth, hiddenCard.cardHeight, null);
                    }
                    g.setFont(theme.font4);
                    g.drawString(theme.themeMessage, boardWidth-85, 40);
                } else {

                    //draw hidden card
                    Image hiddenCardImg = new ImageIcon(getClass().getResource("./"+theme.cardTheme+"/BACK.png")).getImage();
                    if (!standButton.isEnabled() && logic.splitHandNum == 0) {
                        hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath(theme.cardTheme))).getImage();
                    }
                    g.drawImage(hiddenCardImg, 20, 20, hiddenCard.cardWidth, hiddenCard.cardHeight, null);

                    //draw dealer's hand
                    for (int i = 0; i < dealerHand.size(); i++) {
                        Card card = dealerHand.get(i);
                        Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath(theme.cardTheme))).getImage();
                        g.drawImage(cardImg, hiddenCard.cardWidth + 25 + (hiddenCard.cardWidth + 5)*i, 20, hiddenCard.cardWidth, hiddenCard.cardHeight, null);
                    }

                    //draw player's hand
                    for (int i = logic.splitHandNum; i < playerHand.size(); i++) {
                        Card card = playerHand.get(i);
                        Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath(theme.cardTheme))).getImage();
                        g.drawImage(cardImg, 20 + (hiddenCard.cardWidth + 5)*(i-logic.splitHandNum), 320, hiddenCard.cardWidth, hiddenCard.cardHeight, null);
                    }
                    g.setFont(theme.font4);
                    g.drawString(theme.themeMessage, boardWidth-85, 40);
                }      

                //theme change
                if (!themeButton.isEnabled()) {
                    themeButton.setEnabled(true);
                    theme.themeChanges++;       
                    gamePanel.repaint();           
                }

                //stand button pressed
                if (!standButton.isEnabled() && logic.splitHandNum == 0) {
                    //if the hand was split
                    if (logic.isSplit) {
                        logic.standPressedSplit(deck, dealerHand);
                        if (logic.displayMessage == true){
                            g.setFont(theme.font1);
                            int y = 240;
                            for (String line : logic.splitMessage.split("\n")) {
                                g.drawString(line, 290, y);
                                y+=35;
                            }
                        }
                    //if the hand wasn't split
                    } else {
                        logic.standPressed(playerHand, dealerHand);
                        if (logic.displayMessage == true){
                            g.setFont(theme.font2);
                            g.drawString(logic.winLoseMessage, 290, 260);
                        }  
                    }
                    gamePanel.repaint();

                    if (logic.isNextRoundButtonTrue == false) {
                        //readying the JFrame for end of round
                        gamePanel.add(betField);
                        logic.displayBet = false;
                        JButton nextRoundButton = new JButton("Next Round");
                        nextRoundButton.setBounds(boardWidth - 160, boardHeight - 100, 120, 40); // move buttons
                        gamePanel.add(nextRoundButton);
                        nextRoundButton.setFocusable(false);
                        logic.isNextRoundButtonTrue = true;

                        nextRoundButton.addActionListener(new ActionListener() { 
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    //attempting to obtain bet from player
                                    logic.roundReset();
                                    logic.betAmount = Double.parseDouble(betField.getText());
                                    logic.betAmount = Math.round(logic.betAmount * 100) / (double)100;
                                    logic.betDisplay = logic.betAmount;
                                    if (logic.betAmount < 0.01) {
                                        logic.betMessage = "Bet amount is too small";
                                        logic.displayMessage = false;
                                    } else if (logic.betAmount > logic.playerBalance) {
                                        logic.betMessage = "Bet amount exceeds player balance";
                                        logic.displayMessage = false;
                                    } else {
                                        //resetting game panel
                                        doubleButton.setVisible(true);
                                        logic.betMessage = "";
                                        deck.shuffleMessage = "";
                                        logic.playerBalance -= logic.betAmount;
                                        hitButton.setEnabled(true);
                                        standButton.setEnabled(true);
                                        gamePanel.remove(nextRoundButton);
                                        logic.isNextRoundButtonTrue = false;
                                        gamePanel.remove(betField);
                                        logic.displayBet = true;

                                        //clearing hands
                                        playerHand.clear();
                                        dealerHand.clear();

                                        //rebuilding dealer and player hands
                                        hiddenCard = dealerHand.buildDealerHand(logic, deck, hiddenCard);
                                        playerHand.buildPlayerHand(logic, deck);
                                        
                                        //checking if player hand is splittable
                                        if (playerHand.get(0).getValue() == playerHand.get(1).getValue()){
                                            splitButton.setVisible(true);
                                        }

                                        //ending round if player or deal3er has blackjack
                                        if (logic.playerSum == 21 || logic.dealerSum == 21) {
                                            doubleButton.setVisible(false);
                                            splitButton.setVisible(false);
                                            hitButton.setEnabled(false);
                                            standButton.setEnabled(false);
                                        }
                                        logic.addBet = true;
                                        logic.displayMessage = true;
                                    }
                                    gamePanel.repaint();
                                } catch (Exception f) {
                                    //if player does not enter a valid bet
                                    logic.betMessage = "Please enter a valid bet";
                                    logic.displayMessage = false;
                                    gamePanel.repaint();
                                }
                            }
                        });

                    }                     
                } else if (!standButton.isEnabled()) {
                    //stand button is pressed on a split hand
                    logic.playerBust();
                    if (logic.displayMessage == true){
                        g.setFont(theme.font2);
                        g.drawString(logic.splitMessage, 250, 260);
                    }

                    //setting up the end of a split hand
                    logic.splitSums.add(logic.playerSum);
                    JButton nextHandButton = new JButton("Next Hand");
                    nextHandButton.setBounds(boardWidth - 160, boardHeight - 100, 120, 40); // move buttons
                    gamePanel.add(nextHandButton);
                    nextHandButton.setFocusable(false);

                    nextHandButton.addActionListener(new ActionListener() { 
                        public void actionPerformed(ActionEvent e) {
                            //building a new split hand
                            playerHand.newSplitHand(logic, deck);
                            //setting all the buttons
                            hitButton.setEnabled(true);
                            standButton.setEnabled(true);
                            splitButton.setVisible(false);
                            doubleButton.setVisible(true);
                            gamePanel.remove(nextHandButton);
                            gamePanel.repaint();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //creating all the buttons
    JButton startButton = new JButton("Start Game");
    JButton hitButton = new JButton("Hit");
    JButton standButton = new JButton("Stand");
    JButton themeButton = new JButton("Theme");
    JTextField betField = new JTextField(20);
    JButton doubleButton = new JButton("Double");
    JButton splitButton = new JButton("Split");
    JButton resetBalance = new JButton("Reset");

    Blackjack() {
        //building frame
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //building game panel
        gamePanel.setLayout(null);
        gamePanel.setBackground(new Color(100, 0, 0)); //change background r g b
        frame.add(gamePanel);

        //moving and setting buttona
        hitButton.setBounds(20, boardHeight - 100, 80, 40);
        gamePanel.add(hitButton);
        hitButton.setFocusable(false);
        hitButton.setVisible(false);
        standButton.setBounds(115, boardHeight - 100, 80, 40);
        gamePanel.add(standButton);
        standButton.setFocusable(false);
        standButton.setVisible(false);
        doubleButton.setBounds(210, boardHeight - 100, 80, 40);
        gamePanel.add(doubleButton);
        doubleButton.setFocusable(false);
        doubleButton.setVisible(false);
        splitButton.setBounds(305, boardHeight - 100, 80, 40);
        gamePanel.add(splitButton);
        splitButton.setFocusable(false);
        splitButton.setVisible(false);
        resetBalance.setBounds(20, 190, 67, 25);
        gamePanel.add(resetBalance);
        resetBalance.setFocusable(false);
        resetBalance.setVisible(false);
        themeButton.setBounds(boardWidth - 180, 20, 75, 30);
        gamePanel.add(themeButton);
        themeButton.setFocusable(false);
        startButton.setBounds(boardWidth / 2 - 73, boardHeight - 100, 120, 40);
        gamePanel.add(startButton);
        startButton.setFocusable(false);
        betField.setFont(theme.font5);
        betField.setBounds(80, 275, 80, 30);
        gamePanel.add(betField);

        //start button is pressed
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    logic.betAmount = Double.parseDouble(betField.getText());
                    logic.setBet();

                    //checking whether player bet is reasonable
                    if (logic.betAmount < 0.01) {
                        logic.betMessage = "Bet amount is too small";
                    } else if (logic.betAmount > logic.playerBalance) {
                        logic.betMessage = "Bet amount exceeds player balance";
                    } else {
                        //setting the game scene
                        resetBalance.setVisible(true);
                        logic.betMessage = "";
                        logic.playerBalance -= logic.betAmount;
                        hitButton.setVisible(true);
                        standButton.setVisible(true);
                        doubleButton.setVisible(true);
                        startButton.setEnabled(false);
                        gamePanel.remove(startButton);
                        gamePanel.remove(betField);
                        logic.displayBet = true;
                        if (logic.dealerSum == 21 || logic.playerSum == 21) {
                            doubleButton.setVisible(false);
                            splitButton.setVisible(false);
                            hitButton.setEnabled(false);
                            standButton.setEnabled(false);
                        } else if (playerHand.get(0).getValue() == playerHand.get(1).getValue()){
                            splitButton.setVisible(true);
                        }
                    }
                    gamePanel.repaint();
                } catch (Exception f) {
                    //player bet is not valid
                    logic.betMessage = "Please enter a valid bet";
                    gamePanel.repaint();
                }
                
            }
        });

        //button to change theme
        themeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                theme.firstTimeThrough = false;
                themeButton.setEnabled(false);
                gamePanel.repaint();
            }
        });

        //button to reset balance
        resetBalance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logic.resetBalance();
                gamePanel.repaint();
            }
        });

        //button to take card from deck onto player hand
        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playerHand.playerHit(logic, deck);
                splitButton.setVisible(false);
                doubleButton.setVisible(false);
                if (logic.reducePlayerAce() > 21) { //ending round if player busts
                    hitButton.setEnabled(false);
                    standButton.setEnabled(false);
                }
                gamePanel.repaint();
            }
        });

        //button for player to stay with current hand
        standButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dealerHand.playerStand(logic, deck);
                splitButton.setVisible(false);
                doubleButton.setVisible(false);
                hitButton.setEnabled(false);
                standButton.setEnabled(false);
                gamePanel.repaint();
            }
        });

        //button to double bet for one more card
        doubleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deck.shuffleMessage = "";
                if (logic.playerBalance - logic.betAmount < 0) {    //checking whether player has enough in balance
                    logic.betMessage = "Not enough left in balance";
                } else {
                    logic.betMessage = "";
                    doubleButton.setVisible(false);
                    splitButton.setVisible(false);
                    logic.doubleBet();  //doubling bet
                    playerHand.addCard(logic, deck);    //adding card to player hand
                    if (logic.splitHandNum == 0){   //playing dealer hand if it's last round
                        dealerHand.playDealer(logic, deck);
                    }
                    hitButton.setEnabled(false);
                    standButton.setEnabled(false);
                }
                gamePanel.repaint();
            }
        });
        
        //button pressed to split the player's current hand
        splitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deck.shuffleMessage = "";
                if (logic.playerBalance - logic.betAmount < 0) {    //checking whether player has enough in balance to split
                    logic.betMessage = "Not enough left in balance";
                } else {
                    logic.handSplit();
                    playerHand.split(logic, deck); //splitting the player's hand
                    if (playerHand.get(logic.splitHandNum).getValue() != playerHand.get(logic.splitHandNum+1).getValue()) { //ending split if cards aren't same
                        splitButton.setVisible(false);
                    }
                }
                gamePanel.repaint();
            }
        });
        gamePanel.repaint();
    }

    public void startGame() {
        //building game deck
        deck.buildDeck();
        deck.shuffleDeck();

        //building dealer hand
        hiddenCard = dealerHand.buildDealerHand(logic, deck, hiddenCard);

        //building player hand
        playerHand.buildPlayerHand(logic, deck);

        gamePanel.repaint();
    }
    

}