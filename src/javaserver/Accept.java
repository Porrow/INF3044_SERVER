package javaserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Accept extends Thread
{
	private ServerSocket socketServ = null;

	public Accept(ServerSocket socketServ)                                      //On transmet la socket serveur système
	{
		this.socketServ = socketServ;
	}

	@Override
	public void run()
	{
		Socket socket;
		User u;
		try
		{
			while(true)                                                         //Boucle de connexion sur un thread indé pour pas tout bloquer
			{
				socket = socketServ.accept();
				System.out.println("Un client souhaite se connecter...");
				u = new User(socket);											//Création d'une nouvel utilisateur
				JavaServer.addUser(u);											//Ajout à la liste des utilisateurs
				u.start();                                                      //Démarrage du Thread
			}
		}
		catch(IOException e) {System.err.println(e.getMessage());}
	}
}