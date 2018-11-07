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

/**
 * Executable
 * Object returned on start of task execution to track status and notify completion
 * @author Andrew Bowley
 * 27/06/2014
 */
public abstract class Executable
{
    /**
     * Returns task status
     * @return WorkStatus
     */
    public abstract WorkStatus getStatus();

    /**
     * Wait for task completion. The caller thread may block
     * if task is executed asynchronously 
     * @throws InterruptedException Should not happen
     */
    public WorkStatus waitForTask() throws InterruptedException
    {
    	WorkStatus status = getStatus();
    	if ((status == WorkStatus.FINISHED) || (status == WorkStatus.FAILED))
    		return status;
        synchronized (this)
        {
            wait();
        }
        return getStatus() == WorkStatus.FINISHED ? WorkStatus.FINISHED : WorkStatus.FAILED;
    }
}
