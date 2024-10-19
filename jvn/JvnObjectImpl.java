package jvn;

import java.io.Serializable;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;

public class JvnObjectImpl implements JvnObject{
    int id;
    Serializable o;
    transient JvnLocalServer remoteServer;
    STATE state;
    boolean isLockWrite;
//    Lock lock;

    public JvnObjectImpl(int id, Serializable o, JvnLocalServer remoteServer){
        this.id = id;
        this.o = o;
        this.remoteServer = remoteServer;
        this.state = STATE.NL;
//        this.lock = new ReentrantLock();
    }

    @Override
    public void setObject(Serializable o)
    {
        this.o = o;
    }

    @Override
    public synchronized void jvnLockRead() throws JvnException {
        while (isLockWrite){
            try {
                wait();
            }catch(InterruptedException e){
                throw new JvnException(e.getMessage());
            }
        }
        if(this.state == STATE.RC || this.state == STATE.R)
        {
            this.state = STATE.R;
            return;
        }
        if(this.state == STATE.WC)
        {
            this.state = STATE.RWC;
            return;
        }
        if(this.state == STATE.NL) this.state = STATE.R;
        this.o = remoteServer.jvnLockRead(this.id);
    }

    @Override
    public synchronized void jvnLockWrite() throws JvnException {
        while (isLockWrite){
            try {
                wait();
            }catch(InterruptedException e){
                throw new JvnException(e.getMessage());
            }
        }
        if(this.state != STATE.WC){
            this.o = remoteServer.jvnLockWrite(this.id);
        }
        isLockWrite = true;
        this.state = STATE.W;
    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
        if(STATE.W == this.state) {
            this.state = STATE.WC;
            isLockWrite = false;
            notifyAll();
        }
        if(STATE.R == this.state) {
            this.state = STATE.RC;
//            isLockWrite = false;
            notifyAll();
        }
//        lock.unlock();
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return this.id;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return (Serializable) o;
    }

    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
        while(isLockWrite){
            try {
                wait();
            }catch(InterruptedException e){
                throw new JvnException(e.getMessage());
            }
        }
        if (this.state == STATE.R) {
            this.state = STATE.NL;
        }
        if (this.state == STATE.RC) {
            this.state = STATE.NL;
        }
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        while(isLockWrite){
            try {
                wait();
            }catch(InterruptedException e){
                throw new JvnException(e.getMessage());
            }
        }
        if (this.state == STATE.W || this.state == STATE.WC) {
            this.state = STATE.NL;
        }
        return this.o;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
//        try {
//            lock.wait();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        while(isLockWrite){
            try {
                wait();
            }catch(InterruptedException e){
                throw new JvnException(e.getMessage());
            }
        }
//        try {
//            wait();
//        }catch(InterruptedException e){
//            throw new JvnException(e.getMessage());
//        }
        if (this.state == STATE.W || this.state == STATE.WC) {
            this.state = STATE.RC;
        }
        if (this.state == STATE.RWC) {
            this.state = STATE.R ;
        }
        return this.o;
    }
}
