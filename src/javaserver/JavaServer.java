package javaserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class JavaServer 
{
	public static final boolean DEBUG = true;
			
	private static JavaServer js;												//Instance de la classe, initialisée dans le main
	
    private final int PORT = 8525;												//Port système
    private ServerSocket soc;                                                   //Socket serveur synchroniser
	private final ArrayList<User> users = new ArrayList<>();					//Contient tous les utilisateurs
	private Room[] rooms;
    
    public JavaServer()
    {
		rooms = new Room[]{new Room(0), new Room(1), new Room(2)};				//Création de 3 salons de discussion
        try{soc = new ServerSocket(PORT);}                                      //Lancement de la socket synchroniser
        catch (IOException e) {System.err.println(e.getMessage());}
		new Accept(soc).start();                                                //Lancement de la boucle d'acceptation des utilisateurs
		System.out.println("Serveur prêt. En attente de requête sur le port 8525...");
    }
	
	public ArrayList<User> getUsers(){return users;}
	public Room[] getRooms(){return rooms;}
	
	public static JavaServer getInstance(){return js;}
    
	//Ajout d'un utilisateur
	public static void addUser(User u)
	{
		ArrayList<User> users = js.getUsers();
		synchronized(users){users.add(u);}
	}
	
	//Suppression d'un utilisateur
	public static void delUser(User u)
	{
		ArrayList<User> users = js.getUsers();
		synchronized(users){users.remove(u);}
	}
	
    public static void main(String[] args) 
    {
        js = new JavaServer();
    }
}