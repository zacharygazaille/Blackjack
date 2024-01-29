package Game;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    ArrayList<Card> hand = new ArrayList<>();

    //get card at index i
    public Card get(int i) {
        return hand.get(i);
    }

    //return size of hand
    public int size() {
        return hand.size();
    }

    //add card to hand
    public void add(Card card) {
        hand.add(card);
    }

    //return a sub-list
    public List<Card> subList(int x, int y) {
        return hand.subList(x, y);
    }

    //remove last card
    public void remove(int i) {
        hand.remove(i);
    }

    //clear hand
    public void clear() {
        hand.clear();
    }

    //building the dealer's hand
    public Card buildDealerHand(Logic logic, Deck deck, Card hiddenCard) {
        logic.dealerSum = 0;    //setting sum and ace count to 0
        logic.dealerAceCount = 0;

        deck.emptyDeckShuffle();
        hiddenCard = deck.getDeck().remove(deck.getDeck().size()-1); //remove card at last index
        logic.dealerSum += hiddenCard.getValue();
        logic.dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        deck.emptyDeckShuffle();
        Card card = deck.getDeck().remove(deck.getDeck().size()-1); //add next card to dealer deck
        logic.dealerSum += card.getValue();
        logic.dealerAceCount += card.isAce() ? 1 : 0;
        hand.add(card);
        return hiddenCard;
    }

    //building the player's hand
    public void buildPlayerHand(Logic logic, Deck deck) {
        logic.playerSum = 0;    //setting sum and ace count to 0
        logic.playerAceCount = 0;
        for (int i = 0; i < 2; i++) {   //adding top two cards from deck to player hand
            deck.emptyDeckShuffle();
            Card card = deck.getDeck().remove(deck.getDeck().size()-1);
            logic.playerSum += card.getValue();
            logic.playerAceCount += card.isAce() ? 1 : 0;
            hand.add(card);
        }
    }

    //adding a card to player deck
    public void addCard(Logic logic, Deck deck) {
        Card card = deck.getDeck().remove(deck.getDeck().size()-1);
        logic.playerSum += card.getValue();
        logic.playerAceCount += card.isAce() ? 1 : 0;
        hand.add(card);
    }

    //creating the next hand in the split
    public void newSplitHand(Logic logic, Deck deck) {
        logic.betDisplay = logic.betAmount; //resetting variables
        logic.playerAceCount = 0;                            
        logic.splitHandNum -= 1;
        deck.shuffleMessage = "";
        for (int i = hand.size()-1; i > logic.splitHandNum; i--){   //clearing the previous hand
            hand.remove(i);
        }
        split(logic, deck);
    }

    //splitting the player's current hand
    public void split(Logic logic, Deck deck) {
        Card card = deck.getDeck().remove(deck.getDeck().size()-1); //adding a card to the new hand
        hand.add(card);
        for (int i = logic.splitHandNum; i < logic.splitHandNum + 2; i++) { //checking whether the new cards are aces
            Card splitCard = hand.get(i);
            logic.playerAceCount += splitCard.isAce() ? 1 : 0;
        }
        logic.playerSum = 0;
        logic.playerSum += hand.get(logic.splitHandNum).getValue(); //adding cards to sum
        logic.playerSum += hand.get(logic.splitHandNum+1).getValue();
    }

    //player presses hit
    public void playerHit(Logic logic, Deck deck) {
        logic.betMessage = "";
        deck.shuffleMessage = "";
        deck.emptyDeckShuffle();
        Card card = deck.getDeck().remove(deck.getDeck().size()-1); //adding a card to player hand
        logic.playerSum += card.getValue();
        logic.playerAceCount += card.isAce() ? 1 : 0;
        hand.add(card);
    }

    //player presses stand
    public void playerStand(Logic logic, Deck deck) {
        logic.betMessage = "";
        deck.shuffleMessage = "";
        if (logic.splitHandNum == 0) {
            playDealer(logic, deck);
        }
    }

    //playing the dealer's hand until seventeen reached
    public void playDealer(Logic logic, Deck deck) {
        while (logic.reduceDealerAce() < 17) {  
            deck.emptyDeckShuffle();
            Card card = deck.getDeck().remove(deck.getDeck().size()-1);
            logic.dealerSum += card.getValue(); //adding cards to dealer's sum
            logic.dealerAceCount += card.isAce() ? 1 : 0;
            hand.add(card);
        }
    }
}
