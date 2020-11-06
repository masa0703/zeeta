package jp.tokyo.selj.common;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFormattedTextField;

public class DateTextFormatter extends JFormattedTextField.AbstractFormatter{
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	@Override
	public Object stringToValue(String text) throws ParseException {
		return new Timestamp(formatter.parse(text).getTime());
	}
	@Override
	public String valueToString(Object value) throws ParseException {
		if(value != null){
			Date date = (Date)value;
			return formatter.format(date);
		}else{
			return "0000/00/00 00:00";
		}
	}
	
	// Helper
	public static String dateToString(Date date){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		return formatter.format(date);
	}
}
