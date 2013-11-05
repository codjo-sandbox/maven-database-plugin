/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database;
import net.codjo.database.common.api.structure.SqlTable;
import org.apache.maven.plugin.Mojo;

/**
 *
 */
public class DropMojoTest extends DatabaseMojoTestCase {
    public void test_dropTables() throws Exception {
        Mojo mojo = lookupMojo("drop", "drop/pom-default.xml");

        fixture.create(SqlTable.table("MY_TABLE_1"), "MY_FIELD_1 numeric null");
        fixture.create(SqlTable.table("MY_TABLE_2"), "MY_FIELD_1 numeric null");

        mojo.execute();

        fixture.advanced().assertDoesntExist("MY_TABLE_1");
        fixture.advanced().assertDoesntExist("MY_TABLE_2");
    }
}
