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
package org.neo4j.coreedge.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

import io.netty.buffer.ByteBuf;

import org.neo4j.coreedge.raft.replication.StringMarshal;
import org.neo4j.coreedge.raft.state.ByteBufferMarshal;
import org.neo4j.coreedge.raft.state.ChannelMarshal;
import org.neo4j.storageengine.api.ReadableChannel;
import org.neo4j.storageengine.api.WritableChannel;

public class AdvertisedSocketAddress implements Comparable<AdvertisedSocketAddress>
{
    private final String address;

    public AdvertisedSocketAddress( String address )
    {
        if( address.equals( "localhost" ))
        {
            System.out.println();
        }
        this.address = address;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        AdvertisedSocketAddress that = (AdvertisedSocketAddress) o;
        return Objects.equals( address, that.address );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( address );
    }

    public String toString()
    {
        return address;
    }

    public InetSocketAddress socketAddress()
    {
        String[] split = address.split( ":" );
        return new InetSocketAddress( split[0], Integer.valueOf( split[1] ) );
    }

    @Override
    public int compareTo( AdvertisedSocketAddress o )
    {
        return address.compareTo( o.address );
    }

    public static class AdvertisedSocketAddressByteBufferMarshal implements ByteBufferMarshal<AdvertisedSocketAddress>
    {
        public void marshal( AdvertisedSocketAddress address, ByteBuf buffer )
        {
            StringMarshal.marshal( buffer, address.address );
        }

        public void marshal( AdvertisedSocketAddress address, ByteBuffer buffer )
        {
            StringMarshal.marshal( buffer, address.address );
        }

        public AdvertisedSocketAddress unmarshal( ByteBuf buffer )
        {
            try
            {
                String host = StringMarshal.unmarshal( buffer );
                return new AdvertisedSocketAddress( host );
            }
            catch( IndexOutOfBoundsException notEnoughBytes )
            {
                return null;
            }
        }

        public AdvertisedSocketAddress unmarshal( ByteBuffer buffer )
        {
            try
            {
                String host = StringMarshal.unmarshal( buffer );
                return new AdvertisedSocketAddress( host );
            }
            catch( BufferUnderflowException notEnoughBytes )
            {
                return null;
            }
        }
    }

    public static class AdvertisedSocketAddressChannelMarshal implements ChannelMarshal<AdvertisedSocketAddress>
    {
        public void marshal( AdvertisedSocketAddress address, WritableChannel channel) throws IOException
        {
            StringMarshal.marshal( channel, address.address );
        }

        public void marshal( AdvertisedSocketAddress address, ByteBuffer buffer )
        {
            StringMarshal.marshal( buffer, address.address );
        }

        public AdvertisedSocketAddress unmarshal( ReadableChannel channel ) throws IOException
        {
            try
            {
                String host = StringMarshal.unmarshal( channel );
                return new AdvertisedSocketAddress( host );
            }
            catch( IndexOutOfBoundsException notEnoughBytes )
            {
                return null;
            }
        }

        public AdvertisedSocketAddress unmarshal( ByteBuffer buffer )
        {
            try
            {
                String host = StringMarshal.unmarshal( buffer );
                return new AdvertisedSocketAddress( host );
            }
            catch( BufferUnderflowException notEnoughBytes )
            {
                return null;
            }
        }
    }
}
