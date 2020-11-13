package com.br.test.email;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

public class Email {

    private static String diretorio = "/home/rverli/Downloads/lidos/";

    private static String hostEmail = "smtp.office365.com";
    private static Integer porta = 993;
    private static String protocolo = "imaps";
    private static String usuario = "renanverli@ext.cvccorp.com.br";
    private static String senha = "";
    
    public void execute() throws IOException {

    	Message[] messages = getMessages();
    	
    	for (Message message : messages) {
			
    		Part part1;
			try {
				part1 = getFileAttachment(message);
				
				saveFile(part1);
			} catch (MessagingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
    }

    private static Message[] getMessages() {
    	
    	try {
	    	Properties props = new Properties();
	        Session session = Session.getDefaultInstance(props, null); // get a mail session
	        Store store = session.getStore( protocolo ); // get relevent store type, in this case IMAP 
	        store.connect( hostEmail, porta, usuario, senha ); // connect to mail server
	        Folder inbox = store.getFolder("INBOX"); // find and get INBOX folder
	        inbox.open(Folder.READ_WRITE);  // open the folder
	
	        // search for all "unseen" messages
	        Flags seen = new Flags( Flags.Flag.SEEN );
	        FlagTerm unseenFlagTerm = new FlagTerm( seen, false );
	        Message[] unreadMessages = inbox.search( unseenFlagTerm );
	        
	        //Message[] readMessages = inbox.getMessages();
        
	        return unreadMessages;
    	} catch (FolderClosedException f) {
        	f.printStackTrace();
        } catch (MessagingException m) {
        	m.printStackTrace();
        }
		return null;        
    }
    
    private static void saveFile(
    		String contentType, MimeMultipart multi, Integer indexMultPart) 
    				throws IOException, MessagingException {

        if (!contentType.startsWith("text/plain") && !contentType.startsWith("text/html")) {

        	BodyPart att = multi.getBodyPart( indexMultPart );
        	
            String nomeDoArquivo = att.getFileName();
            
            if ( nomeDoArquivo != null 
            		&& nomeDoArquivo.toUpperCase().contains("PDF") ) {
            	
                InputStream is = att.getInputStream();
                copyInputStreamToFile( is, diretorio + nomeDoArquivo );
            }
        }
    }

    // InputStream -> File
    private static void copyInputStreamToFile(
    		InputStream inputStream, String fileName) throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream( fileName )) {

            int read;
            byte[] bytes = new byte[4 * 1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.close();
        }
    }
    
	
	  private static void moveMensagem(Message message, Folder folderOrigem, Folder folderDestino) throws IOException {
		  
		  try { 
			  message.setFlag(Flags.Flag.SEEN, true); 
			  Message[] mensagemCopia = new Message[1]; 
			  mensagemCopia[0] = message;
			  folderOrigem.copyMessages(mensagemCopia, folderDestino);
			  message.setFlag(Flags.Flag.DELETED, true); 
		  } catch (MessagingException ex) {
			  ex.printStackTrace(); 
		  } 	
	  }
	 

	/*
	 * Metodo responsavel por pegar o anexo do e-mail	
	 */
	public static Part getFileAttachment(Message message) throws MessagingException, IOException{
		if(message.getContent() instanceof Multipart){
			Multipart mp = (Multipart) message.getContent();
			for(int i = 0; i < mp.getCount(); i++){
				Part part = mp.getBodyPart(i);
				if(part.getDisposition() != null && part.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)){
					if(part.getFileName().endsWith("pdf")){
						return part;
					}
				}
			}
		}
		return null;
	}
	
	/*
	 * Metodo responsavel por pegar o corpo do e-mail
	 */
	public String getBody(Message message) throws IOException, MessagingException {
		// verifica se a mensagem veio em partes
		if(message.getContent() instanceof Multipart){
			Multipart mp = (Multipart) message.getContent();
			// percorre as partes da mensagem
			for(int i = 0; i < mp.getCount(); i++){
				Part part = mp.getBodyPart(i);
				// verifica se alguma das partes veio em outras partes, aconte quando vem e-mails com anexo
				if(part.getContent() instanceof MimeMultipart){
					MimeMultipart mm = (MimeMultipart) part.getContent();
					return mm.getBodyPart(0).getContent().toString();
				} else {
					return part.getContent().toString();
				}
			}
		}
		return "";
	}
	
	/**
	 * Método responsável por salvar o xml dentro do computador
	 */
	private static boolean saveFile(Part part) {
		try {
			InputStream is = part.getInputStream();
			File temp = new File("temp");
			if (!temp.exists()) {
				if (!temp.mkdir()) {
					return false;
				}
			}
			FileOutputStream fos = new FileOutputStream( diretorio + "/" + part.getFileName() );
			int x = -1;
			while ((x = is.read()) != -1) {
				fos.write((char) x);
			}
			fos.flush();
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}
}

