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
package au.com.cybersearch2.classyjpa.entity;

import android.os.Message;
import au.com.cybersearch2.classytask.InternalHandler;
import au.com.cybersearch2.classytask.ResultMessage;
import au.com.cybersearch2.classytask.UserTaskContext;

/**
 * UserTaskImpl
 * Executes persistence work in background thread, invoked from User thread
 * @author Andrew Bowley
 * 04/09/2014
 */
public class UserTaskImpl extends TaskBase
{
    /** Message handler for posting messages from background thread to User thread */
    protected UserTaskContext userTaskContext;
    /** Persistence container in which to execute persistence work */
    protected PersistenceContainer persistenceContainer;

    /**
     * Create UserTaskImpl object
     * @param persistenceContainer Persistence container in which to execute persistence work
     * @param persistenceWork Work to be performed
     */
    public UserTaskImpl(PersistenceContainer persistenceContainer, PersistenceWork persistenceWork)
    {
        super(persistenceContainer.getPersistenceTask(persistenceWork));
        this.persistenceContainer = persistenceContainer;
        userTaskContext = new UserTaskContext();
    }

    /**
     * Post result to calling thread on background thread completion
     * @see au.com.cybersearch2.classytask.WorkerTask#sendResult(java.lang.Object)
     */
    @Override
    public void sendResult(Boolean result)
    {
        Message message = userTaskContext.getInternalHandler().obtainMessage(InternalHandler.MESSAGE_POST_RESULT,
                new ResultMessage<Boolean>(this, result));
        message.sendToTarget();
    }

    /**
     * Post result to calling thread on background thread cancellation
     * @see au.com.cybersearch2.classytask.WorkerTask#sendCancel(java.lang.Object)
     */
    @Override
    public void sendCancel(Boolean result)
    {
        Message message = userTaskContext.getInternalHandler().obtainMessage(InternalHandler.MESSAGE_POST_CANCEL,
                new ResultMessage<Boolean>(this, result));
        message.sendToTarget();
    }
}
