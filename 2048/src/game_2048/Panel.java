package game_2048;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Panel {

    private ArrayList<Button> buttons;

    //========================================================================//
    public Panel() {
        buttons = new ArrayList<Button>();
    }

    //========================================================================//
    public void update() {
        for (Button b : buttons) {
            b.update();
        }
    }

    //========================================================================//
    public void draw(Graphics2D g) {
        for (Button b : buttons) {
            b.draw(g);
        }
    }

    //========================================================================//
    public void add(Button button) {
        buttons.add(button);
    }

    public void remove(Button button) {
        buttons.remove(button);
    }

    //========================================================================//
    public void mouseReleased(MouseEvent e) {
        for (Button b : buttons) {
            b.mouseReleased(e);
        }
    }

    public void mouseDragged(MouseEvent e) {
        for (Button b : buttons) {
            b.mouseDragged(e);
        }
    }

    public void mouseMoved(MouseEvent e) {
        for (Button b : buttons) {
            b.mouseMoved(e);
        }
    }
}
