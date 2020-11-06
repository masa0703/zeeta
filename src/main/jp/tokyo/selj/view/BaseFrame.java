package jp.tokyo.selj.view;

import java.util.prefs.Preferences;

import javax.swing.JFrame;

import jp.tokyo.selj.common.PreferenceWindowHelper;

public class BaseFrame extends JFrame {
	private PreferenceWindowHelper pref_;
	protected Preferences prefs_ = Preferences.userNodeForPackage(this.getClass());

	public BaseFrame(){
		super();
		pref_ = new PreferenceWindowHelper(this);
	}
    protected void restoreForm(){
    	pref_.restoreForm();
    }
}
