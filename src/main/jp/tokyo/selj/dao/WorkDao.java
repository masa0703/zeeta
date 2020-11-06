 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = Work.class)
public interface WorkDao {

    public List findAll();

    @Arguments( { "docId", "workTypeId" })
    public Work findWork(long docId, int workTypeId);

    @Sql(
    	"SELECT work.docid, work.username, work.newdate, work.versionno, work.worktypeid," 
    	+"	work.outputid, work.pointer, work.compdate, work.compflg, workType.newdate AS newdate_0,"
    	+"	workType.versionno AS versionno_0, workType.worktypeid AS worktypeid_0," 
    	+"	workType.memo AS memo_0, workType.worktypename AS worktypename_0," 
    	+"workType.seq AS seq_0 "
    	+"FROM work "
    	+"	LEFT OUTER JOIN workType workType ON work.workTypeId = workType.workTypeId" 
    	+"	LEFT JOIN output output ON work.outputId = output.outputId "
    	+"WHERE docId = ? and output.outputId=? "
    	+"order by workType.SEQ")
    public Work findWorkByOutputId(long docId, long outputId);

    @Sql(
        	"SELECT work.docid, work.username, work.newdate, work.versionno, work.worktypeid," 
        	+"	work.outputid, work.pointer, work.compdate, work.compflg, workType.newdate AS newdate_0,"
        	+"	workType.versionno AS versionno_0, workType.worktypeid AS worktypeid_0," 
        	+"	workType.memo AS memo_0, workType.worktypename AS worktypename_0," 
        	+"workType.seq AS seq_0 "
        	+"FROM work "
        	+"	LEFT OUTER JOIN workType workType ON work.workTypeId = workType.workTypeId" 
        	+"	LEFT JOIN output output ON work.outputId = output.outputId "
        	+"WHERE output.outputId=? "
        	+"order by workType.SEQ")
    public List<Work> findWorksByOutputId(long outputId);

    @Query( "docId = ? order by workType.SEQ")
    public List<Work> findWorks(long docId);

    public void insert(Work work);

    @Sql("delete from work")
    public void deleteAll();

	public void removeWork(Work work);
	
	public void update(Work work);
}
