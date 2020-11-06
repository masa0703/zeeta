package jp.tokyo.selj.common;

import java.util.prefs.Preferences;

public class SysPreference {
	private static Preferences prefs__ = Preferences.userNodeForPackage(SysPreference.class);

	private static String defaultUserName__ = null; 

	static{
		loadDeafultUserName();
	}
	
	public static void loadDeafultUserName(){
		defaultUserName__ = prefs__.get("defaultUserName", null);
	}

	public static String getDefaultUserName() {
		return defaultUserName__;
	}
	public static void putDefaultUserName(String defaultUserName) {
		if(defaultUserName == null){
			defaultUserName__ = null;
			prefs__.remove("defaultUserName");
		}else{
			defaultUserName__ = defaultUserName;
			prefs__.put("defaultUserName",defaultUserName__);
		}
    	try{
            prefs__.flush();
        }catch(java.util.prefs.BackingStoreException e) {
            throw new RuntimeException(e);
        }

	}

}
