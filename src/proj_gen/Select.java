package ##package_prefix##.procedures;

import org.voltdb.*;

/** A VoltDB stored procedure is a Java class defining one or
 * more SQL statements and implementing a <code>public
 * VoltTable[] run</code> method. VoltDB requires a
 * <code>ProcInfo</code> annotation providing metadata for the
 * procedure.  The <code>run</code> method is
 * defined to accept one or more parameters. These parameters take the
 * values the client passes via the
 * <code>VoltClient.callProcedure</code> invocation.
 *
 * <a
 * href="https://hzproject.com/svn/repos/doc/trunk/Stored%20Procedure%20API.docx">Stored
 * Procedure API</a> specifies valid stored procedure definitions,
 * including valid run method parameter types, required annotation
 * metadata, and correct use the Volt query interface.
*/
@ProcInfo(
    partitionInfo = "##upper_project_name##.##upper_project_name##_ID: 0",
    singlePartition = true
)
public class Select extends VoltProcedure {

    public final SQLStmt selectItem =
      new SQLStmt("SELECT ##upper_project_name##_ID,  ##upper_project_name##_ITEM " +
                  "FROM ##upper_project_name## WHERE  ##upper_project_name##_ID = ?");

    public VoltTable[] run( long ##upper_project_name##_ID ) throws VoltAbortException {
        // Add a SQL statement to the current execution queue. Queries
        // and DMLs may not be mixed in one batch.
        voltQueueSQL( selectItem, ##upper_project_name##_ID );

        // Run all queued queries.
        return voltExecuteSQL();
    }
}
