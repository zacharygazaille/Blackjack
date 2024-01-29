package Game;

import java.util.ArrayList;

public class Logic {
    //dealer
    int dealerSum;
    int dealerAceCount;
    
    //player
    int playerSum;
    int playerAceCount;
    String winLoseMessage;

    //betting
    Double playerBalance = 5000.00;
    Double betAmount;
    Double betDisplay;
    boolean addBet = true;
    boolean displayBet = false;
    String betMessage = "";

    //splits
    boolean displayMessage = true;
    int totalSplits = 0;
    int splitHandNum = 0;
    boolean isSplit = false;
    ArrayList<Integer> splitSums = new ArrayList<Integer>();
    ArrayList<Integer> splitDoubles = new ArrayList<Integer>();
    int firstLoop = 0;
    boolean isNextRoundButtonTrue = false;
    String splitMessage = "";

    //stand button pressed
    public String standPressed(Hand playerHand, Hand dealerHand) {
        dealerSum = reduceDealerAce();
        playerSum = reducePlayerAce();

        winLoseMessage = "";
        if (playerSum > 21) {   //player goes bust
            winLoseMessage = "You go bust!";
        }
        else if (dealerSum > 21) {  //dealer goes bust
            winLoseMessage = "Dealer goes bust!";
            if (addBet == true) {
                playerBalance += 2 * betAmount;
            }
        }
        else if (playerSum == dealerSum) { //dealer and player have same amount
            winLoseMessage = "Push!";
            if (addBet == true) {
                playerBalance += betAmount;
            }
        }
        else if (playerSum == 21 && playerHand.subList(splitHandNum, playerHand.size()).size() == 2) {  //player has blackjack
            winLoseMessage = ("Blackjack!");
            if (addBet == true) {
                playerBalance += 2.5 * betAmount;
            }
        }
        else if (dealerSum == 21 && dealerHand.size() == 1) {   //dealer has blackjack
            winLoseMessage = ("Dealer has Blackjack!");
        }
        else if (playerSum > dealerSum) {   //player wins
            winLoseMessage = ("You win!");
            if (addBet == true) {
                playerBalance += 2 * betAmount;
            }
        }
        else if (playerSum < dealerSum) {   //dealer wins
            winLoseMessage = "Dealer wins!";
        }
        addBet = false;
        return winLoseMessage;
    }

    //stand button pressed on a split hand
    public void standPressedSplit(Deck deck, Hand dealerHand) {
        while (reduceDealerAce() < 17) {    //playing the dealer hand
            deck.emptyDeckShuffle();
            Card card = deck.getDeck().remove(deck.getDeck().size()-1);
            dealerSum = card.getValue();
            dealerAceCount += card.isAce() ? 1 : 0;
            dealerHand.add(card);
        }
        if (firstLoop == 0) {   //adding the last sum from the split hand
            splitSums.add(playerSum);
        }
        int x = 1;
        splitMessage = "";  //creating message for split losses and wins
        for (int i = 0; i < splitSums.size(); i++) {
            splitMessage = splitMessage + "Hand " + String.valueOf(x) + ": ";
            if (splitSums.get(splitSums.size()-1-i) > 21) { //player busts
                splitMessage += "Loss";
            } else if (splitSums.get(splitSums.size()-1-i) > dealerSum) {   //player wins
                splitMessage += "Win";
                if (firstLoop == 0) {
                    if (splitDoubles.get(x-1) > 0) {
                        playerBalance += 4 * betAmount;
                    } else {
                        playerBalance += 2 * betAmount;
                    }
                }
            } else if (dealerSum > 21) {    //dealer busts
                splitMessage += "Win";
                if (firstLoop == 0) {
                    if (splitDoubles.get(x-1) > 0) {
                        playerBalance += 4 * betAmount;
                    } else {
                        playerBalance += 2 * betAmount;
                    }
                }
            } else if (splitSums.get(splitSums.size()-1-i) < dealerSum) {   //dealer wins
                splitMessage += "Loss";
            } else {    //dealer and player have same amount
                splitMessage += "Push";
                if (firstLoop == 0) {
                    if (splitDoubles.get(x-1) > 0) {
                        playerBalance += 2 * betAmount;
                    } else {
                        playerBalance += betAmount;
                    }
                }
            }
            if (x != splitSums.size()) {
                splitMessage += ", ";
            }
            if (x % 3 == 0) {   //splitting the line for every third hand
                splitMessage += "\n";
            }
            x += 1;
        }
        firstLoop += 1;
    }
    
    //changes player ace to 1
    public int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10;
            playerAceCount -= 1;
        }
        return playerSum;
    }

    //changes dealer ace to 1
    public int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }

    //resetting variables for new round
    public void roundReset() {
        splitDoubles.clear();
        splitDoubles.add(0);
        isSplit = false;
        firstLoop = 0;
        splitSums.clear();
        totalSplits = 0;
    }

    //changes meesage if player busts
    public void playerBust() {
        splitMessage = "";
        if (playerSum > 21) {
            splitMessage = "You go bust!";
        }
    }

    //set the player bet
    public void setBet() {
        splitDoubles.add(0);
        betAmount = Math.round(betAmount * 100) / (double)100;
        betDisplay = betAmount;
    }

    //resetting the player balance
    public void resetBalance() {
        betMessage = "";
        playerBalance = 5000.0;
    }

    //doubling the player's bet
    public void doubleBet() {
        if (isSplit){
            betDisplay = 2 * betAmount;
            playerBalance -= betAmount;
            splitDoubles.set(splitHandNum, 1);
        } else {
            playerBalance -= betAmount;
            betAmount *= 2;
            betDisplay = betAmount;
        }
    }

    //setting up for a player hand split
    public void handSplit() {
        splitDoubles.add(0);
        betMessage = "";
        playerBalance -= betAmount;
        isSplit = true;
        totalSplits +=1;
        splitHandNum+=1;
        playerAceCount = 0;
    }
}
