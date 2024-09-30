package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject{
    int id;
    Serializable o;
    JvnServerImpl remoteServer;
    STATE state;

    public JvnObjectImpl(int id, Serializable o, JvnServerImpl remoteServer){
        this.id = id;
        this.o = o;
        this.remoteServer = remoteServer;
        this.state = STATE.NL;
    }

    @Override
    public void jvnLockRead() throws JvnException {
        try {
            this.o = remoteServer.jvnLockRead(this.id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        try {
            this.o = remoteServer.jvnLockWrite(this.id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void jvnUnLock() throws JvnException {
        this.state = STATE.NL;
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
        if (this.state == STATE.R) {
            this.state = STATE.NL;
        }
        if (this.state == STATE.RC) {
            this.state = STATE.NL;
        }
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        if (this.state == STATE.W) {
            this.state = STATE.NL;
        }
        return this.o;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        if (this.state == STATE.W) {
            this.state = STATE.RC;
        }
        if (this.state == STATE.RWC) {
            this.state = STATE.R ;
        }
        return this.o;
    }
}
