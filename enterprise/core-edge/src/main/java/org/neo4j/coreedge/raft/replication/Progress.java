/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.coreedge.raft.replication;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * The progress of a single replicated operation, from replication to result, and associated synchronization.
 */
public class Progress
{
    private ReentrantLock lock = new ReentrantLock();
    private Condition replicationEvent = lock.newCondition();
    private CompletableFuture<Object> futureResult = new CompletableFuture<>();

    private boolean isReplicated;

    public void triggerReplication()
    {
        lock.lock();
        replicationEvent.signal();
        lock.unlock();
    }

    public void setReplicated()
    {
        lock.lock();
        isReplicated = true;
        replicationEvent.signal();
        lock.unlock();
    }

    public void awaitReplication( long timeoutMillis ) throws InterruptedException
    {
        lock.lock();
        if( !isReplicated )
        {
            replicationEvent.await( timeoutMillis, MILLISECONDS );
        }
        lock.unlock();
    }

    public boolean isReplicated()
    {
        return isReplicated;
    }

    public CompletableFuture<Object> futureResult()
    {
        return futureResult;
    }
}
