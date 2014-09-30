/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> */
package au.com.cybersearch2.classytask;

import java.io.Serializable;

/**
 * WorkTracker
 * WorkStatus accessor
 * @author Andrew Bowley
 * 29/06/2014
 */
public class WorkTracker implements Executable, Serializable
{
    private static final long serialVersionUID = 1573465579866704524L;
    protected volatile WorkStatus workStatus;
    
    /**
     * Create WorkTracker object
     */
    public WorkTracker()
    {
        workStatus = WorkStatus.PENDING;
    }

    /**
     * Returns current work status
     * @see au.com.cybersearch2.classytask.Executable#getStatus()
     */
    @Override
    public WorkStatus getStatus() {
        return workStatus;
    }

    /**
     * Set work status
     * @param status WorkStatus
     */
    public void setStatus(WorkStatus status)
    {
        workStatus = status;
    }
}
