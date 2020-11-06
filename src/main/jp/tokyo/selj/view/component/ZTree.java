package jp.tokyo.selj.view.component;

import java.awt.event.MouseEvent;
import java.util.UUID;

import javax.swing.JTree;

public class ZTree extends JTree {
	//�c���[�̎��
	String treeType_ = "";
	
	boolean needShiftForMoveNode_ = true;	//Tree�̃h���b�OMove��shiftKey�������Ȃ���΂Ȃ�Ȃ��ꍇ��true
	public enum DnDStatus {None, Move, Copy};
	DnDStatus dndStatus_ = DnDStatus.None;

	boolean canPasteNodeFromAnotherProcess_ = false;
	
	public ZTree(){
		super();
		
	}
	public String getTreeType(){
		return treeType_;
	}
	public void setTreeType(String treeType){
		treeType_ = treeType;
	}
	public boolean canPasteNodeFromAnotherProcess() {
		return canPasteNodeFromAnotherProcess_;
	}
	public void setCanPasteNodeFromAnotherProcess(
			boolean canPasteNodeFromAnotherProcess) {
		canPasteNodeFromAnotherProcess_ = canPasteNodeFromAnotherProcess;
	}
	public void setNeedShiftForMoveNode(boolean smartDnD){
		needShiftForMoveNode_ = smartDnD;
	}
	public DnDStatus getDnDStatus(){
		return dndStatus_;
	}
	
	protected void processMouseMotionEvent(MouseEvent e){
		if(e.getID() == MouseEvent.MOUSE_DRAGGED){ 
			dndStatus_ =DnDStatus.None;
			if( (!e.isShiftDown() && needShiftForMoveNode_) && !e.isControlDown()){
				//shift��ctrl�L�[��������Ă��Ȃ�������h���b�O�͖���
				return;
			}else if( e.isShiftDown() && e.isControlDown()){
				//����������Ă��Ă�����
				return;
			}
			if( e.isShiftDown() || !needShiftForMoveNode_){
				dndStatus_ =DnDStatus.Move;
			}
			if( e.isControlDown()){
				dndStatus_ =DnDStatus.Copy;
			}
		}
		super.processMouseMotionEvent(e);
	}

}
