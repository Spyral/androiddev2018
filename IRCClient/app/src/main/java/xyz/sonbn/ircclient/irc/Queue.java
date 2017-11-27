package xyz.sonbn.ircclient.irc;

import java.util.Vector;

/**
 * Created by sonbn on 11/24/2017.
 */

public class Queue extends Object {
    public int size() {
        return mQueue.size();
    }




    private final Vector<Object> mQueue = new Vector<Object>();

    public void add(Object o) {
        synchronized(mQueue) {
            mQueue.addElement(o);
            mQueue.notify();
        }
    }

    public Object next(){
        Object o;
        synchronized (mQueue){
            if (mQueue.size() == 0){
                try {
                    mQueue.wait();
                } catch (InterruptedException e){
                    return null;
                }
            }

            try {
                o = mQueue.firstElement();
                mQueue.removeElementAt(0);
            } catch (ArrayIndexOutOfBoundsException e){
                throw new InternalError("Race hazard in Queue object.");
            }
        }
        return o;
    }
}
