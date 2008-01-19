/**
 * 
 */
package org.mmocore.test.testserver;

import java.nio.ByteBuffer;

import org.mmocore.network.MMOClient;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.SendablePacket;


/**
 * @author KenM
 *
 */
public final class ServerClient extends MMOClient<MMOConnection<ServerClient>>
{

    public ServerClient(MMOConnection<ServerClient> con)
    {
        super(con);
    }

    @Override
    public boolean decrypt(ByteBuffer buf, int size)
    {
        return true;
    }

    @Override
    public boolean encrypt(ByteBuffer buf, int size)
    {
        buf.position(buf.position() + size);
        return true;
    }

    public void sendPacket(SendablePacket<ServerClient> sp)
    {
        //System.out.println("SENDPACKET");
        this.getConnection().sendPacket(sp);
    }
    
    public void onDisconection()
    {
        //System.out.println("CLIENT DISCONNECTED\n=============================\n");
    }
}
