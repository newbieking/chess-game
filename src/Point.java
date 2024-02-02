import java.io.Serializable;

public class Point implements Serializable {

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x, y;
}