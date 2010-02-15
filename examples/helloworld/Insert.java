import org.voltdb.*;

@ProcInfo(
    partitionInfo = "HELLOWORLD.DIALECT: 0",
    singlePartition = true
)

public class Insert extends VoltProcedure {

  public final SQLStmt sql = new SQLStmt(
      "INSERT INTO HELLOWORLD VALUES (?, ?, ?);"
  );

  public VoltTable[] run( String hello, 
                          String world, 
                          String language) 
      throws VoltAbortException {
          voltQueueSQL( sql, hello, world, language );
          VoltTable[] retval = voltExecuteSQL();
          return null;
      }
}
