/* 
Copyright Paul James Mutton, 2001-2007, http://www.jibble.org/

This file is part of PircBot.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

*/


package xyz.sonbn.ircclient.irc;

import android.util.Log;

import java.io.BufferedWriter;

public class OutputThread extends Thread {
    OutputThread(IRCProtocol protocol, Queue outQueue) {
        mProtocol = protocol;
        outputQueue = outQueue;
        this.setName(this.getClass() + "-Thread");
    }

    static void sendRawLine(IRCProtocol protocol, BufferedWriter bwriter, String line) {
//        if (line.length() > protocol.getMaxLineLength() - 2) {
//            line = line.substring(0, protocol.getMaxLineLength() - 2);
//        }
        Log.d("COMMAND", line);
        try {
            bwriter.write(line + "\r\n");
            bwriter.flush();
        }
        catch (Exception e) {
            // Silent response - just lose the line.
        }
    }
    
    
    /**
     * This method starts the Thread consuming from the outgoing message
     * Queue and sending lines to the server.
     */
    public void run() {
        try {
            boolean running = true;
            while (running) {
                // Small delay to prevent spamming of the channel
                Thread.sleep(mProtocol.getMessageDelay());
                
                String line = (String) outputQueue.next();
                if (line != null) {
                    mProtocol.sendRawLine(line);
                }
                else {
                    running = false;
                }
            }
        }
        catch (InterruptedException e) {
            // Just let the method return naturally...
        }
    }
    
    private IRCProtocol mProtocol = null;
    private Queue outputQueue = null;
    
}
