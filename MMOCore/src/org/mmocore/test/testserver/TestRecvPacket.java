/**
 * 
 */
package org.mmocore.test.testserver;

import org.mmocore.network.ReceivablePacket;

/**
 * @author KenM
 *
 */
public class TestRecvPacket extends ReceivablePacket<ServerClient>
{
    private int _value;
    
    @Override
    protected boolean read()
    {
        _value = readD();
        return true;
    }

    @Override
    public void run()
    {
        System.out.println("ServerRecebeu "+_value);
        TestSendPacket tsp = new TestSendPacket(_value);
        this.getClient().sendPacket(tsp);
        this.getClient().sendPacket(tsp);
        this.getClient().sendPacket(tsp);
        this.getClient().sendPacket(tsp);
        this.getClient().sendPacket(tsp);
        this.getClient().sendPacket(tsp);
    }
}
