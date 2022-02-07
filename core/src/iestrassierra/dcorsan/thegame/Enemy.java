package iestrassierra.dcorsan.thegame;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public static float StateTime = 0f;
    public static int amount = 5;
    public static float speed = 10;

    private Animation top;
    private Animation right;
    private Animation down;
    private Animation left;

    private Animation active = null;

    private Vector2 source;
    private Vector2 destination;

    private Vector2 position;

    public Enemy(Animation top, Animation right, Animation down, Animation left, Vector2 source, Vector2 destination) {
        this.top = top;
        this.right = right;
        this.down = down;
        this.left = left;

        this.source = source;
        this.position = source;
        this.destination = destination;
    }
}
