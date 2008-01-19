/**
 * 
 */
package org.mmocore.test.testserver;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mmocore.network.HeaderInfo;
import org.mmocore.network.TCPHeaderHandler;
import org.mmocore.network.IClientFactory;
import org.mmocore.network.IMMOExecutor;
import org.mmocore.network.IPacketHandler;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.ReceivablePacket;


/**
 * @author KenM
 *
 */
public class SelectorHelper extends TCPHeaderHandler<ServerClient> implements IPacketHandler<ServerClient>, IMMOExecutor<ServerClient>, IClientFactory<ServerClient>
{
    public SelectorHelper()
    {
        super(null);
    }

    ExecutorService _tpe = Executors.newFixedThreadPool(4);
    
    public ReceivablePacket<ServerClient> handlePacket(ByteBuffer buf, ServerClient client)
    {
        return new TestRecvPacket();
    }

    public void execute(ReceivablePacket<ServerClient> packet)
    {
        _tpe.execute(packet);
    }

    public ServerClient create(MMOConnection<ServerClient> con)
    {
        System.out.println("new client");
        /*try
        {
            con.getSocketChannel().socket().setSendBufferSize(2*65536);
        }
        catch (SocketException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        ServerClient sc = new ServerClient(con);
        return sc;
    }

    @Override
    public HeaderInfo<ServerClient> handleHeader(SelectionKey key, ByteBuffer buf)
    {
        int avaliable = buf.remaining();
        if (avaliable >= 2)
        {
            int dataSize = buf.getShort() & 0xFFFF;
            System.err.println("DATASIZE: "+dataSize);
            return this.getHeaderInfoReturn().set(0, dataSize - 2, false, null);
        }
        else
        {
            System.err.println("HEADER PENDING: "+(2 - avaliable));
            return this.getHeaderInfoReturn().set(2 - avaliable, 0, false, null);
        }
    }

}
