/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package org.mmocore.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author KenM
 *
 */
public class UDPSocket implements ISocket
{
    private final DatagramSocket _socket;

    public UDPSocket(DatagramSocket socket)
    {
        _socket = socket;
    }
    
    /* (non-Javadoc)
     * @see com.l2jserver.mmocore.network.ISocket#close()
     */
    public void close() throws IOException
    {
        _socket.close();
    }

    /* (non-Javadoc)
     * @see com.l2jserver.mmocore.network.ISocket#getReadableByteChannel()
     */
    public ReadableByteChannel getReadableByteChannel()
    {
        return _socket.getChannel();
    }

    /* (non-Javadoc)
     * @see com.l2jserver.mmocore.network.ISocket#getWritableByteChannel()
     */
    public WritableByteChannel getWritableByteChannel()
    {
        return _socket.getChannel();
    }

    /* (non-Javadoc)
     * @see org.mmocore.network.ISocket#getInetAddress()
     */
    public InetAddress getInetAddress()
    {
        return _socket.getInetAddress();
    }
    
}
