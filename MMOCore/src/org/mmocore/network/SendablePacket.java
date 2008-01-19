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

/**
 * @author KenM
 *
 */
public abstract class SendablePacket<T extends MMOClient> extends AbstractPacket<T>
{
    protected void putShort(int value)
    {
        this.getByteBuffer().putShort((short) value);
    }
    
    protected void putInt(int value)
    {
        this.getByteBuffer().putInt(value);
    }
    
    protected void putDouble(double value)
    {
        this.getByteBuffer().putDouble(value);
    }
    
    protected void putFloat(float value)
    {
        this.getByteBuffer().putFloat(value);
    }
    
    protected void writeC(int data)
    {
        this.getByteBuffer().put((byte) data);
    }
    
    protected void writeF(double value)
    {
        this.getByteBuffer().putDouble(value);
    }
    
    protected void writeH(int value)
    {
        this.getByteBuffer().putShort((short) value);
    }
    
    protected void writeD(int value)
    {
        this.getByteBuffer().putInt(value);
    }
    
    protected void writeQ(long value)
    {
        this.getByteBuffer().putLong(value);
    }
    
    protected void writeB(byte[] data)
    {
        this.getByteBuffer().put(data);
    }
    
    protected void writeS(CharSequence charSequence)
    {
        if (charSequence == null)
        {
            charSequence = "";
        }
        
        int length = charSequence.length();
        for (int i = 0; i < length; i++)
        {
            this.getByteBuffer().putChar(charSequence.charAt(i));
        }
        this.getByteBuffer().putChar('\000');
    }
    
    protected abstract void write();
    
    protected abstract int getHeaderSize();
    
    protected abstract void writeHeader(int dataSize);
}
