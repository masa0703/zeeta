package jp.tokyo.selj.common;

import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import jp.tokyo.selj.view.Util;


public class PreferenceWindowHelper {

	Preferences prefs_;
    private final Dimension dim = new Dimension();
    private final Point pos = new Point();
	Window window_;
	String key_;
	
	public 	Preferences getPriference(){
		return prefs_;
	}
	
	public PreferenceWindowHelper(Window w){
		window_ = w;
		key_ = Util.getClassName(w)+ "/" + w.getName() + "_";
		prefs_ = Preferences.userNodeForPackage(w.getClass());
        w.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveLocation();
				super.windowClosing(e);
            }

//			@Override   一瞬妙な場所に表示されてから移動するのでやめた
//			public void windowOpened(WindowEvent e) {
//				restoreForm();
//				super.windowOpened(e);
//			}
        });
        w.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
//                if(window_.getExtendedState()==JFrame.NORMAL) {
                if( window_.isShowing()) {
                    try{
                        pos.setLocation(window_.getLocationOnScreen());
                    }catch(IllegalComponentStateException icse) {}
                }
            }
            public void componentResized(ComponentEvent e) {
//                if(window_.getExtendedState()==JFrame.NORMAL) {
                if( window_.isShowing()) {
                    dim.setSize(window_.getSize());
                }
            }
        });

	}
    private void saveLocation() {
        if(dim.width < 1 && dim.height < 1){		//一度も移動もリサイズもしないと0になる
        	return;
        }
    	prefs_.putInt(key_+"locx", pos.x);
    	prefs_.putInt(key_+"locy", pos.y);
    	prefs_.putInt(key_+"dimw", dim.width);
    	prefs_.putInt(key_+"dimh", dim.height);
    	try{
            prefs_.flush();
        }catch(java.util.prefs.BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }
    public void restoreForm(){
        int wdim = prefs_.getInt(key_+"dimw", window_.getSize().width);
        int hdim = prefs_.getInt(key_+"dimh", window_.getSize().height);
        if(wdim > 0 && hdim > 0){		//一度も移動もリサイズもしないと0になる
        	window_.setSize(wdim, hdim);
        }
        
        Rectangle screen = window_.getGraphicsConfiguration().getBounds();
        pos.setLocation(screen.x + screen.width/2  - window_.getSize().width/2,
                        screen.y + screen.height/2 - window_.getSize().height/2);
        int xpos = prefs_.getInt(key_+"locx", pos.x);
        int ypos = prefs_.getInt(key_+"locy", pos.y);
        pos.setLocation(xpos,ypos);
        window_.setLocation(pos.x, pos.y);

    }
}
