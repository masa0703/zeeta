package jp.tokyo.selj.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;


public class ClipboardStringWriter extends Writer implements ClipboardOwner{
	Logger log = Logger.getLogger(this.getClass());  //  @jve:decl-index=0:
	StringBuffer buf_;
	public ClipboardStringWriter(){
		buf_ = new StringBuffer();
	}
	@Override
	public void close() throws IOException {
		String data = buf_.toString();
		log.debug(data);
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		cb.setContents(
				new MyTransferable(data)
				, this);
	}
	@Override
	public void flush() throws IOException {
		//doNothing
	}
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		throw new RuntimeException("unsupported!");
	}
	@Override
	public void write(String str) throws IOException {
		buf_.append(str);
	}
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		log.debug("よばれたー");
		buf_ = null;
	}
}

class MyTransferable implements Transferable{
	Logger log = Logger.getLogger(this.getClass());  //  @jve:decl-index=0:

	String data_;
    DataFlavor[] localFlavors = null;
    
	public MyTransferable(String data){
		data_ = data;
		try {
			localFlavors = new DataFlavor[]
				{	new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
			                            ";class=java.lang.String")
					, DataFlavor.stringFlavor
				};
		} catch (ClassNotFoundException e) {
			log.error(e);
		}
	}
	public Object getTransferData(DataFlavor flavor) 
			throws UnsupportedFlavorException, IOException {
		//1種類しかサポートしてないのでflavorの判定はいらんだろう
		return data_;
	}
	public DataFlavor[] getTransferDataFlavors() {
		log.debug("よばれたし");
		return localFlavors;
	}
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return false;
	}
}
