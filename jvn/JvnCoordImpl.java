/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


class refObject{
    JvnObject o;
    JvnRemoteServer writer;
    ArrayList<JvnRemoteServer> readers;

    public refObject(JvnObject o, JvnRemoteServer js){
        this.o = o;
        writer = js;
        readers = new ArrayList<JvnRemoteServer>();
    }
}

public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{
	

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private HashMap<Integer, refObject> objects;
    private HashMap<String, Integer> ids;
    private HashMap<JvnRemoteServer, ArrayList<Integer>> locks;
    private int current_id = 0;
    private Registry registry;
/**
  * Default constructor
  * @throws JvnException
  **/
JvnCoordImpl() throws Exception {
		objects = new HashMap<>();
        ids = new HashMap<>();
        locks = new HashMap<>();

        this.registry = LocateRegistry.createRegistry(20000);
        registry.bind("coordinateurLeonard", this);
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
    current_id++;
    return current_id-1;
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
      refObject ref = new refObject(jo, js);
      int id = jo.jvnGetObjectId();
      ids.put(jon, id);
      objects.put(id, ref);
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
      if(objects.get(ids.get(jon)) == null) return null;
    return objects.get(ids.get(jon)).o;
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
    if(!locks.containsKey(js)) locks.put(js, new ArrayList<Integer>());
    if(!(objects.get(joi).writer == null))
    {
        objects.get(joi).o.setObject(objects.get(joi).writer.jvnInvalidateWriterForReader(joi));
        if(!objects.get(joi).readers.contains(objects.get(joi).writer))objects.get(joi).readers.add(objects.get(joi).writer);
        objects.get(joi).writer = null;
    }
    locks.get(js).add(joi);
    objects.get(joi).readers.add(js);
    return objects.get(joi).o.jvnGetSharedObject();
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{

    if((objects.get(joi).writer == null)) objects.get(joi).o.setObject(objects.get(joi).writer.jvnInvalidateWriter(joi));
    objects.get(joi).readers.remove(js);
    for(JvnRemoteServer reader : objects.get(joi).readers) reader.jvnInvalidateReader(joi);
    objects.get(joi).readers.clear();
    objects.get(joi).writer = js;
    return objects.get(joi).o.jvnGetSharedObject();
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
        for (int id : locks.get(js)) {
            refObject ref = objects.get(id);
            if(ref.writer == js) ref.writer = null;
            ref.readers.remove(js);
        }
    }
}

 
