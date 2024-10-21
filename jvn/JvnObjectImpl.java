package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
    int id;
    Serializable o;
    transient JvnLocalServer remoteServer;
    STATE state;

    public JvnObjectImpl(int id, Serializable o, JvnLocalServer remoteServer) {
        this.id = id;
        this.o = o;
        this.remoteServer = remoteServer;
        this.state = STATE.NL;
    }

    @Override
    public void setObject(Serializable o) {
        this.o = o;
    }

    @Override
    public synchronized void jvnLockRead() throws JvnException {
        if (this.state == STATE.RWC || this.state == STATE.RC || this.state == STATE.R) {
            this.state = STATE.R;
            return;
        }
        if (this.state == STATE.WC) {
            this.state = STATE.RWC;
            return;
        }
        if (this.state == STATE.NL) this.state = STATE.R;
        this.o = remoteServer.jvnLockRead(this.id);
    }

    @Override
    public synchronized void jvnLockWrite() throws JvnException {
        if (this.state != STATE.WC && this.state != STATE.RWC) {
            this.o = remoteServer.jvnLockWrite(this.id);
        }
        this.state = STATE.W;
    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
        if (this.state == STATE.W) this.state = STATE.WC;
        if (this.state == STATE.R) this.state = STATE.RC;
        notifyAll();
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
        if (this.state == STATE.W) {
            try {
                wait();
            } catch (InterruptedException e) {
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
        if (this.state == STATE.W) {
            try {
                wait();
            } catch (InterruptedException e) {
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
        if (this.state == STATE.W) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new JvnException(e.getMessage());
            }
        }
        if (this.state == STATE.W || this.state == STATE.WC) {
            this.state = STATE.RC;
        }
        if (this.state == STATE.RWC) {
            this.state = STATE.R;
        }
        return this.o;
    }
}
