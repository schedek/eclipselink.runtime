/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.databaseaccess;

import java.sql.*;
import org.eclipse.persistence.internal.helper.*;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.structures.ObjectRelationalDatabaseField;

public class OutputParameterForCallableStatement extends BindCallCustomParameter {
    // this attribute is provided by the caller
    protected boolean isCursor;

    // these attributes are generated by prepare method
    protected int jdbcType;
    protected String typeName;
    protected boolean isTypeNameRequired;

    public OutputParameterForCallableStatement(DatabaseField field) {
        super(field);
    }

    public OutputParameterForCallableStatement(DatabaseField field, AbstractSession session) {
        this(field, session, false);
    }

    public OutputParameterForCallableStatement(DatabaseField field, AbstractSession session, boolean isCursor) {
        this(field);
        this.isCursor = isCursor;
        prepare(session);
    }

    public OutputParameterForCallableStatement(OutputParameterForCallableStatement outParameter) {
        super(outParameter.obj);
        this.isCursor = outParameter.isCursor;
        this.jdbcType = outParameter.jdbcType;
        this.typeName = outParameter.typeName;
        this.isTypeNameRequired = outParameter.isTypeNameRequired;
    }

    protected OutputParameterForCallableStatement() {
    }

    public void setIsCursor(boolean isCursor) {
        this.isCursor = isCursor;
    }

    // make sure to call prepare() method after this
    public boolean isCursor() {
        return isCursor;
    }

    public boolean isTypeNameRequired() {
        return isTypeNameRequired;
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public String getTypeName() {
        return typeName;
    }

    public DatabaseField getOutputField() {
        return (DatabaseField)obj;
    }

    public void prepare(AbstractSession session) {
        DatabasePlatform dbplatform = session.getPlatform();
        if (isCursor()) {
            jdbcType = dbplatform.getCursorCode();// Oracle code for cursors
        } else {
            jdbcType = dbplatform.getJDBCType(getOutputField());
            if (obj instanceof ObjectRelationalDatabaseField) {
                isTypeNameRequired = true;
                typeName = ((ObjectRelationalDatabaseField)obj).getSqlTypeName();
            } else {
                isTypeNameRequired = dbplatform.requiresTypeNameToRegisterOutputParameter();
                if (isTypeNameRequired) {
                    typeName = dbplatform.getJdbcTypeName(jdbcType);
                }
            }
        }
    }

    public void set(DatabasePlatform platform, PreparedStatement statement, int index, AbstractSession session) throws SQLException {
        if (isTypeNameRequired) {
            ((CallableStatement)statement).registerOutParameter(index, jdbcType, typeName);
        } else {
            ((CallableStatement)statement).registerOutParameter(index, jdbcType);
        }
    }

    public String toString() {
        return "=> " + getOutputField().getNameDelimited();
    }
}
