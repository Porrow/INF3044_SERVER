package javaserver.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class IO {

	public static void writeString(String file, String data) {
		File f = new File(file);
		FileWriter fw;
		FileReader fr;
		try {
			fw = new FileWriter(f);
			fw.write(data);
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String readString(String file) {
		File f = new File(file);
		FileReader fr;
		String data = "";
		try {
			fr = new FileReader(f);
			int i = 0;
			while ((i = fr.read()) != -1)
				data += (char) i;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	/*try 
        {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
		for (int i = 0; i < data.length; i++) {
			dos.write(data[i]);
		}
		dos.close();
	}
	catch (IOException ex

	
		) {Print.Wri.println("Erreur");
	}*/
}
