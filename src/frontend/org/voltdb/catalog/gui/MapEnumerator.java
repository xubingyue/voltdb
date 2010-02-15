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

package org.voltdb.catalog.gui;

import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.tree.TreeNode;

public class MapEnumerator implements Enumeration<TreeNode> {

    Iterator<TreeNode> m_iterator;

    MapEnumerator(Iterator<TreeNode> iterator) {
        m_iterator = iterator;
    }

    @Override
    public boolean hasMoreElements() {
        return m_iterator.hasNext();
    }

    @Override
    public TreeNode nextElement() {
        return m_iterator.next();
    }

}
