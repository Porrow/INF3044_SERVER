package javaserver;

import java.io.*;
import java.util.ArrayList;

public class Room
{
	private static final String[] FILES = {"res/room1", "res/room2", "res/room3"};
	
	private final ArrayList<User> users = new ArrayList<>();					//Contient tous les utilisateurs du salon
	private final int id;
	
	public Room(int id)
	{
		this.id = id;
	}
	
	//Ajout d'un utilisateur
	public void addUser(User u)
	{
		synchronized(users){users.add(u);}
	}
	
	//Suppression d'un utilisateur
	public void delUser(User u)
	{
		synchronized(users){users.remove(u);}
	}
	
	//Sauvegarde le message puis, l'envoie à tout les utilisateurs dans le salon (sauf this)
	public void send(User u, String mes)
	{
		for(User user : users)
			if(!user.equals(u))
				user.addMessage("TXT#"+mes);
	}
	
	public ArrayList<User> getUsers(){return users;}
	public String getFilePath()
	{
		return FILES[id];
	}
	
	public void transfert(BufferedInputStream in, BufferedOutputStream out) throws IOException
    {
        byte buf[] = new byte[4096];

        int n;
        while((n = in.read(buf)) != -1) 
            out.write(buf, 0, n);
    }
}