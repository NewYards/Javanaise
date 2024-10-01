package jvn;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JvnObjectImpl implements JvnObject{
    int id;
    Serializable o;
    JvnServerImpl remoteServer;
    STATE state;
    Lock lock;

    public JvnObjectImpl(int id, Serializable o, JvnServerImpl remoteServer){
        this.id = id;
        this.o = o;
        this.remoteServer = remoteServer;
        this.state = STATE.NL;
        this.lock = new ReentrantLock();
    }

    @Override
    public void jvnLockRead() throws JvnException {
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
        try {
            this.o = remoteServer.jvnLockRead(this.id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        lock.lock();
        try {
            this.o = remoteServer.jvnLockWrite(this.id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void jvnUnLock() throws JvnException {
        lock.unlock();
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return this.id;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return this.o;
    }

    @Override
    public void jvnInvalidateReader() throws JvnException {
        lock.lock();
        if (this.state == STATE.R) {
            this.state = STATE.NL;
        }
        if (this.state == STATE.RC) {
            this.state = STATE.NL;
        }
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        lock.lock();
        if (this.state == STATE.W) {
            this.state = STATE.NL;
        }
        return this.o;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        lock.lock();
        if (this.state == STATE.W) {
            this.state = STATE.RC;
        }
        if (this.state == STATE.RWC) {
            this.state = STATE.R ;
        }
        return this.o;
    }
}
