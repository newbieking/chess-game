import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Timer;
import java.util.*;

public class GameFrame extends JFrame {

    public static void main(String[] args) {
        new GameFrame();
    }

    private static final int BLACK_CHESS = 1;
    private static final int WHITE_CHESS = 2;
    private static final int BLANK_CHESS = 0;
    private static final int COUNT = 30;

    private static final int ROW = 15;
    private static final int COLUMN = 15;

    private static final boolean IS_BLACK_FIRST = true;
    private static final int ROBOT_MODE = 1;
    private static final int PLAYER_MODE = 0;

    private int mode = PLAYER_MODE; // 0: 玩家对战模式 1: 人机模式
    private Image buffer;
    private Graphics g;

    // 初始化计时器
    private final transient Timer timer = new Timer();

    // 组件
    private final JButton button = new JButton("悔棋");
    private final JButton restartButton = new JButton("重新开始");
    private final JToggleButton toggleButton = new JToggleButton("开启人机");
    //取得屏幕的宽度和高度
    private final int width = Toolkit.getDefaultToolkit().getScreenSize().width;
    private final int height = Toolkit.getDefaultToolkit().getScreenSize().height;

    // 当前位置
    private final Point currentPos = new Point(0, 0);

    // 未落子的位置
    private final ArrayList<Point> newPosList = new ArrayList<>();

    private final Random random = new Random();
    private State state = new State(
            new int[ROW][COLUMN],
            new Stack<>(),
            IS_BLACK_FIRST,
            true,
            COUNT,
            COUNT
    );

    // 超时提示语
    private static final String BLACK_TIMEOUT = "黑方超时";
    private static final String WHITE_TIMEOUT = "白方超时";


    public GameFrame() {


        initGameFrameListener();
        initGameFrame();
        initComponents();
        initTimer();

    }

    private void initNewPosList() {
        for (int i = 0; i < state.allChess.length; i++) {
            for (int j = 0; j < state.allChess[i].length && state.allChess[i][j] == 0; j++) {
                newPosList.add(new Point(i, j));
            }
        }
    }

    private boolean playChess(MouseEvent e) {
        // 获得当前落子位置
        currentPos.x = e.getX();
        currentPos.y = e.getY();
        if (state.canPlay) {
            // 转化落子位置
            currentPos.x = (currentPos.x - 20) / 28;
            currentPos.y = (currentPos.y - 20) / 28;
            // 判断落子位置是否在棋盘内
            if (currentPos.x >= 0 && currentPos.x < state.allChess.length && currentPos.y >= 0 && currentPos.y < state.allChess.length) {
                state.posList.push(new Point(currentPos.x, currentPos.y)); // 加入历史记录
                state.blackCount = state.whiteCount = COUNT; // 黑白两子重新计时
                // 先判断是否为空位
                if (state.allChess[currentPos.x][currentPos.y] == BLANK_CHESS) {
                    newPosList.remove(new Point(currentPos.x, currentPos.y));
                    // 判断当前所下的棋子是什么颜色
                    if (state.isBlack) {
                        state.allChess[currentPos.x][currentPos.y] = BLACK_CHESS;
                        state.isBlack = false;
                    } else {
                        state.allChess[currentPos.x][currentPos.y] = WHITE_CHESS;
                        state.isBlack = true;
                    }
                    // 判断是否有赢家
                    if (checkWin()) {
                        repaint();
                        JOptionPane.showMessageDialog(GameFrame.this, "游戏结束，"
                                + (state.allChess[currentPos.x][currentPos.y] == 1 ? "黑方" : "白方") + "获胜!");
                        state.canPlay = false;
                        return false;
                    }
                    GameFrame.this.repaint(); // 重新加载棋盘，更新棋盘
                    return true;
                } else { // 位置已经有棋子了
                    JOptionPane.showMessageDialog(GameFrame.this, "当前位置已有棋子，请重新落子！");
                }
            }
        }
        return false;
    }

    private void initGameFrameListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                int ret = JOptionPane.showConfirmDialog(GameFrame.this, "保存本场对局吗?",
                        UIManager.getString("OptionPane.titleText"), JOptionPane.YES_NO_OPTION);
                if (ret == JOptionPane.OK_OPTION) {
                    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data.ser"))) {
                        oos.writeObject(new State(state.allChess, state.posList, state.isBlack, state.canPlay, state.blackCount, state.whiteCount));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else if (ret == JOptionPane.NO_OPTION) {
                    File file = new File("data.ser");
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }

            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                File file = new File("data.ser");
                if (file.exists())
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data.ser"))) {
                        state = (State) ois.readObject();
                    } catch (ClassNotFoundException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                repaint();
                initNewPosList();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (playChess(e) && mode == ROBOT_MODE) {
                    int x = random.nextInt(newPosList.size());
                    Point point = newPosList.get(x);
                    state.posList.push(point);
                    state.allChess[point.x][point.y] = state.isBlack ? BLACK_CHESS : WHITE_CHESS;
                    state.isBlack = !state.isBlack;
                    GameFrame.this.repaint();
                }
            }
        });
    }

    private void initGameFrame() {
        this.setTitle("五子棋游戏（黑棋先下）");
        this.setSize(500, 500);  //窗口大小
        this.setLocation((width - 500) / 2, (height - 500) / 2);  //窗口位置
        this.setResizable(false); //将棋盘设置为大小不可变
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//默认关闭后程序结束
        this.setVisible(true); //将窗口显示出来
    }

    private void initComponents() {
        // 添加悔棋和重新开始按钮
        setLayout(null);
        button.setBounds(370, 410, 90, 20);
        add(button);
        restartButton.setBounds(370, 432, 90, 20);
        add(restartButton);
        toggleButton.setBounds(150, 410, 90, 20);
        add(toggleButton);

        toggleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mode = toggleButton.isSelected() ? ROBOT_MODE : PLAYER_MODE;
            }
        });

        // 复原游戏初始状态
        restartButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < state.allChess.length; i++) {
                    Arrays.fill(state.allChess[i], 0);
                }
                state.isBlack = IS_BLACK_FIRST;
                state.posList.clear();
                state.blackCount = COUNT;
                state.whiteCount = COUNT;
                state.canPlay = true;
                repaint();
            }
        });

        // 恢复到上一步的状态
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (state.posList.size() == 0) {
                    return;
                }

                if (mode == PLAYER_MODE) {
                    Point pos = state.posList.pop();
                    state.allChess[pos.x][pos.y] = 0;
                    state.blackCount = COUNT;
                    state.whiteCount = COUNT;
                    state.canPlay = true;
                    state.isBlack = !state.isBlack;
                } else {
                    if (!state.isBlack) {
                        Point pos = state.posList.pop();
                        state.allChess[pos.x][pos.y] = 0;
                        state.blackCount = COUNT;
                        state.whiteCount = COUNT;
                        state.canPlay = true;
                        state.isBlack = !state.isBlack;
                    } else {
                        Point pos = state.posList.pop();
                        state.allChess[pos.x][pos.y] = 0;
                        pos = state.posList.pop();
                        state.allChess[pos.x][pos.y] = 0;
                        state.blackCount = COUNT;
                        state.whiteCount = COUNT;
                        state.canPlay = true;
                    }
                }


                repaint();
            }
        });
    }

    private void initTimer() {
        // 开始计时器， 每秒黑白两子计数变量自减1
        // 任何一方减为0，则游戏结束
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!state.canPlay) {
                    return;
                }
                if (state.isBlack) {
                    state.blackCount--;
                } else {
                    state.whiteCount--;
                }
                repaint();
                if (state.whiteCount == 0 || state.blackCount == 0) {
                    state.canPlay = !state.canPlay;
                    JOptionPane.showMessageDialog(GameFrame.this,
                            state.isBlack ? BLACK_TIMEOUT : WHITE_TIMEOUT);
                }
            }
        }, 0L, 1000L);
    }

    @Override
    public void paint(Graphics graphics) {
        if (buffer == null) {
            buffer = createImage(getWidth(), getHeight());
            g = buffer.getGraphics();
        }

        super.paint(g); // 调用父类paint方法，绘制通过add()方法添加的那些控件（这里是 悔棋按钮 和 重开按钮）
        g.setFont(new Font("黑体", Font.BOLD, 20));//游戏信息--字体加粗
        g.drawString("游戏信息", 20, 20);  //标题信息
        g.setFont(new Font("宋体", 0, 14));//设置字体
        //时间信息
        g.drawString("黑方时间：" + state.blackCount + "秒", 40, 480);
        g.drawString("白方时间：" + state.whiteCount + "秒", 260, 480);
        //画出棋盘
        for (int i = 0; i < 15; i++) {   //15*15
            g.drawLine(44, 44 + i * 28, 436, i * 28 + 44);
            g.drawLine(44 + i * 28, 44, 44 + i * 28, 436);
        }
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                currentPos.x = 44 + 28 * i;
                currentPos.y = 44 + j * 28;
                if (state.allChess[i][j] == BLACK_CHESS) {  //黑子
                    g.fillOval(currentPos.x - 8, currentPos.y - 8, 16, 16);

                }
                if (state.allChess[i][j] == WHITE_CHESS) {   //白子 圆加白圆
                    g.setColor(Color.WHITE);
                    g.fillOval(currentPos.x - 8, currentPos.y - 8, 16, 16); //实心圆
                    g.setColor(Color.BLACK);
                    g.drawOval(currentPos.x - 8, currentPos.y - 8, 16, 16);//空心圆
                }
            }
        }
        graphics.drawImage(buffer, 0, 0, null);
    }

    // 扫描棋盘，找到棋子相连最多的位置（最可能五子相连的位置）
    private Point scanWinPos() {
        // TODO
        return null;
    }

    // 检查是否产生胜方
    private boolean checkWin() {
        boolean flag = false;
        //判断横向
        int color = state.allChess[currentPos.x][currentPos.y];//我用012表示颜色
        int count = 1;//计数
        int i = 1;
        //right
        while (currentPos.x + i < state.allChess.length && color == state.allChess[currentPos.x + i][currentPos.y]) {
            count++;
            i++;
        }
        i = 1;
        //left
        while (currentPos.x - i >= 0 && color == state.allChess[currentPos.x - i][currentPos.y]) {
            count++;
            i++;
        }
        if (count >= 5) {
            flag = true;
        }
        //纵向判断
        int count2 = 1;//计数
        int i2 = 1;
        //上
        while ((currentPos.y + i2) <= 14 && color == state.allChess[currentPos.x][currentPos.y + i2]) {
            count2++;
            i2++;
        }
        i2 = 1;
        //下
        while ((currentPos.y - i2) >= 0 && color == state.allChess[currentPos.x][currentPos.y - i2]) {
            count2++;
            i2++;
        }
        if (count2 >= 5) {
            flag = true;
        }
        //斜方向
        int count3 = 1;//计数
        int i3 = 1;
        //右上
        while ((currentPos.x + i3) < state.allChess.length && (currentPos.y - i3) >= 0 && color == state.allChess[currentPos.x + i3][currentPos.y - i3]) {
            count3++;
            i3++;
        }
        i3 = 1;
        //左下
        while ((currentPos.x - i3) >= 0 && (currentPos.y + i3) < state.allChess.length && color == state.allChess[currentPos.x - i3][currentPos.y + i3]) {
            count3++;
            i3++;
        }
        if (count3 >= 5) {
            flag = true;
        }
        //斜方向
        int count4 = 1;//计数
        int i4 = 1;
        //左上
        while ((currentPos.x - i4) >= 0 && (currentPos.y - i4) >= 0 && color == state.allChess[currentPos.x - i4][currentPos.y - i4]) {
            count4++;
            i4++;
        }
        i4 = 1;
        //右下
        while ((currentPos.x + i4) < state.allChess.length && (currentPos.y + i4) < state.allChess.length && color == state.allChess[currentPos.x + i4][currentPos.y + i4]) {
            count4++;
            i4++;
        }
        if (count4 >= 5) {
            flag = true;
        }
        return flag;
    }
}