 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.NoPersistentProperty;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = Output.class)
public interface OutputDao {

    @Query ("order by outputType.SEQ")
    public List<Output> findAll();
    
//    @Arguments ({"outputId"})
    @Sql(
    		"SELECT "
    		+"output.path, output.username, output.newdate, output.versionno"
    		+", output.outputid, output.memo, output.outputtypeid, output.name"
    		+", outputType.newdate AS newdate_0, outputType.versionno AS versionno_0"
    		+", outputType.worktypeid AS worktypeid_0, outputType.memo AS memo_0"
    		+", outputType.outputtypeid AS outputtypeid_0, outputType.seq AS seq_0"
    		+", outputType.outputtypename AS outputtypename_0"
    		+",w.workCount"
    		+" FROM output "
    		+"  LEFT OUTER JOIN outputType outputType ON output.outputTypeId = outputType.outputTypeId "
    		+"  LEFT OUTER JOIN (select count(*) as workCount, outputid from work group by outputid) w ON output.outputId=w.outputId"
    		+" WHERE output.outputid=? order by outputId, outputType.SEQ"
    		)
    public Output findById(long outputId);

    @Arguments ({"name"})
    public List<Output> findByName(String name);

//    @Arguments ({"outputTypeId"})
    @Query ("outputTypeId=? order by outputId, outputType.SEQ")
    public List<Output> findByType(int outputTypeId);

    @NoPersistentProperty("workCount")
    public void insert(Output output);
    @NoPersistentProperty("workCount")
	public void update(Output output);

    @Sql("delete from output")
    public void deleteAll();


	public void delete(Output output);

//    @Query ("outputType.workTypeId=? order by outputId, outputType.SEQ")
    @Sql(
    		"SELECT "
    		+"output.path, output.username, output.newdate, output.versionno"
    		+", output.outputid, output.memo, output.outputtypeid, output.name"
    		+", outputType.newdate AS newdate_0, outputType.versionno AS versionno_0"
    		+", outputType.worktypeid AS worktypeid_0, outputType.memo AS memo_0"
    		+", outputType.outputtypeid AS outputtypeid_0, outputType.seq AS seq_0"
    		+", outputType.outputtypename AS outputtypename_0"
    		+",w.workCount"
    		+" FROM output "
    		+"  LEFT OUTER JOIN outputType outputType ON output.outputTypeId = outputType.outputTypeId "
    		+"  LEFT OUTER JOIN (select count(*) as workCount, outputid from work group by outputid) w ON output.outputId=w.outputId"
    		+" WHERE outputType.workTypeId=? order by outputId, outputType.SEQ"
    		)
	public List<Output> findByWorkTypeId(int workTypeId);
    @Sql(
    		"SELECT "
    		+"output.path, output.username, output.newdate, output.versionno"
    		+", output.outputid, output.memo, output.outputtypeid, output.name"
    		+", outputType.newdate AS newdate_0, outputType.versionno AS versionno_0"
    		+", outputType.worktypeid AS worktypeid_0, outputType.memo AS memo_0"
    		+", outputType.outputtypeid AS outputtypeid_0, outputType.seq AS seq_0"
    		+", outputType.outputtypename AS outputtypename_0"
    		+",w.workCount"
    		+" FROM output "
    		+"  LEFT OUTER JOIN outputType outputType ON output.outputTypeId = outputType.outputTypeId "
    		+"  LEFT OUTER JOIN (select count(*) as workCount, outputid from work group by outputid) w ON output.outputId=w.outputId"
    		+" WHERE LOWER(output.name) like LOWER(?) or LOWER(output.path) like LOWER(?) or LOWER(output.username) like LOWER(?) " 
    		+	"or LOWER(output.memo) like LOWER(?)  "
    		+" order by outputId, outputType.SEQ"
    		)
	public List<Output> findByNameOrPathOrCreator(String word);
    @Sql(
    		"SELECT "
    		+"output.path, output.username, output.newdate, output.versionno"
    		+", output.outputid, output.memo, output.outputtypeid, output.name"
    		+", outputType.newdate AS newdate_0, outputType.versionno AS versionno_0"
    		+", outputType.worktypeid AS worktypeid_0, outputType.memo AS memo_0"
    		+", outputType.outputtypeid AS outputtypeid_0, outputType.seq AS seq_0"
    		+", outputType.outputtypename AS outputtypename_0"
    		+",w.workCount"
    		+" FROM output "
    		+"  LEFT OUTER JOIN outputType outputType ON output.outputTypeId = outputType.outputTypeId "
    		+"  LEFT OUTER JOIN (select count(*) as workCount, outputid from work group by outputid) w ON output.outputId=w.outputId"
    		+" WHERE outputType.workTypeId=/*p.workTypeId*/ AND (LOWER(output.name) like LOWER(/*p.word*/) or"
    		+" LOWER(output.path) like LOWER(/*p.word*/) or LOWER(output.username) like LOWER(/*p.word*/)) or" 
    		+" LOWER(output.memo) like LOWER(/*p.word*/)  "
    		+" order by outputId, outputType.SEQ"
    		)
	public List<Output> findByWorkTypeAndNameOrPathOrCreator(Param01 p);

    public static class Param01{
    	int workTypeId;
    	String word;
		public String getWord() {
			return word;
		}
		public void setWord(String word) {
			this.word = word;
		}
		public int getWorkTypeId() {
			return workTypeId;
		}
		public void setWorkTypeId(int workTypeId) {
			this.workTypeId = workTypeId;
		}
    }
}
