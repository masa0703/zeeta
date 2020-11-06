package jp.tokyo.selj.common;

import java.math.BigDecimal;
import java.text.ParseException;

import javax.swing.JFormattedTextField;

public class BigDecimalTextFormatter extends JFormattedTextField.AbstractFormatter{
	static final BigDecimal DEF_VALUE = new BigDecimal(0);
	int scale_ = 0;
	
	public BigDecimalTextFormatter(int scale){
		scale_= scale;
	}
	
	@Override
	public Object stringToValue(String text) throws ParseException {
		try{
			BigDecimal ret= new BigDecimal(0);
			ret.setScale(scale_);
			ret = ret.add(new BigDecimal(text));
			return ret;
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
