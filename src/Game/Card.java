package Game;

public class Card {
    int cardWidth = 110; //ratio should be 1/1.4
    int cardHeight = 154;

    String[] menuCards = {"/BACK.png", "/A-H.png", "/A-S.png", "/A-D.png", "/A-C.png", "/BACK.png"};

    String value;
    String type;

    //initialize card
    Card(String value, String type) {
        this.value = value;
        this.type = type;
    }

    //convert card to string
    public String toString() {
        return value + "-" + type;
    }

    //get the numeric value of a face or ace card
    public int getValue() {
        if ("AJQK".contains(value)) { //A J Q K
            if (value == "A") {
                return 11;
            }
            return 10;
        }
        return Integer.parseInt(value); //2-10
    }

    //check if card is ace
    public boolean isAce() {
        return value == "A";
    }

    //return the image path of a card
    public String getImagePath(String cardTheme) {
        return "./"+cardTheme+"/" + toString() + ".png";
    }
}
