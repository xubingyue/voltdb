/* This file is part of VoltDB.
 * Copyright (C) 2008-2010 VoltDB L.L.C.
 *
 * VoltDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VoltDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.voltdb.planner;

import java.io.Serializable;

public class AdHocPlannerWork implements Serializable {
    private static final long serialVersionUID = 3297865891152100922L;

    boolean shouldShutdown = false;
    boolean shouldDump = false;
    long clientHandle = -1;
    String sql = null;
    int connectionId = -1;
    int sequenceNumber = -1;
    transient public Object clientData = null;

    @Override
    public String toString() {
        String retval = "shouldShutdown:" + String.valueOf(shouldShutdown) + ", ";
        retval += "clientHandle:" + String.valueOf(clientHandle) + ", ";
        retval += "connectionId:" + String.valueOf(connectionId) + ", ";
        retval += "sequenceNumber:" + String.valueOf(sequenceNumber) + ", ";
        retval += "\n  sql: " + ((sql != null) ? sql : "null");
        return retval;
    }
}
