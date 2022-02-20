package iestrassierra.dcorsan.thegame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public static float stateTime = 0.75f;
    public static int amount = 5;
    public static float speed = 1f;
    public static float animationDuration = 0.15f;

    private Texture img;

    private Animation top;
    private Animation right;
    private Animation down;
    private Animation left;

    private Animation active = null;

    private Vector2 source;
    private Vector2 destination;

    private Vector2 position;

    public Enemy() { }

    /**
     * Splits the image and sets the top, right, down and left animations. Defaults active to down
     * First image row -> down
     * Second image row -> left
     * Third image row -> right
     * Fourth image row -> top
     * @param img
     * @param source
     * @param destination
     */
    public Enemy(Texture img, Vector2 source, Vector2 destination) {
        this.img = img;
        TextureRegion[][] tr = TextureRegion.split(img, img.getWidth() / TheGame.FRAME_COLS, img.getHeight() / TheGame.FRAME_ROWS);



        this.down = new Animation(animationDuration, tr[0]);
        this.down.setPlayMode(Animation.PlayMode.LOOP);

        this.left = new Animation(animationDuration, tr[1]);
        this.left.setPlayMode(Animation.PlayMode.LOOP);

        this.right = new Animation(animationDuration, tr[2]);
        this.right.setPlayMode(Animation.PlayMode.LOOP);

        this.top = new Animation(animationDuration, tr[3]);
        this.top.setPlayMode(Animation.PlayMode.LOOP);

        // Default active animation will be down
        this.active = down;

        this.source = source;
        this.position = new Vector2(source);
        this.destination = destination;
    }

    /**
     * All arguments constructor
     * @param top
     * @param right
     * @param down
     * @param left
     * @param active
     * @param source
     * @param destination
     * @param position
     */
    public Enemy(Animation top, Animation right, Animation down, Animation left, Animation active, Vector2 source, Vector2 destination, Vector2 position) {
        this.top = top;
        this.right = right;
        this.down = down;
        this.left = left;
        this.active = active;
        this.source = source;
        this.destination = destination;
        this.position = position;
    }

    public void move() {
        if (position.y < destination.y) {
            position.y += speed;
            active = top;
        }
        if (position.y > destination.y) {
            position.y -= speed;
            active = down;
        }
        if (position.x < destination.x) {
            position.x += speed;
            active = right;
        }
        if (position.x > destination.x) {
            position.x -= speed;
            active = left;
        }

        position.x = MathUtils.clamp(position.x, 0, TheGame.anchoMapa - TheGame.anchoJugador);
        position.y = MathUtils.clamp(position.y, 0, TheGame.altoMapa - TheGame.altoJugador);

        //Dar la vuelta al NPC cuando llega a un extremo
        if (position.epsilonEquals(destination)) {
            destination.set(source);
            source.set(position);
        }
    }

    public Texture getImg() {
        return img;
    }

    public void setImg(Texture img) {
        this.img = img;
    }

    public Animation getTop() {
        return top;
    }

    public void setTop(Animation top) {
        this.top = top;
    }

    public Animation getRight() {
        return right;
    }

    public void setRight(Animation right) {
        this.right = right;
    }

    public Animation getDown() {
        return down;
    }

    public void setDown(Animation down) {
        this.down = down;
    }

    public Animation getLeft() {
        return left;
    }

    public void setLeft(Animation left) {
        this.left = left;
    }

    public Animation getActive() {
        return active;
    }

    public void setActive(Animation active) {
        this.active = active;
    }

    public Vector2 getSource() {
        return source;
    }

    public void setSource(Vector2 source) {
        this.source = source;
    }

    public Vector2 getDestination() {
        return destination;
    }

    public void setDestination(Vector2 destination) {
        this.destination = destination;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }
}
