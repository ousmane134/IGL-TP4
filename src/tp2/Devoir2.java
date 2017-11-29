// Travail fait par :
//   NomEquipier1 - Matricule
//   NomEquipier2 - Matricule

package tp2;

import java.io.*;
import java.util.StringTokenizer;
import java.sql.*;

/**
 * Fichier de base pour le TP2 du cours IFT287
 *
 * <pre>
 * 
 * Vincent Ducharme
 * Universite de Sherbrooke
 * Version 1.0 - 7 juillet 2016
 * IFT287 - Exploitation de BD relationnelles et OO
 * 
 * Ce programme permet d'appeler des transactions d'un systeme
 * de gestion utilisant une base de donnees.
 *
 * Paramètres du programme
 * 0- site du serveur SQL ("local" ou "dinf")
 * 1- nom de la BD
 * 2- user id pour etablir une connexion avec le serveur SQL
 * 3- mot de passe pour le user id
 * 4- fichier de transaction [optionnel]
 *           si non spécifié, les transactions sont lues au
 *           clavier (System.in)
 *
 * Pré-condition
 *   - La base de donnees doit exister
 *
 * Post-condition
 *   - Le programme effectue les mises à jour associees à chaque
 *     transaction
 * </pre>
 */
public class Devoir2
{
    private static Connexion cx;
    
    private static PreparedStatement stmtExisteParticipant;
    private static PreparedStatement stmtAjoutParticipant;
    private static PreparedStatement stmtSupprimerParticipant;
    private static PreparedStatement stmtExisteJoueur2;

    private static PreparedStatement stmtExisteJoueur;
    private static PreparedStatement stmtAjoutJoueur;
    private static PreparedStatement stmtSupprimerJoueur;
    private static PreparedStatement stmtAfficherEquipe;

    private static PreparedStatement stmtExisteEquipe;
    private static PreparedStatement stmtAjoutEquipe;
    private static PreparedStatement stmtAfficherEquipes;
    private static PreparedStatement stmtSupprimerEquipes;
    
    private static PreparedStatement stmtExisteLigue;
    private static PreparedStatement stmtAjoutLigue;
    private static PreparedStatement stmtAfficherLigue;
    private static PreparedStatement stmtSupprimerLigue;
    private static PreparedStatement stmtJoueurEquipe;
    
    private static PreparedStatement stmtExisteResultat;
    private static PreparedStatement stmtAjoutResultat;
    
    
    private static int nombreResultats;
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length < 4)
        {
            System.out.println("Usage: java tp2.Devoir2 <serveur> <bd> <user> <password> [<fichier-transactions>]");
            return;
        }
        
        cx = null;
        
        try
        {
            cx = new Connexion(args[0], args[1], args[2], args[3]);
            initialiserStatements();
            nombreResultats = 0;
            BufferedReader reader = ouvrirFichier(args);
            String transaction = lireTransaction(reader);
            while (!finTransaction(transaction))
            {
                executerTransaction(transaction);
                transaction = lireTransaction(reader);
            }
        }
        finally
        {
            if (cx != null)
                cx.fermer();
        }
    }
    private static void initialiserStatements()
    		throws SQLException
    {
    	stmtExisteParticipant = cx.getConnection().prepareStatement("SELECT matricule, age, nom, prenom FROM Participant WHERE matricule = ?");
    	stmtAjoutParticipant = cx.getConnection().prepareStatement("INSERT INTO Participant (matricule, age, nom, prenom) VALUES (?,?,?,?)");
        stmtSupprimerParticipant = cx.getConnection().prepareStatement("DELETE FROM Participant WHERE matricule = ?");
        stmtExisteJoueur2 = cx.getConnection().prepareStatement("SELECT nomEquipe, matricule FROM Joueur WHERE matricule = ?");
        
        stmtExisteJoueur = cx.getConnection().prepareStatement("SELECT nomEquipe, matricule FROM Joueur WHERE nomEquipe = ? AND matricule = ?");
        stmtAjoutJoueur = cx.getConnection().prepareStatement("INSERT INTO Joueur (nomEquipe, matricule) VALUES (?,?)");
        stmtSupprimerJoueur = cx.getConnection().prepareStatement("DELETE FROM Joueur WHERE nomEquipe = ? AND matricule = ?");
        stmtAfficherEquipe = cx.getConnection().prepareStatement("SELECT * FROM Joueur WHERE nomEquipe = ?");

        stmtExisteEquipe = cx.getConnection().prepareStatement("SELECT nomEquipe, NbreJoueurMax, nomLigue FROM Equipe WHERE nomEquipe = ?");
        stmtAjoutEquipe = cx.getConnection().prepareStatement("INSERT INTO Equipe (nomEquipe, NbreJoueurMax, nomLigue) VALUES (?,?,?)");
        stmtAfficherEquipes = cx.getConnection().prepareStatement("SELECT * FROM Equipe");
        stmtSupprimerEquipes = cx.getConnection().prepareStatement("DELETE FROM Equipe WHERE nomLigue = ?");
        
        stmtExisteLigue = cx.getConnection().prepareStatement("SELECT nomLigue FROM Ligue WHERE nomLigue = ?");
        stmtAjoutLigue = cx.getConnection().prepareStatement("INSERT INTO Ligue (nomLigue) VALUES (?)");
        stmtAfficherLigue = cx.getConnection().prepareStatement("SELECT * FROM Equipe WHERE nomLigue = ?");
        stmtSupprimerLigue= cx.getConnection().prepareStatement("DELETE FROM Ligue WHERE nomLigue = ?");
        stmtJoueurEquipe = cx.getConnection().prepareStatement("SELECT Joueur.matricule FROM Equipe JOIN Joueur on (Equipe.nomEquipe = Joueur.nomEquipe) WHERE Equipe.nomLigue = ?");
        
        stmtExisteResultat = cx.getConnection().prepareStatement("SELECT Id, scoreEquipeA, scoreEquipeB, nomEquipeA, nomEquipeB FROM Resultat WHERE Id = ?" );
        stmtAjoutResultat = cx.getConnection().prepareStatement("INSERT INTO Resultat (Id, scoreEquipeA, scoreEquipeB, nomEquipeA, nomEquipeB) VALUES (?,?,?,?,?)");
    }

    /**
     * Decodage et traitement d'une transaction
     */
    static void executerTransaction(String transaction) throws Exception, IFT287Exception
    {
        try
        {
            System.out.print(transaction);
            // Decoupage de la transaction en mots
            StringTokenizer tokenizer = new StringTokenizer(transaction, " ");
            if (tokenizer.hasMoreTokens())
            {
                String command = tokenizer.nextToken();
                
                if (command.equals("ajoutParticipant"))
                {
                	String param1 = readString(tokenizer);
                    String param2 = readString(tokenizer);
                    int param3 = readInt(tokenizer);
                    String param4 = readString(tokenizer);
                    
                    ajoutParticipant(param1, param2, param3, param4);
                }
                else if (command.equals("supprimerParticipant"))
                {
                	String param1 = readString(tokenizer);
                	supprimerParticipant(param1);
                }
                else if (command.equals("ajoutLigue"))
                {
                	String param1 = readString(tokenizer);
                	ajoutLigue(param1);
                }
                else if (command.equals("supprimerLigue"))
                {
                	String param1 = readString(tokenizer);
                	supprimerLigue(param1);
                }
                else if (command.equals("ajoutEquipe"))
                {
                	String param1 = readString(tokenizer);
                	String param2 = readString(tokenizer);
                	int param3 = readInt(tokenizer);
                	ajoutEquipe(param1, param2, param3);
                }
                else if (command.equals("ajoutJoueur"))
                {
                	String param1 = readString(tokenizer);
                	String param2 = readString(tokenizer);
                	ajoutJoueur(param1, param2);
                }
                else if (command.equals("supprimerJoueur"))
                {
                	String param1 = readString(tokenizer);
                	String param2 = readString(tokenizer);
                	supprimerJoueur(param1, param2);
                }
                else if (command.equals("ajoutResultat"))
                {
                	nombreResultats++;
                	String param1 = readString(tokenizer);
                	int param2 = readInt(tokenizer);
                	String param3 = readString(tokenizer);
                	int param4 = readInt(tokenizer);
                	ajoutResultat(param1, param2, param3, param4);
                }
                else if (command.equals("afficheEquipe"))
                {
                	String param1 = readString(tokenizer);
                	afficheEquipe(param1);
                }
                else if (command.equals("afficheLigue"))
                {
                	String param1 = readString(tokenizer);
                	afficheLigue(param1);
                }
                else if (command.equals("afficheEquipes"))
                {
                	afficheEquipes();
                }
                else
                {
                    System.out.println("Transaction non reconnue");
                }
            }
        }
        catch (SQLException e)
        {
            System.out.println("** Erreur SQL - Ne devrait arriver que s'il y a une perte de connexion avec la BD.** \n" + e.toString());
        }
        catch (IFT287Exception e)
        {
            System.out.println("** " + e.toString());
            cx.rollback();
        }
    }

    public static void ajoutParticipant(String param1, String param2, int param3, String param4)
            throws SQLException, IFT287Exception
    {
    	try
        {
            stmtExisteParticipant.setString(1, param1);
            ResultSet rsetParticipant = stmtExisteParticipant.executeQuery();
            if (rsetParticipant.next())
            {
            	rsetParticipant.close();
                throw new IFT287Exception("Participant existe deja: " + param4);
            }
            rsetParticipant.close();

            stmtAjoutParticipant.setString(1, param4);
            stmtAjoutParticipant.setInt(2, param3);
            stmtAjoutParticipant.setString(3, param2);
            stmtAjoutParticipant.setString(4, param1);
            stmtAjoutParticipant.executeUpdate();
            
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }
    public static void supprimerParticipant(String param1)
            throws SQLException, IFT287Exception
    {
    	try
    	{
    		stmtExisteJoueur2.setString(1, param1);
    		ResultSet rsetJoueur = stmtExisteJoueur2.executeQuery();
    		if (!rsetJoueur.next()){
    			rsetJoueur.close();
    			stmtSupprimerParticipant.setString(1, param1);
        		
        		if(stmtSupprimerParticipant.executeUpdate() == 0)
        			throw new IFT287Exception("Participant: "+ param1 + " n'existe pas.");
        		
        		cx.commit();
    		}
    		else{
    			rsetJoueur.close();
    			throw new IFT287Exception("Participant: "+ param1 + " existe toujours dans la table Joueur. Supprimez le de cette table d'abord.");
    		}
    	}

    catch(Exception e){
    	cx.rollback();
    	throw e;
    }
    }
    public static void ajoutLigue(String param1)
            throws SQLException, IFT287Exception
    {
    	try
        {
            stmtExisteLigue.setString(1, param1);
            ResultSet rsetLigue = stmtExisteLigue.executeQuery();
            if (rsetLigue.next())
            {
            	rsetLigue.close();
                throw new IFT287Exception("Ligue existe deja: " + param1);
            }
            rsetLigue.close();

            stmtAjoutLigue.setString(1, param1);
            stmtAjoutLigue.executeUpdate();
            
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }
    public static void supprimerLigue(String param1)
            throws SQLException, IFT287Exception
    {
    	try
    	{
    		stmtJoueurEquipe.setString(1, param1);
    		ResultSet rsetEquipe = stmtJoueurEquipe.executeQuery();
    		if(!rsetEquipe.next()){
    			
    			rsetEquipe.close();
    			
    			stmtSupprimerEquipes.setString(1, param1);
    			stmtSupprimerEquipes.executeUpdate();
        		
    			stmtSupprimerLigue.setString(1, param1);
        		
        		if(stmtSupprimerLigue.executeUpdate() == 0)
        			throw new IFT287Exception("Ligue: "+ param1 + " n'existe pas.");
        		
        		cx.commit();
    		}
    		else{
    			rsetEquipe.close();
    			throw new IFT287Exception("Ligue: "+ param1 + " contient toujours des joueurs faisant partis de cette ligue. Supprimez les de la table Joueur d'abord.");
    		}
    		
    		
    	}

    catch(Exception e){
    	cx.rollback();
    	throw e;
    }
    }
    public static void ajoutEquipe(String param1, String param2, int param3)
            throws SQLException, IFT287Exception
    {
    	try
        {
            stmtExisteEquipe.setString(1, param1);
            ResultSet rsetEquipe = stmtExisteEquipe.executeQuery();
            if (rsetEquipe.next())
            {
            	rsetEquipe.close();
                throw new IFT287Exception("Equipe existe deja: " + param2);
            }
            rsetEquipe.close();

            stmtAjoutEquipe.setString(1, param2);
            stmtAjoutEquipe.setInt(2, param3);
            stmtAjoutEquipe.setString(3, param1);
            stmtAjoutEquipe.executeUpdate();
            
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }
    public static void ajoutJoueur(String param1, String param2)
            throws SQLException, IFT287Exception
    {
    	try
        {
            stmtExisteJoueur.setString(1, param1);
            stmtExisteJoueur.setString(2, param2);
            ResultSet rsetJoueur = stmtExisteJoueur.executeQuery();
            if (rsetJoueur.next())
            {
            	rsetJoueur.close();
                throw new IFT287Exception("Joueur existe deja: " + param2);
            }
            rsetJoueur.close();

            stmtAjoutJoueur.setString(1, param1);
            stmtAjoutJoueur.setString(2, param2);
            stmtAjoutJoueur.executeUpdate();
            
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }
    public static void supprimerJoueur(String param1, String param2)
            throws SQLException, IFT287Exception
    {
    	try
    	{
    		stmtSupprimerJoueur.setString(1, param1);
    		stmtSupprimerJoueur.setString(2, param2);
    		
    		if(stmtSupprimerJoueur.executeUpdate() == 0)
    			throw new IFT287Exception("Joueur: "+ param1 + " n'existe pas.");
    		
    		cx.commit();
    	}

    catch(Exception e){
    	cx.rollback();
    	throw e;
    }
    }
    public static void ajoutResultat(String param1, int param2, String param3, int param4)
            throws SQLException, IFT287Exception
    {
    	try
        {
            stmtExisteResultat.setInt(1, nombreResultats);
            ResultSet rsetResultat = stmtExisteResultat.executeQuery();
            if (rsetResultat.next())
            {
            	rsetResultat.close();
                throw new IFT287Exception("Resultat existe deja: " + param2);
            }
            rsetResultat.close();

            stmtAjoutResultat.setInt(1, nombreResultats);
            stmtAjoutResultat.setInt(2, param2);
            stmtAjoutResultat.setInt(3, param4);
            stmtAjoutResultat.setString(4, param1);
            stmtAjoutResultat.setString(5, param3);
            stmtAjoutResultat.executeUpdate();
            
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }
    public static void afficheEquipe(String param1)
            throws SQLException, IFT287Exception
    {
    	stmtAfficherEquipe.setString(1, param1);
    	ResultSet rset = stmtAfficherEquipe.executeQuery();
    	
    	System.out.println("Joueurs de l'équipe: "+ param1);
    	
    	while(rset.next()){
    		System.out.println(rset.getString("matricule"));
    	}
    	rset.close();
        cx.commit();    	
    }
    public static void afficheLigue(String param1)
            throws SQLException, IFT287Exception
    {
    	stmtAfficherLigue.setString(1, param1);
    	ResultSet rset = stmtAfficherLigue.executeQuery();
    	
    	System.out.println("nomLigue");
    	
    	while(rset.next()){
    		System.out.println(rset.getString("nomLigue")+ " " + rset.getString("nomEquipe") + " " + rset.getInt("NbreJoueurMax"));
    	}
    	rset.close();
        cx.commit();
    }
    public static void afficheEquipes()
            throws SQLException, IFT287Exception
    {
    	ResultSet rset = stmtAfficherEquipes.executeQuery();
    	System.out.println("NomEquipe	NbreJoueursMax	Ligue");
    	
    	while(rset.next()){
    		System.out.println(rset.getString("nomEquipe") + " " + rset.getInt("NbreJoueurMax") + " " + rset.getString("nomLigue"));
    	}
    	rset.close();
        cx.commit();
    }
   
    /*public static void effectuerUneTransaction(String param1, Date param2, int param3)
            throws SQLException, IFT287Exception
    {

    }*/

    // ****************************************************************
    // *   Les methodes suivantes n'ont pas besoin d'etre modifiees   *
    // ****************************************************************

    /**
     * Ouvre le fichier de transaction, ou lit à partir de System.in
     */
    public static BufferedReader ouvrirFichier(String[] args) throws FileNotFoundException
    {
        if (args.length < 5)
            // Lecture au clavier
            return new BufferedReader(new InputStreamReader(System.in));
        else
            // Lecture dans le fichier passe en parametre
            return new BufferedReader(new InputStreamReader(new FileInputStream(args[4])));
    }

    /**
     * Lecture d'une transaction
     */
    static String lireTransaction(BufferedReader reader) throws IOException
    {
        return reader.readLine();
    }

    /**
     * Verifie si la fin du traitement des transactions est atteinte.
     */
    static boolean finTransaction(String transaction)
    {
        // fin de fichier atteinte
        return (transaction == null || transaction.equals("quitter"));
    }

    /** Lecture d'une chaine de caracteres de la transaction entree a l'ecran */
    static String readString(StringTokenizer tokenizer) throws Exception
    {
        if (tokenizer.hasMoreElements())
            return tokenizer.nextToken();
        else
            throw new Exception("Autre parametre attendu");
    }

    /**
     * Lecture d'un int java de la transaction entree a l'ecran
     */
    static int readInt(StringTokenizer tokenizer) throws Exception
    {
        if (tokenizer.hasMoreElements())
        {
            String token = tokenizer.nextToken();
            try
            {
                return Integer.valueOf(token).intValue();
            }
            catch (NumberFormatException e)
            {
                throw new Exception("Nombre attendu a la place de \"" + token + "\"");
            }
        }
        else
            throw new Exception("Autre parametre attendu");
    }

    static Date readDate(StringTokenizer tokenizer) throws Exception
    {
        if (tokenizer.hasMoreElements())
        {
            String token = tokenizer.nextToken();
            try
            {
                return Date.valueOf(token);
            }
            catch (IllegalArgumentException e)
            {
                throw new Exception("Date dans un format invalide - \"" + token + "\"");
            }
        }
        else
            throw new Exception("Autre parametre attendu");
    }

}
