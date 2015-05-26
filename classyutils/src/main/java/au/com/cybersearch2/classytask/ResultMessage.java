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
 * ResultMessage
 * Dispatch object for inter-thread result communications
 * @author Andrew Bowley
 * 04/09/2014
 */
public class ResultMessage<Result> 
{
    protected final Result result;
    protected final WorkerTask<Result> task;

    /**
     * Create ResultMessage object
     * @param task The target of the message
     * @param result Object of generic type "Result" - maybe null in cancel case
     */
    public ResultMessage(WorkerTask<Result> task, Result result)
    {
        this.task = task;
        this.result = result;
    }

    /**
     * Action result - to be executed in caller thread, not background thread
     */
    public void action()
    {
        task.finish(result);
    }
    
    /**
     * Cancel result - to be executed in caller thread, not background thread
     */
    public void cancel()
    {
        task.onCancelled(result);
    }
}