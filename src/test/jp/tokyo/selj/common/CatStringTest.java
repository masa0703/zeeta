package jp.tokyo.selj.common;

import jp.tokyo.selj.common.CatString;
import junit.framework.TestCase;

public class CatStringTest extends TestCase {

	public void testConcatLine01() {
		String[] lines = new String[]{
				"あああ いいい"
		};
		
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "あああ いいい");
	}
	public void testConcatLine02() {
		String[] lines = new String[]{
				"\tあああ いいい"
		};
		
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "\tあああ いいい");
	}
	public void testConcatLine03() {
		String[] lines = new String[]{
				"あああ\t\"いいい\""
		};
		
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "あああ\tいいい");
	}
	public void testConcatLine04() {
		String[] lines = new String[]{
				"あああ\t\t\"いいい\""
		};
		
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "あああ\tいいい");
	}
	public void testConcatLine05() {
		String[] lines = new String[]{
				"\"あああ\" いいい"
		};
		//タイトルが"で囲まれていたらtabがなくても後半はmemoとする
		//このため、区切り文字は、tabに置換される
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "あああ\tいいい");
	}
	public void testConcatLine06() {
		String[] lines = new String[]{
				"\"あああ\"\tいいい"
		};
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "あああ\tいいい");
	}
	public void testConcatLine07() {
		String[] lines = new String[]{
				"\"あああ\"\t  \"いいい\""
		};
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "あああ\tいいい");
	}
	public void testConcatLine08() {
		String[] lines = new String[]{
				"\"あああ\" \"いいい"
		};
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "あああ\tいいい");
	}
	public void testConcatLine09() {
		String[] lines = new String[]{
				"\"あああ\"\t  \"いいい\" \"ううう\""
		};
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "あああ\tいいい\tううう");
	}
	public void testConcatLine10() {
		String[] lines = new String[]{
				"\"あああ\"\t  \"いいい\" \"ううう\"    えええ"
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "あああ\tいいい\tううう\tえええ");
	}
	public void testConcatLine11() {
		String[] lines = new String[]{
				"\"あああ\"\t  \"いいい\" \"ううう\"    えええ おおお"
		};
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "あああ\tいいい\tううう\tえええ おおお");
	}

	//==== 複数行に跨る
	public void testConcatLine20() {

	}
	public void testConcatLine21() {
		String[] lines = new String[]{
				"あああ\t\"い"
				,"い"
				,"い\""
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "あああ\tい\nい\nい");
	}
	public void testConcatLine22() {
		String[] lines = new String[]{
				"\"あ"
				,"あ"
				,"あ\"\tいいい"
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "あ\nあ\nあ\tいいい");
	}
	public void testConcatLine23() {
		String[] lines = new String[]{
				"あああ\t\t\t\t\"い"
				,"い"
				,"い"
				,"うへへへ\""
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "あああ\tい\nい\nい\nうへへへ");
	}
	public void testConcatLine24() {
		String[] lines = new String[]{
				"あああ\t\t\t\t\"い"
				,"----い"
				,"\tい"
				,"うへへへ\""
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "あああ\tい\n----い\n\tい\nうへへへ");
	}
	public void testConcatLine25() {
		String[] lines = new String[]{
				"\"あ"
				,"----あ"
				,"あ\"\tいいい"
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "あ\n----あ\nあ\tいいい");
	}

	//==== "" を"と認識する
	public void testConcatLine40() {
		String[] lines = new String[]{
				"あ\"\"ああ"
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "あ\"ああ");
	}
	public void testConcatLine41() {
		String[] lines = new String[]{
				"あ\"\"ああ\tいい\"\"い"
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "あ\"ああ\tいい\"い");
	}


	//====
	public void testConcatLine90() {
		String[] lines = new String[]{
				"認証・認可\t\"利用者視点"
				,"    * 利用者に付与される固有ID、パスワードによりCWSを自由に編集することが出来る。"
				,"管理者視点\""
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "認証・認可\t利用者視点\n"
				+"    * 利用者に付与される固有ID、パスワードによりCWSを自由に編集することが出来る。\n"
				+"管理者視点"
				);
	}
	public void testConcatLine91() {
		String[] lines = new String[]{
				"あああ\t\"い"
				,"い"
				,"い"	//後ろの"が欠けている
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "あああ\tい\nい\nい");
	}
}
