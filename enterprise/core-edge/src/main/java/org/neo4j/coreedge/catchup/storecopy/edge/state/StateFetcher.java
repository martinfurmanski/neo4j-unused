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
package org.neo4j.coreedge.catchup.storecopy.edge.state;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.neo4j.coreedge.catchup.storecopy.StoreCopyFailedException;
import org.neo4j.coreedge.catchup.storecopy.core.RaftStateType;
import org.neo4j.coreedge.catchup.storecopy.edge.CoreClient;
import org.neo4j.coreedge.catchup.tx.edge.RaftStateSnapshotListener;
import org.neo4j.coreedge.raft.state.CoreState;
import org.neo4j.coreedge.server.AdvertisedSocketAddress;

public class StateFetcher
{
    private final CoreClient coreClient;

    public StateFetcher( CoreClient coreClient )
    {
        this.coreClient = coreClient;
    }

    public void copyRaftState( AdvertisedSocketAddress from, CoreState coreState ) throws StoreCopyFailedException
    {
        HashMap<RaftStateType, Object> map = new HashMap<>();

        CompletableFuture<HashMap<RaftStateType, Object>> completableSnapshot = new CompletableFuture<>();

        RaftStateSnapshotListener listener = snapshot -> {
            map.put( snapshot.type(), snapshot.state() );

            if ( map.size() >= RaftStateType.values().length )
            {
                completableSnapshot.complete( map );
            }
        };

        coreClient.addRaftStateSnapshotListener( listener );
        coreClient.requestRaftState( from );
        try
        {
            HashMap<RaftStateType, Object> snapshot = completableSnapshot.get( 10, TimeUnit.SECONDS );
            coreState.installSnapshots( snapshot );

            coreClient.removeRaftStateSnapshotListener( listener );
        }
        catch ( InterruptedException | ExecutionException | TimeoutException e )
        {
            throw new StoreCopyFailedException( e );
        }
    }
}
