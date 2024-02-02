import java.io.Serializable;
import java.util.Stack;

public class State implements Serializable {
    public int[][] allChess;
    public Stack<Point> posList;     // 保存每一步的下棋状态 {落子位置, 剩余时间}
    public boolean isBlack;      //保存已经下过的棋子的坐标  0:无子  1：黑子  2：白子
    public boolean canPlay;     //游戏是否继续

    // 黑白子的剩余时间
    public long blackCount;
    public long whiteCount;

    public State(
            int[][] allChess, Stack<Point> posList,
            boolean isBlack, boolean canPlay,
            long blackCount, long whiteCount) {
        this.allChess = allChess;
        this.posList = posList;
        this.isBlack = isBlack;
        this.canPlay = canPlay;
        this.blackCount = blackCount;
        this.whiteCount = whiteCount;
    }


}