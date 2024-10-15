package jvn;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JvnObjectImpl implements JvnObject{
    int id;
    Serializable o;
    transient JvnLocalServer remoteServer;
    STATE state;
    Lock lock;

    public JvnObjectImpl(int id, Serializable o, JvnLocalServer remoteServer){
        this.id = id;
        this.o = o;
        this.remoteServer = remoteServer;
        this.state = STATE.NL;
        this.lock = new ReentrantLock();
    }

    @Override
    public void setObject(Serializable o)
    {
        this.o = o;
    }

    @Override
    public void jvnLockRead() throws JvnException {
        lock.lock();
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
    public void jvnLockWrite() throws JvnException {
        lock.lock();
        this.o = remoteServer.jvnLockWrite(this.id);
        this.state = STATE.W;
    }

    @Override
    public void jvnUnLock() throws JvnException {
        if(STATE.W == this.state) this.state = STATE.WC;
        if(STATE.R == this.state) this.state = STATE.RC;
        lock.unlock();
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
    public void jvnInvalidateReader() throws JvnException {
        try {
            lock.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (this.state == STATE.R) {
            this.state = STATE.NL;
        }
        if (this.state == STATE.RC) {
            this.state = STATE.NL;
        }
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        try {
            lock.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (this.state == STATE.W || this.state == STATE.WC) {
            this.state = STATE.NL;
        }
        return this.o;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        try {
            lock.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (this.state == STATE.W || this.state == STATE.WC) {
            this.state = STATE.RC;
        }
        if (this.state == STATE.RWC) {
            this.state = STATE.R ;
        }
        return this.o;
    }
}
