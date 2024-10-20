/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.HashMap;


public class JvnServerImpl
        extends UnicastRemoteObject
        implements JvnLocalServer, JvnRemoteServer {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // A JVN server is managed as a singleton
    private static JvnServerImpl js = null;
    private HashMap<Integer, JvnObject> hashMap;
    JvnRemoteCoord remoteCoord;

    /**
     * Default constructor
     *
     * @throws JvnException
     **/
    private JvnServerImpl() throws Exception {
        super();
        hashMap = new HashMap<Integer, JvnObject>();
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 20000);
            remoteCoord = (JvnRemoteCoord) registry.lookup("coordinateurLeonard");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Static method allowing an application to get a reference to
     * a JVN server instance
     *
     * @throws JvnException
     **/
    public static JvnServerImpl jvnGetServer() {
        if (js == null) {
            try {
                js = new JvnServerImpl();
            } catch (Exception e) {
                return null;
            }
        }
        return js;
    }

    /**
     * The JVN service is not used anymore
     *
     * @throws JvnException
     **/
    public void jvnTerminate()
            throws JvnException {
        try {
            remoteCoord.jvnTerminate((JvnRemoteServer) this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * creation of a JVN object
     *
     * @param o : the JVN object state
     * @throws JvnException
     **/
    public JvnObject jvnCreateObject(Serializable o)
            throws JvnException {
        try {
            int id = remoteCoord.jvnGetObjectId();
            JvnObjectImpl object = new JvnObjectImpl(id, o, this);
            object.state = STATE.W;
            object.isLockWrite = true;
            hashMap.put(id, object);
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @throws JvnException
     **/
    public void jvnRegisterObject(String jon, JvnObject jo)
            throws JvnException {
        try {
            remoteCoord.jvnRegisterObject(jon, jo, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     *
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
     **/
    public JvnObject jvnLookupObject(String jon)
            throws JvnException {
        try {
            JvnObject jo = remoteCoord.jvnLookupObject(jon, this);
            if (jo == null) return null;
            JvnObjectImpl object = new JvnObjectImpl(jo.jvnGetObjectId(), jo.jvnGetSharedObject(), this);
            hashMap.put(jo.jvnGetObjectId(), object);
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockRead(int joi)
            throws JvnException {
        try {
            return remoteCoord.jvnLockRead(joi, this);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockWrite(int joi)
            throws JvnException {
        try {
            return remoteCoord.jvnLockWrite(joi, this);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invalidate the Read lock of the JVN object identified by id
     * called by the JvnCoord
     *
     * @param joi : the JVN object id
     * @return void
     * @throws java.rmi.RemoteException,JvnException
     **/
    public void jvnInvalidateReader(int joi)
            throws java.rmi.RemoteException, JvnException {
        hashMap.get(joi).jvnInvalidateReader();
    }
    /**
     * Invalidate the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriter(int joi)
            throws java.rmi.RemoteException, JvnException {
        return hashMap.get(joi).jvnInvalidateWriter();
    }

    /**
     * Reduce the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriterForReader(int joi)
            throws java.rmi.RemoteException, JvnException {
        return hashMap.get(joi).jvnInvalidateWriterForReader();
    }

}

 
