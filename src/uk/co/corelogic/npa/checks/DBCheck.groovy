/*
 * A generic Database Check class, to replace OracleCheck and SSCheck.  Emphasis is instead placed on being able to
 * run custom SQL and compare results
 */
package uk.co.corelogic.npa.checks

import groovy.sql.Sql
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.Log
import uk.co.corelogic.npa.gatherers.*

/**
 *
 * @author Chris Gilbert
 */
class DBCheck {

String conn
String result
String host
String port
String database
String user
String password


public init(variables1) {
    this.variables = variables1
    this.host = variables1.host
    this.port = variables1.port
    this.database = variables1.database
    this.user = variables1.user
    this.password = variables1.password

    // Check for nulls
    assert this.host != null, 'Host cannot be null!'
    assert this.port != null, 'Port cannot be null!'
    assert this.database != null, 'Database cannot be null!'
    assert this.user  != null, 'Username cannot be null!'
    assert this.password != null, 'Password cannot be null!'

    this.initiatorID  = UUID.randomUUID();
    variables1.initiatorID = this.initiatorID
    try {
        this.gatherer = new DBGatherer(variables1)
    } catch(e) {
        Log.error("An error occurred when creating the gatherer:", e)
        CheckResultsQueue.add(super.generateResult(this.initiatorID, variables1.nagiosServiceName, this.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a DB connection!"))
        Log.error("Throwing error up chain")
        throw e
    }
    Log.debug("Gatherer initiator ID is ${this.initiatorID}")
}
    synchronized public DBCheck clone() {
        DBCheck clone = (DBCheck) super.makeClone(this.chk_name);
        clone.conn = this.conn;
        clone.result = this.result;
        clone.host = this.host;
        clone.port = this.port;
        clone.database = this.database;
        clone.user = this.user;
        clone.password = this.password;
        clone.instance = this.database;
        return clone;
    }

    DBCheck(args) {
        init(args)
    }
    DBCheck() {
        super()
    }

    public reinit() {
                try {
                    this.gatherer = new DBGatherer(this.variables)
                } catch(e) {
                    Log.error("An error occurred when creating the gatherer:", e)
                    CheckResultsQueue.add(super.generateResult(this.initiatorID, this.variables.nagiosServiceName, this.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a DB connection!"))
                    Log.error("Throwing error up chain")
                    throw e
                }
    }

	
}

