package jp.tokyo.selj.model;

import java.util.List;

import javax.swing.DefaultListModel;

public class CommonListModel extends DefaultListModel{

	public void setList(List list){
		for(int i=0; i<list.size();i++){
			addElement(list.get(i));
		}
	}
	public void setArray(Object[] objects){
		for(int i=0; i<objects.length;i++){
			addElement(objects[i]);
		}
	}
}
