package org.fogbowcloud.saps.engine.scheduler.restlet;

public class TestRestletServerMain {

    public static void main(String[] args) throws Exception {
        TestDatabaseApplication databaseApplication = new TestDatabaseApplication();
        databaseApplication.startServer();
    }
}
