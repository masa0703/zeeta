package jp.tokyo.selj.model;

public class Util {
	static public String cnvLikeWord(String s){
		s = s.replaceAll("\\\\", "\\\\\\\\");	// \ -> \\
		s = s.replaceAll("\\%", "\\\\%");		// % -> \%
		s = s.replaceAll("\\_", "\\\\_");		// _ -> \_
		return s;
	}
}
