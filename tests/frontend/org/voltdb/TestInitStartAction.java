/* This file is part of VoltDB.
 * Copyright (C) 2008-2017 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.voltdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.voltdb.VoltDB.Configuration;
import org.voltdb.VoltDB.SimulatedExitException;
import org.voltdb.catalog.Catalog;
import org.voltdb.compiler.VoltCompiler;
import org.voltdb.compiler.VoltProjectBuilder;
import org.voltdb.utils.CatalogUtil;
import org.voltdb.utils.InMemoryJarfile;
import org.voltdb.utils.VoltFile;

import com.google_voltpatches.common.base.Joiner;

final public class TestInitStartAction {

    static File rootDH;
    static File cmdlogDH;

    private static final String[] deploymentXML = {
            "<?xml version=\"1.0\"?>",
            "<deployment>",
            "    <cluster hostcount=\"1\"/>",
            "    <paths>",
            "        <voltdbroot path=\"_VOLTDBROOT_PATH_\"/>",
            "        <commandlog path=\"_COMMANDLOG_PATH_\"/>",
            "    </paths>",
            "    <httpd enabled=\"true\">",
            "        <jsonapi enabled=\"true\"/>",
            "    </httpd>",
            "    <commandlog enabled=\"false\"/>",
            "</deployment>"
        };

    static final Pattern voltdbrootRE = Pattern.compile("_VOLTDBROOT_PATH_");
    static final Pattern commandlogRE = Pattern.compile("_COMMANDLOG_PATH_");
    static File legacyDeploymentFH;

    @ClassRule
    static public final TemporaryFolder tmp = new TemporaryFolder();

    @BeforeClass
    public static void setupClass() throws Exception {
        rootDH = tmp.newFolder();
        cmdlogDH = new File(tmp.newFolder(), "commandlog");

        legacyDeploymentFH = new File(rootDH, "deployment.xml");
        try (FileWriter fw = new FileWriter(legacyDeploymentFH)) {
            Matcher mtc = voltdbrootRE.matcher(Joiner.on('\n').join(deploymentXML));
            String expnd = mtc.replaceAll(new File(rootDH, "voltdbroot").getPath());

            mtc = commandlogRE.matcher(expnd);
            expnd = mtc.replaceAll(cmdlogDH.getPath());

            fw.write(expnd);
        }
        System.setProperty("VOLT_JUSTATEST", "YESYESYES");
        VoltDB.ignoreCrash = true;
    }

    AtomicReference<Throwable> serverException = new AtomicReference<>(null);

    final Thread.UncaughtExceptionHandler handleUncaught = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            serverException.compareAndSet(null, e);
        }
    };

    /** Verifies that the VoltDB exit 'crash' was a simulated exit with the specified exit code.
     * @param exitCode Expected exit code from VoltDB
     */
    private void expectSimulatedExit(int exitCode){
        assertNotNull(serverException.get());
        if (!(serverException.get() instanceof VoltDB.SimulatedExitException)) {
            System.err.println("got an unexpected exception");
            serverException.get().printStackTrace(System.err);
            if (VoltDB.wasCrashCalled) {
                System.err.println("Crash message is:\n  "+ VoltDB.crashMessage);
            }
        }

        assertTrue(serverException.get() instanceof VoltDB.SimulatedExitException);
        VoltDB.SimulatedExitException exitex = (VoltDB.SimulatedExitException)serverException.get();
        assertEquals(exitCode, exitex.getStatus());
    }

    /** Clears recorded crash (or simulated exit) in preparation for another test.
     */
    private void clearCrash(){
        VoltDB.wasCrashCalled = false;
        VoltDB.crashMessage = null;
        serverException.set(null);
    }


    @Test
    public void testInitStartAction() throws Exception {

        File deplFH = new VoltFile(new VoltFile(new VoltFile(rootDH, "voltdbroot"), "config"), "deployment.xml");
        Configuration c1 = new Configuration(
                new String[]{"initialize", "voltdbroot", rootDH.getPath(), "deployment", legacyDeploymentFH.getPath()});
        ServerThread server = new ServerThread(c1);
        server.setUncaughtExceptionHandler(handleUncaught);
        c1.m_forceVoltdbCreate = false;

        server.start();
        server.join();
        expectSimulatedExit(0);

        assertTrue(deplFH.exists() && deplFH.isFile() && deplFH.canRead());

        if (c1.m_isEnterprise) {
            assertTrue(cmdlogDH.exists()
                    && cmdlogDH.isDirectory()
                    && cmdlogDH.canRead()
                    && cmdlogDH.canWrite()
                    && cmdlogDH.canExecute());

            for (int i=0; i<10; ++i) {
                new FileOutputStream(new File(cmdlogDH, String.format("dummy-%02d.log", i))).close();
            }
            assertEquals(10, cmdlogDH.list().length);
        }

        serverException.set(null);
        // server thread sets m_forceVoltdbCreate to true by default
        c1 = new Configuration(
                new String[]{"initialize", "voltdbroot", rootDH.getPath(), "force", "deployment", legacyDeploymentFH.getPath()});
        assertTrue(c1.m_forceVoltdbCreate);
        server = new ServerThread(c1);
        server.setUncaughtExceptionHandler(handleUncaught);

        server.start();
        server.join();

        expectSimulatedExit(0);

        assertTrue(deplFH.exists() && deplFH.isFile() && deplFH.canRead());
        if (c1.m_isEnterprise) {
            assertTrue(cmdlogDH.exists()
                    && cmdlogDH.isDirectory()
                    && cmdlogDH.canRead()
                    && cmdlogDH.canWrite()
                    && cmdlogDH.canExecute());
            assertEquals(0, cmdlogDH.list().length);
        }

        try {
            c1 = new Configuration(new String[]{"initialize", "voltdbroot", rootDH.getPath()});
            fail("did not detect prexisting initialization");
        } catch (VoltDB.SimulatedExitException e) {
            assertEquals(-1, e.getStatus());
        }

        VoltDB.wasCrashCalled = false;
        VoltDB.crashMessage = null;
        serverException.set(null);

        c1 = new Configuration(new String[]{"create", "deployment", legacyDeploymentFH.getPath(), "host", "localhost"});
        server = new ServerThread(c1);
        server.setUncaughtExceptionHandler(handleUncaught);

        server.start();
        server.join();

        assertNotNull(serverException.get());
        assertTrue(serverException.get() instanceof AssertionError);
        assertTrue(VoltDB.wasCrashCalled);
        assertTrue(VoltDB.crashMessage.contains("Cannot use legacy start action"));

        if (!c1.m_isEnterprise) return;

        clearCrash();

        c1 = new Configuration(new String[]{"recover", "deployment", legacyDeploymentFH.getPath(), "host", "localhost"});
        server = new ServerThread(c1);
        server.setUncaughtExceptionHandler(handleUncaught);

        server.start();
        server.join();

        assertNotNull(serverException.get());
        assertTrue(serverException.get() instanceof AssertionError);
        assertTrue(VoltDB.wasCrashCalled);
        assertTrue(VoltDB.crashMessage.contains("Cannot use legacy start action"));

        // this test which action should be considered legacy
        EnumSet<StartAction> legacyOnes = EnumSet.complementOf(EnumSet.of(StartAction.INITIALIZE,StartAction.PROBE, StartAction.GET));
        assertTrue(legacyOnes.stream().allMatch(StartAction::isLegacy));
    }


    /*
     * Tests:
     * 1.  Positive test with valid schema that requires no procedures
     * 2a. Positive test with valid schema and procedures that are in CLASSPATH
     * 2b. Negative test with valid files but not "init --force"
     * 3.  Negative test with a bad schema
     * 4.  Negative test with procedures missing
     *
     * CAVEAT: Until ENG-11953 is complete, the files will be installed but not honored.
     * The only verification performed during init are sanity checks.
     * More comprehensive checks will be added during 'start', since 'init' has no way to verify that the entire cluster has the same schema.
     *
     * Note that SimulatedExitException is thrown by the command line parser with no descriptive details.
     * VoltDB.crashLocalVoltDB() throws an AssertionError with the message "Faux crash of VoltDB successful."
     */

    /** Verifies that the staged catalog matches what VoltCompiler emits given the supplied schema.
     * @param schema Schema used to generate the staged catalog
     * @throws Exception upon test failure or error (unable to write temp file for example)
     */
    private void validateStagedCatalog(String schema) throws Exception {
        // setup reference point for the supplied schema
        File schemaFile = VoltProjectBuilder.writeStringToTempFile(schema);
        schemaFile.deleteOnExit();
        File referenceFile = File.createTempFile("reference", ".jar");
        referenceFile.deleteOnExit();
        VoltCompiler compiler = new VoltCompiler(false);
        final boolean success = compiler.compileFromDDL(referenceFile.getAbsolutePath(), schemaFile.getPath());
        assertEquals(true, success);
        InMemoryJarfile referenceCatalogJar = new InMemoryJarfile(referenceFile);
        Catalog referenceCatalog = new Catalog();
        referenceCatalog.execute(CatalogUtil.getSerializedCatalogStringFromJar(referenceCatalogJar));

        // verify that the staged catalog is identical
        File stagedJarFile = new VoltFile(RealVoltDB.getStagedCatalogPath(rootDH.getPath() + File.separator + "voltdbroot"));
        assertEquals(true, stagedJarFile.isFile());
        InMemoryJarfile stagedCatalogJar = new InMemoryJarfile(stagedJarFile);
        Catalog stagedCatalog = new Catalog();
        stagedCatalog.execute(CatalogUtil.getSerializedCatalogStringFromJar(stagedCatalogJar));

        assertEquals(true, referenceCatalog.equals(stagedCatalog));
        assertEquals(true, stagedCatalog.equals(referenceCatalog));

        assertEquals(true, referenceFile.delete());
        assertEquals(true, schemaFile.delete());
    }

    /** Test that a valid schema with no procedures can be used to stage a matching catalog.
     * @throws Exception upon failure or error
     */
    @Test
    public void testInitWithSchemaValidNoProcedures() throws Exception {

        final String schema =
                "create table books (cash integer default 23 not null, title varchar(3) default 'foo', PRIMARY KEY(cash));" +
                "create procedure Foo as select * from books;\n" +
                "PARTITION TABLE books ON COLUMN cash;";
        File schemaFile = VoltProjectBuilder.writeStringToTempFile(schema);

        Configuration c1 = new Configuration(
                new String[]{"initialize", "voltdbroot", rootDH.getPath(), "force", "schema", schemaFile.getPath()});
        ServerThread server = new ServerThread(c1);
        server.setUncaughtExceptionHandler(handleUncaught);
        server.start();
        server.join();
        expectSimulatedExit(0);
        validateStagedCatalog(schema);
        assertEquals(true, schemaFile.delete());
    }

    /** Test that a valid schema with procedures can be used to stage a matching catalog,
     * but running a second time without 'init --force' fails due to existing artifacts.
     * @throws Exception upon failure or error
     */
    @Test
    public void testInitWithSchemaValidWithProcedures() throws Exception {

        String schema =
                "create table books" +
                " (cash integer default 23 not null," +
                " title varchar(3) default 'foo'," +
                " PRIMARY KEY(cash));" +
                "PARTITION TABLE books ON COLUMN cash;" +
                "CREATE PROCEDURE FROM CLASS org.voltdb.compiler.procedures.AddBook;";
        File schemaFile = VoltProjectBuilder.writeStringToTempFile(schema);
        {
            // valid use case
            Configuration c1 = new Configuration(
                    new String[]{"initialize", "force", "voltdbroot", rootDH.getPath(), "schema", schemaFile.getPath()});
            ServerThread server = new ServerThread(c1);
            server.setUncaughtExceptionHandler(handleUncaught);
            server.start();
            server.join();
            expectSimulatedExit(0);
            validateStagedCatalog(schema);
            clearCrash();
        }
        try {
            // second attempt is not valid due to existing artifacts
            new Configuration(
                    new String[]{"initialize", "voltdbroot", rootDH.getPath(), "schema", schemaFile.getPath()});
        } catch (SimulatedExitException e){
            assertEquals(e.getStatus(), -1);
        }
        assertEquals(true, schemaFile.delete());
    }

    /** Test that a valid schema with no procedures can be used to stage a matching catalog.
     * @throws Exception upon failure or error
     */
    @Test
    public void testInitWithSchemaInvalidJunkSchema() throws Exception {

        File schemaFile = Files.createTempDirectory("junk").toFile();
        try {
            new Configuration(
                    new String[]{"initialize", "voltdbroot", rootDH.getPath(), "force", "schema", schemaFile.getPath()});
            fail("did not detect unusable schema file");
        } catch (VoltDB.SimulatedExitException e) {
            assertEquals(e.getStatus(), -1);
        }

        assertEquals(true, schemaFile.delete());
    }

    /** Test that a valid schema with no procedures can be used to stage a matching catalog.
     * @throws Exception upon failure or error
     */
    @Test
    public void testInitWithSchemaInvalidMissingClass() throws Exception {

        String schema =
                "CREATE TABLE unicorns" +
                " (horn_size integer DEFAULT 12 NOT NULL," +
                " name varchar(32) DEFAULT 'Pixie' NOT NULL," +
                " PRIMARY KEY(name));" +
                "PARTITION TABLE unicorns ON COLUMN name;" +
                "CREATE PROCEDURE FROM CLASS org.voltdb.unicorns.ComputeSocialStanding;";
        File schemaFile = VoltProjectBuilder.writeStringToTempFile(schema);

        Configuration c1 = new Configuration(
                    new String[]{"initialize", "voltdbroot", rootDH.getPath(), "force", "schema", schemaFile.getPath()});
        ServerThread server = new ServerThread(c1);
        server.setUncaughtExceptionHandler(handleUncaught);
        server.start();
        server.join();

        assertNotNull(serverException.get());
        assertTrue(serverException.get().getMessage().equals("Faux crash of VoltDB successful."));
        assertTrue(VoltDB.wasCrashCalled);
        assertTrue(VoltDB.crashMessage.contains("Could not compile specified schema"));
        assertEquals(true, schemaFile.delete());
    }
}
