/**
 * 
 */
package org.mmocore.test.testserver;

import org.mmocore.network.SendablePacket;

/**
 * @author KenM
 *
 */
public class TestSendPacket extends SendablePacket<ServerClient>
{
    private int _value;
    
    public TestSendPacket(int value)
    {
        _value = value;
    }
    
    @Override
    protected void write()
    {
        writeD(_value);
        for (int i = 0; i < 48000; i++)
        {
            writeC(i%256);
        }
    }

    @Override
    protected int getHeaderSize()
    {
        return 2;
    }

    @Override
    protected void writeHeader(int dataSize)
    {
        writeH(dataSize + 2);
    }
}
