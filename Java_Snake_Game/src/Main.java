import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Timer;

public class Main extends JPanel implements KeyListener {
    public static final int CELL_SIZE = 20;
    public static int width = 400;
    public static int height = 400;
    public static int row = height/CELL_SIZE;
    public static int column = width/CELL_SIZE;
    private Snake snake;
    private Fruit fruit;
    private Timer t;
    private int speed = 100;
    private static String direction;
    private boolean allowKeyPress; // 當按下按鈕時，可以控制蛇，因為在按按鍵時direction的值會快速變化,
    // 要避免快速操作按鍵導致於莫名其妙頭咬到自己尾巴的情況出現，因此讓在畫面更新前無法讓按鍵操作方向
    private int score;
    private int highest_score;
    String desktop = System.getProperty("user.home") + "/Desktop/";
    String myFile = desktop + "score.txt";
    public Main(){
        read_highest_score();
        reset();
        addKeyListener(this);
    }
    private void setTimer(){
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                repaint();  //執行paintComponent
            }
        },0,speed); //delay為0,每隔一段時間就去執行,單位為毫秒 100為01.s
    }
    public void reset(){
        score = 0;
        if (snake != null){
            snake.getSnakeBody().clear();
        }
        allowKeyPress = true;//預設可以控制蛇方向
        direction = "Right";//預設蛇方向往右跑
        snake = new Snake();
        fruit = new Fruit();
        setTimer();
    }

    @Override
    public void paintComponent(Graphics g){
        //   System.out.println("We are calling paint component..."); //測試Timer是否運作正常

        ArrayList<Node> snake_body = snake.getSnakeBody();
        Node head = snake_body.get(0);
        for (int i = 1; i<snake_body.size(); i++){
            if (snake_body.get(i).x == head.x && snake_body.get(i).y == head.y){
                allowKeyPress = false;
                t.cancel();
                t.purge();//將遊戲暫停
                int response = JOptionPane.showOptionDialog(this,"Game Over !!! Your score is "+ score + ". The highest score was "+ highest_score +" Would you like to start over ?","Game Over",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE,null,null,JOptionPane.YES_OPTION);
                write_a_file(score);
                switch(response){
                    case JOptionPane.CLOSED_OPTION:
                        System.exit(0);
                        break;
                    case JOptionPane.NO_OPTION:
                        System.exit(0);
                        break;
                    case JOptionPane.YES_OPTION:
                        reset();
                        return;
                }
            }
        }
        // draw a black background
        g.fillRect(0,0,width,height);
        snake.drawSnake(g);
        fruit.drawFruit(g);

        // remove snake tail and put it in head
        int snakeX = snake.getSnakeBody().get(0).x; //get(0),取得arrayList index 0 的x 也就是頭
        int snakeY = snake.getSnakeBody().get(0).y;

        if(direction.equals("Right")){
            snakeX += CELL_SIZE;
        }else if (direction.equals("Left")){
            snakeX -= CELL_SIZE;
        }else if (direction.equals("Up")){
            snakeY -= CELL_SIZE;
        }else if (direction.equals("Down")){
            snakeY += CELL_SIZE;
        }
        Node newHead = new Node(snakeX,snakeY);

        // check if snake eating the fruit
        if (snake.getSnakeBody().get(0).x == fruit.getX() && snake.getSnakeBody().get(0).y == fruit.getY()){
            //System.out.println("The snake eating the fruit !!!");
            //set fruit to a new location
            fruit.drawFruit(g);
            fruit.setNewLocation(snake);
            score++;
        }else {
            snake.getSnakeBody().remove(snake.getSnakeBody().size()-1);  //尾巴移除
        }


        snake.getSnakeBody().add(0,newHead); //newHead add 到index 0 也就是頭

        allowKeyPress = true;  //等到畫面更新才能控制蛇方向
        requestFocusInWindow();
    }

    @Override
    public Dimension getPreferredSize(){
        return new Dimension(width,height); //將視窗大小固定(400,400)
    }

    public static void main(String[] args) {
        JFrame window = new JFrame("Snake Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setContentPane(new Main());
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.setResizable(false);  //視窗無法隨意放大縮小
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(allowKeyPress){
            int k = e.getKeyCode();
            if ((k == KeyEvent.VK_UP || k == KeyEvent.VK_W)  && !direction.equals("Down")) {
                direction = "Up"; //假如要往上，原方向不能為下，也就是說原本往下，不能往上
            } else if ((k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S)  && !direction.equals("Up")) {
                direction = "Down";
            } else if ((k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A)  && !direction.equals("Right")) {
                direction = "Left";
            }else if ((k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D)  && !direction.equals("Left")) {
                direction = "Right";
            }
            allowKeyPress = false; //剛按完按鍵先將按鍵無法改變方向
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void read_highest_score(){
        try {
            File myObj = new File(myFile);
            Scanner myReader = new Scanner(myObj);
            highest_score = myReader.nextInt();
            myReader.close();
        }catch (FileNotFoundException e){ //如果沒有找到檔案，代表從來沒有玩過
            highest_score = 0;
            try {
                File myObj = new File(myFile); //新增一個檔案
                if (myObj.createNewFile()){
                    System.out.println("File created: "+myObj.getName());
                }
                FileWriter myWriter = new FileWriter(myObj.getName());
                myWriter.write(""+0); //將檔案寫入0
            }catch (IOException err){
                System.out.println("An error occurred");
                err.printStackTrace();
            }
        }
    }

    public void write_a_file(int score){
        try {
            FileWriter myWriter = new FileWriter(myFile);
            if (score > highest_score){
                myWriter.write(""+ score);
                highest_score = score;
            }else {
                myWriter.write(""+highest_score);
            }
            myWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}