import javax.swing.*;

public class Main{
    public static void main(String[] args) {
        JFrame frame = new JFrame();

        // Create button to connect to the database
        JButton database_connect = new JButton("Button");
        database_connect.addActionListener(e ->{
            Database_Creator.main(new String[]{});
            frame.setVisible(false);
        });


        frame.add(database_connect);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(100,100);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        }
    }