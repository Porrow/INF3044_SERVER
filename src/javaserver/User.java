package javaserver;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import javaserver.io.IO;

class User extends Thread
{
	private static final String[] FLAGS = {"SYN", "ROOM","ACK", "TXT", "FILE", "FIN"};
	private static final String S = "#";                                        //Caractère séparateur
	private static final int TIME = 10;											//Temps entre chaque envoie/réception requête
	private static final int MAX_MES = 30;
	
	private final Socket soc;
	private String name;
	private PrintWriter writer;
	private BufferedInputStream reader;
	private Room room;
	private final ArrayList<String> buffer = new ArrayList<>();
	private Calendar c;

	public User(Socket soc)
	{
		this.soc = soc;
		c = Calendar.getInstance();
	}

	@Override
	public void run()
	{
		System.out.println("Lancement du traitement de la connexion cliente");
		String[] message;
		while(!soc.isClosed())													//Tant que la connexion est active, on traite les demandes
		{
			try
			{
				writer = new PrintWriter(soc.getOutputStream());
				reader = new BufferedInputStream(soc.getInputStream());
				//BufferedOutputStream writer2 = new BufferedOutputStream(soc.getOutputStream());
				message = receive();											//On lit la demande du client
				debug(message);													//On affiche quelques informations
				handling(message);												//On traite la requête
				try{sleep(TIME);}
				catch(InterruptedException e){System.err.println("Connexion.run : InterruptedException");}
			}
			catch(SocketException | StringIndexOutOfBoundsException e)
			{
				System.err.println("User : " + name + ". La connexion a été interrompue : "+e.getMessage());
				if(room != null)
					room.delUser(this);
				JavaServer.delUser(this);
				break;
			}
			catch(IOException e)
			{
				System.err.println(e.getMessage());
				if(room != null)
					room.delUser(this);
				JavaServer.delUser(this);
				break;
			}
		}
		if(room != null)
			room.delUser(this);
		JavaServer.delUser(this);
	}
	
	//Information de debuggage
	private void debug(String[] mes)
	{
		if(!JavaServer.DEBUG) return;
		String debug;
		InetSocketAddress remote;
		remote = (InetSocketAddress) soc.getRemoteSocketAddress();
		debug = "    Thread : " + Thread.currentThread().getName() + ".";
		debug += "Demande de l'adresse : " + remote.getAddress().getHostAddress() + ".";
		debug += " Sur le port : " + remote.getPort() + ".";
		debug += "-> Commande reçue : " + mes[0] + "#" + mes[1];
		if(!mes[0].equals(FLAGS[2]) && !mes[1].equals(" "))
			System.out.println(debug);
	}
	
	//Traitement du message reçu, et génération de la réponse à envoyer
	private void handling(String[] mes) throws IOException
	{
		JavaServer js = JavaServer.getInstance();
		switch(mes[0])
		{
			case "SYN":
				name = mes[1];
				send(FLAGS[0]+S+(js.getUsers().size()-1));
				JavaServer.addUser(this);
				break;
			case "ROOM":
				if(room != null)
					room.delUser(this);
				int r = Integer.parseInt(mes[1]);
				room = js.getRooms()[r];
				room.addUser(this);												//Ajoute l'utilisateur au salon
				String[] toSend = IO.readString(room.getFilePath()).split("\n");
				int i = 0;
				if(toSend.length-i > MAX_MES)
					i = toSend.length - MAX_MES;
				while(i < toSend.length){
					if(toSend[i].equals("")) 
					{
						i++;
						continue;
					}
					addMessage(FLAGS[3]+S+toSend[i]);
					i++;
				}
				addMessage(FLAGS[3]+S+room.getUsers().size());
				send(FLAGS[2]+S+" ");
				break;
			case "ACK":
				send(FLAGS[2]+S+" ");
				break;
			case "TXT":
				if(room != null)
				{
					if(!mes[1].equals(""))
					{
						String message = formatMessage(mes[1]);
						String conversation = IO.readString(room.getFilePath());
						if(!"".equals(conversation))
							conversation += "\n"+message;
						else
							conversation = message;
						IO.writeString(room.getFilePath(), conversation);
						room.send(this, message);								//Envoie le message dans la room
						send(FLAGS[3]+S+message);
					}
					else
						send(FLAGS[2]+S+" ");
				}
				break;
			case "FILE":
				if(room != null)
				{
					send(FLAGS[2]+S+" ");
				}
				break;
			case "FIN":
				if(room != null)
					room.delUser(this);											//Supprime l'utilisateur du salon
				JavaServer.delUser(this);										//Supprime l'utilisateur du serveur
				send(FLAGS[2]+S+" ");
				close();
				break;
		}
	}
	
	//Envoie un message à l'utilisateur
	private void send(String mes)
	{
		//System.out.println("To send : "+mes);
		synchronized(buffer)
		{
			if(mes.equals(FLAGS[2]+S+" ") && !buffer.isEmpty())					//Si on n'a rien d'autre à envoyer qu'un ACK et qu'il y a des messages en attente, on envoie le plus vieux message
				mes = buffer.remove(0);
		}
		writer.write(mes);														//On envoie la réponse au client
		writer.flush();
	}
	
	
	//Lit la réponse reçue par l'utilisateur
	private String[] receive() throws IOException
    {
        String response;
        int stream;
        byte[] b = new byte[4096];
        stream = reader.read(b);
        response = new String(b, 0, stream);
        return response.split(S);
    }
	
	//Ferme la connexion de l'utilisateur
	private void close()														//Arrête les flux et supprime la session
    {
        writer.close();
    }
	
	//Ajoute dans le buffer un message à envoyer à l'utilisateur
	public void addMessage(String mes)
	{
		synchronized(buffer){buffer.add(mes);}
	}
	
	public String formatMessage(String mes)
    {   
        String heure = "" + c.get(Calendar.HOUR_OF_DAY);
        if (heure.length() == 1){heure = "0" + heure;}
        
        String minute = "" + c.get(Calendar.MINUTE);
        if (minute.length() == 1){minute = "0" + minute;}
        
        String seconde = "" + c.get(Calendar.SECOND);
        if (seconde.length() == 1){seconde = "0" + seconde;}
        
        mes = name + " (" + heure + ":" + minute + ":" + seconde + "): " + mes;
        return mes;
    }
}