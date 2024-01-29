package Game;

import java.awt.*;

public class Theme {
    boolean firstTimeThrough = true;
    int themeChanges = 0;
    Color myGold = new Color(179, 147, 60);
    String themeMessage = "";
    String cardTheme = "darkCards";
    Color buttonColor = myGold;
    Color backgroundColor = new Color(100, 0, 0);

    //Fonts
    Font titleFont = new Font("Arial", Font.BOLD, 140);
    Font font1 = new Font("Arial", Font.BOLD, 25);
    Font font2 = new Font("Arial", Font.BOLD, 30);
    Font font3 = new Font("Arial", Font.BOLD, 20);
    Font font4 = new Font("Arial", Font.BOLD, 15);
    Font font5 = new Font("Arial", Font.BOLD, 24);

    //check and change theme
    public void checkTheme() {
        if (themeChanges % 2 == 0) {
            themeMessage = "Dark";
            buttonColor = myGold;                
            backgroundColor = new Color(100, 0, 0);
            cardTheme = "darkCards";
        } else {
            themeMessage = "Light";
            buttonColor = Color.white;
            backgroundColor = new Color(0, 120, 0);
            cardTheme = "lightCards";
        }
    }
}
