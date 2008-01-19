/**
 * 
 */
package org.mmocore.test.testserver;

import java.io.IOException;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;


/**
 * @author KenM
 *
 */
public class TestServer
{
    public static int PORT = 0xCAFE;
    
    public static void main(String[] args) throws IOException
    {
        if (args.length > 0)
        {
            PORT = Integer.parseInt(args[0]);
        }
        
        SelectorHelper sh = new SelectorHelper();
        SelectorConfig<ServerClient> ssc = new SelectorConfig<ServerClient>(null, sh);
        ssc.setMaxSendPerPass(2);
        SelectorThread<ServerClient> selector = new SelectorThread<ServerClient>(ssc, null, sh, sh, sh, null);
        selector.openServerSocket(null, PORT);
        //selector.openDatagramSocket();
        selector.start();
        
        System.err.println("TestServer active on port "+PORT);
    }


}
