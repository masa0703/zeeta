package jp.tokyo.selj.common;

import java.text.ParseException;

import javax.swing.JFormattedTextField;

public class NumericTextFormatter extends JFormattedTextField.AbstractFormatter{
	static final Integer DEF_VALUE = new Integer(0);
	
	@Override
	public Object stringToValue(String text) throws ParseException {
		try{
			return new Integer(text);
		}catch(NumberFormatException e){
			return DEF_VALUE;
		}
	}
	@Override
	public String valueToString(Object value) throws ParseException {
		if(value != null){
			return ((Number)value).toString();
		}else{
			return "0";
		}
	}
}
