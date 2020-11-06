package jp.tokyo.selj;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

public abstract class Splash {
    private static final String SPLASH_PATH = "/image/splash.png";

    public Splash() {
        super();
        createSplashScreen(SPLASH_PATH);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showSplashScreen();
            }
        });

        //これを入れないと枠しか出ない場合がある
        try{
            Thread.sleep(20);
        }catch(InterruptedException e) {
            System.out.println(e);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	try{
            		execute();
            	}finally{
            		hideSplash();
            	}
            }
        });
    }

    private JWindow   splashScreen;
    private JLabel    splashLabel;

    public void createSplashScreen(String path) {
        ImageIcon img = new ImageIcon(getClass().getResource(path));
        splashLabel  = new JLabel(img);
        splashScreen = new JWindow(new JFrame());
        splashScreen.getContentPane().add(splashLabel);
        splashScreen.pack();
        splashScreen.setLocationRelativeTo(null);
    }

    public void showSplashScreen() {
        splashScreen.setVisible(true);
    }

    public void hideSplash() {
        splashScreen.setVisible(false);
        splashScreen = null;
        splashLabel  = null;
    }

    public abstract void execute();

}
