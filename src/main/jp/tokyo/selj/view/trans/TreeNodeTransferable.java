package jp.tokyo.selj.view.trans;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

import javax.swing.JTree;

import org.apache.log4j.Logger;

import jp.tokyo.selj.model.DocNode;

public class TreeNodeTransferable implements Transferable {
	Logger log = Logger.getLogger(this.getClass());

	DataFlavor[] dataFlavors_;
	
	public static class Data implements Serializable{
		DocNode node_;
		String ownerType_;
		Data(String ownerType, DocNode node){
			ownerType_ = ownerType;
			node_ = node;
		}
		public DocNode getNode() {
			return node_;
		}
		public String getOwnerType() {
			return ownerType_;
		}
	}
	Data data_;
	
    public TreeNodeTransferable(String ownerType, DocNode node, DataFlavor[] dataFlavors) {
    	log.trace("start");
    	dataFlavors_ = dataFlavors;
    	data_ = new Data(ownerType, node);
    	log.trace("end");
    }

	public Object getTransferData(DataFlavor flavor)
                             throws UnsupportedFlavorException {
    	log.trace("start");
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
    	log.trace("end");
        if(DataFlavor.stringFlavor.equals(flavor)){
        	return data_.getNode().toString();
        }else{
        	return data_;
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
    	log.trace("start/end");
        return dataFlavors_;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
    	log.trace("start");
    	log.debug("flavor="+flavor);
    	for(DataFlavor dataFlavor : dataFlavors_){
	        if (dataFlavor.equals(flavor)) {
	            return true;
	        }
    	}
    	log.trace("end");
        return false;
    }
}
