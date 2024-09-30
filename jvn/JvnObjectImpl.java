package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject{
    int id;
    Serializable o;
    JvnServerImpl remoteServer;


    public JvnObjectImpl(int id, Serializable o, JvnServerImpl remoteServer){
        this.id = id;
        this.o = o;
        this.remoteServer = remoteServer;
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

    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return id;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return o;
    }

    @Override
    public void jvnInvalidateReader() throws JvnException {

    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        return o;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        return o;
    }
}
