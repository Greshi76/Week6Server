package encrptserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;

/**
 *
 * @author Wayne Alden
 * Q9504941
 */
public class Server {
    
    public static void main(String[] args) {
        ServerSocket serverSocket;
        int serverPort = 9999;
        
        try {
            
            serverSocket = new ServerSocket(serverPort);
            
            while(true) {
                Socket clientSocket = serverSocket.accept();
                Connection c = new Connection(clientSocket);
                c.start();
            }            

        } catch (Exception e) {
        }
    }
}

class Connection extends Thread {
    
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    KeyPairGenerator keyPairGenerator;
    KeyPair keyPair;
    PublicKey pubKey;
    PrivateKey privateKey;
    
    public Connection(Socket aClientSocket) {
        
        
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.genKeyPair();
            pubKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
            
            
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            
            
          
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        String data;
       
        try {
            byte[] bytesPubKey = pubKey.getEncoded();
            byte[] bytesPword = null;
            System.out.println("Public Key size in bytes" + bytesPubKey.length);
            
            while ((data = in.readUTF()) != null) {
                System.out.println("Message from client: " + data);
                if(data.startsWith("Hello"))
                    out.writeUTF("Hello");
                if(data.equalsIgnoreCase("Key")){
                    System.out.println(bytesPubKey.length);
                    out.writeInt(bytesPubKey.length);
                    System.out.println(bytesPubKey);
                    out.write(bytesPubKey, 0, bytesPubKey.length);
                }
                if(data.equalsIgnoreCase("password")){
                    int passwordLength = in.readInt();
                    bytesPword = new byte[passwordLength];
                    System.out.println("Encrypted Length: " + passwordLength);
                    in.readFully(bytesPword, 0, passwordLength);
                    System.out.println(new String(decrypt(privateKey, bytesPword)));
                    
                }
                if(data.equalsIgnoreCase("finished"))
                    break;
                
            }
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static byte [] encrypt(PublicKey publicKey, byte [] Message) throws Exception {
        
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(Message);
    }
    
    public static byte [] decrypt(PrivateKey privateKey, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encrypted);
    }
    
}
