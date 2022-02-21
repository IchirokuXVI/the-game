package iestrassierra.dcorsan.thegame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;

public class Player {
    public static float speed = 1f;
    public static float animationDuration = 0.15f;

    private Texture img;

    public float stateTime = 0f;

    private Animation top;
    private Animation right;
    private Animation down;
    private Animation left;

    private Animation active = null;


}
