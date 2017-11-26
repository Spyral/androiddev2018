package xyz.sonbn.ircclient.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by sonbn on 11/24/2017.
 */

public abstract class IRCProtocol {
    private final int DEFAULT_PORT = 6667;

    private String mHostname, mPassword;
    private int mPort;
    private Socket mSocket;
    private InetAddress mInetAddress;
    private long mMessageDelay = 1000;
    private InputThread mInputThread = null;

    public IRCProtocol() {
    }

    public long getMessageDelay() {
        return mMessageDelay;
    }

    public void setMessageDelay(long messageDelay) {
        if (messageDelay < 0){
            throw new IllegalArgumentException("Time cannot negative");
        }
        mMessageDelay = messageDelay;
    }

    //Overloading connect
    public final synchronized void connect(String hostname) throws IOException{
        connect(hostname, DEFAULT_PORT, null);
    }

    public final synchronized void connect(String hostname, int port) throws IOException{
        connect(hostname, port, null);
    }

    public final synchronized void connect(String hostname, int port, String password) throws IOException{
        mHostname = hostname;
        mPort = port;
        mPassword = password;

        mSocket = new Socket(mHostname, mPort);

        mInetAddress = mSocket.getLocalAddress();

        InputStreamReader inputStreamReader = new InputStreamReader(mSocket.getInputStream());
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mSocket.getOutputStream());

        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        mInputThread = new InputThread(this, mSocket, bufferedReader, bufferedWriter);
    }

    public final synchronized void sendRawLine(String line){
        mInputThread.sendRawLine(line);
    }

    public final int getMaxLineLength() {
        return InputThread.MAX_LINE_LENGTH;
    }
}
