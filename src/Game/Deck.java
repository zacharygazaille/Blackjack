package game;

import java.util.ArrayList;
import java.util.Random;


public class Deck {

    ArrayList<Card> deck = new ArrayList<>();
    Random random = new Random();
    String shuffleMessage = "";

    public ArrayList<Card> getDeck() {
        return deck;
    }

    //build a deck of cards
    public void buildDeck() {
        int deckNumber = 6; // change the number of decks
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (int i = 0; i < deckNumber; i++) {
            for (int j = 0; j < types.length; j++) {
                for (int k = 0; k < values.length; k++) {
                    Card card = new Card(values[k], types[j]);
                    deck.add(card);
                }
            }
        }
    }

    //shuffle the deck
    public void shuffleDeck() {
        for (int i = 0; i< getDeck().size(); i++) {
            int j = random.nextInt(getDeck().size());
            Card currCard = getDeck().get(i);
            Card randomCard = getDeck().get(j);
            getDeck().set(i, randomCard);
            getDeck().set(j, currCard);
        }
    }

    //shuffle deck if it is empty
    public void emptyDeckShuffle() {
        if (getDeck().size() == 0) {
            buildDeck();
            shuffleDeck();
            shuffleMessage = "Deck shuffled";
        }
    }
}
