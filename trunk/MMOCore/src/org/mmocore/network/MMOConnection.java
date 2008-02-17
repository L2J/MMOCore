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

import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

import javolution.util.FastList;


/**
 * @author KenM
 *
 */
public class MMOConnection<T extends MMOClient>
{
    private final SelectorThread<T> _selectorThread;
    private T _client;
    
    private ISocket _socket;
    private WritableByteChannel _writableByteChannel;
    private ReadableByteChannel _readableByteChannel;
    
    private FastList<SendablePacket<T>> _sendQueue = new FastList<SendablePacket<T>>();
    private SelectionKey _selectionKey;
    
    private int _readHeaderPending;
    private ByteBuffer _readBuffer;
    
    private ByteBuffer _primaryWriteBuffer;
    private ByteBuffer _secondaryWriteBuffer;
    
    private boolean _pendingClose;
    
    public MMOConnection(SelectorThread<T> selectorThread, ISocket socket, SelectionKey key)
    {
        _selectorThread = selectorThread;
        this.setSocket(socket);
        this.setWritableByteChannel(socket.getWritableByteChannel());
        this.setReadableByteChannel(socket.getReadableByteChannel());
        this.setSelectionKey(key);
    }
    
    public MMOConnection(T client, SelectorThread<T> selectorThread, ISocket socket, SelectionKey key)
    {
        this(selectorThread, socket, key);
        this.setClient(client);
    }
    
    protected void setClient(T client)
    {
        _client = client;
    }
    
    public T getClient()
    {
        return _client;
    }
    
    public void sendPacket(SendablePacket<T> sp)
    {
        sp.setClient(this.getClient());
        synchronized (this.getSendQueue())
        {
            if (!_pendingClose)
            {
                try
                {
                    this.getSelectionKey().interestOps(this.getSelectionKey().interestOps() | SelectionKey.OP_WRITE);
                    this.getSendQueue().addLast(sp);
                }
                catch (CancelledKeyException e)
                {
                    // ignore
                }
            }
        }
    }
    
    protected SelectorThread<T> getSelectorThread()
    {
        return _selectorThread;
    }
    
    protected void setSelectionKey(SelectionKey key)
    {
        _selectionKey = key;
    }
    
    protected SelectionKey getSelectionKey()
    {
        return _selectionKey;
    }
    
    protected void enableReadInterest()
    {
        try
        {
            this.getSelectionKey().interestOps(this.getSelectionKey().interestOps() | SelectionKey.OP_READ);
        }
        catch (CancelledKeyException e)
        {
            // ignore
        }
    }
    
    protected void disableReadInterest()
    {
        try
        {
            this.getSelectionKey().interestOps(this.getSelectionKey().interestOps() & ~SelectionKey.OP_READ);
        }
        catch (CancelledKeyException e)
        {
            // ignore
        }
    }
    
    protected void enableWriteInterest()
    {
        try
        {
            this.getSelectionKey().interestOps(this.getSelectionKey().interestOps() | SelectionKey.OP_WRITE);
        }
        catch (CancelledKeyException e)
        {
            // ignore
        }
    }
    
    protected void disableWriteInterest()
    {
        try
        {
            this.getSelectionKey().interestOps(this.getSelectionKey().interestOps() & ~SelectionKey.OP_WRITE);
        }
        catch (CancelledKeyException e)
        {
            // ignore
        }
    }
    
    /**
     * @param socket the socket to set
     */
    protected void setSocket(ISocket socket)
    {
        _socket = socket;
    }

    /**
     * @return the socket
     */
    public ISocket getSocket()
    {
        return _socket;
    }

    protected void setWritableByteChannel(WritableByteChannel wbc)
    {
        _writableByteChannel = wbc;
    }
    
    public WritableByteChannel getWritableChannel()
    {
        return _writableByteChannel;
    }
    
    protected void setReadableByteChannel(ReadableByteChannel rbc)
    {
        _readableByteChannel = rbc;
    }
    
    public ReadableByteChannel getReadableByteChannel()
    {
        return _readableByteChannel;
    }
    
    protected FastList<SendablePacket<T>> getSendQueue()
    {
        return _sendQueue;
    }
    
    protected void createWriteBuffer(ByteBuffer buf)
    {
        if (_primaryWriteBuffer == null)
        {
            //System.err.println("APPENDING FOR NULL");
            //System.err.flush();
            _primaryWriteBuffer = this.getSelectorThread().getPooledBuffer();
            _primaryWriteBuffer.put(buf);
        }
        else
        {
            //System.err.println("PREPENDING ON EXISTING");
            //System.err.flush();
            
            ByteBuffer temp = this.getSelectorThread().getPooledBuffer();
            temp.put(buf);
            
            int remaining = temp.remaining();
            _primaryWriteBuffer.flip();
            int limit = _primaryWriteBuffer.limit();
            
            if (remaining >= _primaryWriteBuffer.remaining())
            {
                temp.put(_primaryWriteBuffer);
                this.getSelectorThread().recycleBuffer(_primaryWriteBuffer);
                _primaryWriteBuffer = temp;
            }
            else
            {
                _primaryWriteBuffer.limit(remaining);
                temp.put(_primaryWriteBuffer);
                _primaryWriteBuffer.limit(limit);
                _primaryWriteBuffer.compact();
                _secondaryWriteBuffer = _primaryWriteBuffer;
                _primaryWriteBuffer = temp;
            }
        }
    }
    
    /*
    protected void appendIntoWriteBuffer(ByteBuffer buf)
    {
        // if we already have a buffer
        if (_secondaryWriteBuffer != null && (_primaryWriteBuffer != null && !_primaryWriteBuffer.hasRemaining()))
        {
            _secondaryWriteBuffer.put(buf);
            
            if (MMOCore.ASSERTIONS_ENABLED)
            {
                // correct state
                assert _primaryWriteBuffer == null || !_primaryWriteBuffer.hasRemaining();
                // full write
                assert !buf.hasRemaining();
            }
        }
        else if (_primaryWriteBuffer != null)
        {
            int size = Math.min(buf.limit(), _primaryWriteBuffer.remaining());
            _primaryWriteBuffer.put(buf.array(), buf.position(), size);
            buf.position(buf.position() + size);
            
            // primary wasnt enough
            if (buf.hasRemaining())
            {
                _secondaryWriteBuffer = this.getSelectorThread().getPooledBuffer();
                _secondaryWriteBuffer.put(buf);
            }
            
            if (MMOCore.ASSERTIONS_ENABLED)
            {
                // full write
                assert !buf.hasRemaining();
            }
        }
        else
        {
            // a single empty buffer should be always enough by design
            _primaryWriteBuffer = this.getSelectorThread().getPooledBuffer();
            _primaryWriteBuffer.put(buf);
            System.err.println("ESCREVI "+_primaryWriteBuffer.position());
            if (MMOCore.ASSERTIONS_ENABLED)
            {
                // full write
                assert !buf.hasRemaining();
            }
        }
    }*/
    
    /*protected void prependIntoPendingWriteBuffer(ByteBuffer buf)
    {
        int remaining = buf.remaining();
        
        //do we already have some buffer
        if (_primaryWriteBuffer != null && _primaryWriteBuffer.hasRemaining())
        {
            if (remaining == _primaryWriteBuffer.capacity())
            {
                if (MMOCore.ASSERTIONS_ENABLED)
                {
                    assert _secondaryWriteBuffer == null;
                }
                
                _secondaryWriteBuffer = _primaryWriteBuffer;
                _primaryWriteBuffer = this.getSelectorThread().getPooledBuffer();
                _primaryWriteBuffer.put(buf);
            }
            else if (remaining < _primaryWriteBuffer.remaining())
            {
                
            }
        }
        else
        {
            
        }
    }*/
    
    protected boolean hasPendingWriteBuffer()
    {
        return _primaryWriteBuffer != null;
    }
    
    protected void movePendingWriteBufferTo(ByteBuffer dest)
    {
        //System.err.println("PRI SIZE: "+_primaryWriteBuffer.position());
        //System.err.flush();
        _primaryWriteBuffer.flip();
        dest.put(_primaryWriteBuffer);
        this.getSelectorThread().recycleBuffer(_primaryWriteBuffer);
        _primaryWriteBuffer = _secondaryWriteBuffer;
        _secondaryWriteBuffer = null;
    }
    
    /*protected void finishPrepending(int written)
    {
        _primaryWriteBuffer.position(Math.min(written, _primaryWriteBuffer.limit()));
        // discard only the written bytes
        _primaryWriteBuffer.compact();
        
        if (_secondaryWriteBuffer != null)
        {
            _secondaryWriteBuffer.flip();
            _primaryWriteBuffer.put(_secondaryWriteBuffer);
            
            if (!_secondaryWriteBuffer.hasRemaining())
            {
                this.getSelectorThread().recycleBuffer(_secondaryWriteBuffer);
                _secondaryWriteBuffer = null;
            }
            else
            {
                _secondaryWriteBuffer.compact();
            }
        }
    }*/
    
    protected ByteBuffer getWriteBuffer()
    {
        ByteBuffer ret = _primaryWriteBuffer;
        if (_secondaryWriteBuffer != null)
        {
            _primaryWriteBuffer = _secondaryWriteBuffer;
            _secondaryWriteBuffer = null;
        }
        return ret;
    }

    protected void setPendingHeader(int size)
    {
        _readHeaderPending = size;
    }
    
    protected int getPendingHeader()
    {
        return _readHeaderPending;
    }
    
    protected void setReadBuffer(ByteBuffer buf)
    {
        _readBuffer = buf;
    }
    
    protected ByteBuffer getReadBuffer()
    {
        return _readBuffer;
    }
    
    public boolean isClosed()
    {
        return _pendingClose;
    }
    
    protected void closeNow()
    {
        synchronized (this.getSendQueue())
        {
            if (!this.isClosed())
            {
                _pendingClose = true;
                this.getSendQueue().clear();
                this.disableWriteInterest();
                this.getSelectorThread().closeConnection(this);
            }
        }
        
    }
    
    public void close(SendablePacket<T> sp)
    {
        synchronized (this.getSendQueue())
        {
            if (!this.isClosed())
            {
                this.getSendQueue().clear();
                this.sendPacket(sp);
                _pendingClose = true;
                this.getSelectorThread().closeConnection(this);
            }
        }
    }
    
    protected void closeLater()
    {
        synchronized (this.getSendQueue())
        {
            if (!this.isClosed())
            {
                _pendingClose = true;
                this.getSelectorThread().closeConnection(this);
            }
        }
        
    }
    
    protected void releaseBuffers()
    {
        if (_primaryWriteBuffer != null)
        {
            this.getSelectorThread().recycleBuffer(_primaryWriteBuffer);
            _primaryWriteBuffer = null;
            if (_secondaryWriteBuffer != null)
            {
                this.getSelectorThread().recycleBuffer(_secondaryWriteBuffer);
                _secondaryWriteBuffer = null;
            }
        }
        if (_readBuffer != null)
        {
            this.getSelectorThread().recycleBuffer(_readBuffer);
            _readBuffer = null;
        }
    }
    
    protected void onDisconnection()
    {
        this.getClient().onDisconnection();
    }
    
    protected void onForcedDisconnection()
    {
        this.getClient().onForcedDisconnection();
    }
}
