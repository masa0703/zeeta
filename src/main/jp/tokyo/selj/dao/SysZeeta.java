 package jp.tokyo.selj.dao;

import java.io.Serializable;

import org.seasar.dao.annotation.tiger.Bean;

@Bean(table = "syszeeta")
public class SysZeeta implements Serializable {

    private int id;
    private String version;
    private boolean needUserName;
    private String linkNodePref;

    public SysZeeta() {
    }
	public void check(){
    	if(id != 1 ){
    		throw new ModelCheckException("id‚Í1‚µ‚©Žw’è‚Å‚«‚Ü‚¹‚ñ");
    	}
    	if(version == null || (version.trim().length() <= 0) ){
    		throw new ModelCheckException("version‚ª–¢Žw’è‚Å‚·");
    	}
	}

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("ID="+id).append(", ");
        buf.append("db version="+version).append(", ");
        buf.append("needUserName="+needUserName).append(", ");
        buf.append("linkNodePref="+linkNodePref).append(", ");
        return buf.toString();
    }
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLinkNodePref() {
		return linkNodePref;
	}
	public void setLinkNodePref(String linkNodePref) {
		this.linkNodePref = linkNodePref;
	}
	public boolean isNeedUserName() {
		return needUserName;
	}
	public void setNeedUserName(boolean needUserName) {
		this.needUserName = needUserName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

}