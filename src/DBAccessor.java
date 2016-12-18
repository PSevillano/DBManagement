import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DBAccessor {
	private String dbname;
	private String host;
	private String port;
	private String user;
	private String passwd;
	private String schema;
	Connection conn = null;

	/**
	 * Initializes the class loading the database properties file and assigns
	 * values to the instance variables.
	 * 
	 * @throws RuntimeException
	 *             Properties file could not be found.
	 */
	public void init() {
		Properties prop = new Properties();
		InputStream propStream = this.getClass().getClassLoader().getResourceAsStream("db.properties");

		try {
			prop.load(propStream);
			this.host = prop.getProperty("host");
			this.port = prop.getProperty("port");
			this.dbname = prop.getProperty("dbname");
			this.schema = prop.getProperty("schema");
		} catch (IOException e) {
			String message = "ERROR: db.properties file could not be found";
			System.err.println(message);
			throw new RuntimeException(message, e);
		}
	}

	/**
	 * Obtains a {@link Connection} to the database, based on the values of the
	 * <code>db.properties</code> file.
	 * 
	 * @return DB connection or null if a problem occurred when trying to
	 *         connect.
	 */
	public Connection getConnection(Identity identity) {

		// Implement the DB connection
		String url = null;
		try {
			// Loads the driver
			Class.forName("org.postgresql.Driver");

			// Preprara connexió a la base de dades
			StringBuffer sbUrl = new StringBuffer();
			sbUrl.append("jdbc:postgresql:");
			if (host != null && !host.equals("")) {
				sbUrl.append("//").append(host);
				if (port != null && !port.equals("")) {
					sbUrl.append(":").append(port);
				}
			}
			sbUrl.append("/").append(dbname);
			url = sbUrl.toString();

			// Utilitza connexió a la base de dades
			conn = DriverManager.getConnection(url, identity.getUser(), identity.getPassword());
			conn.setAutoCommit(false);
		} catch (ClassNotFoundException e1) {
			System.err.println("ERROR: Al Carregar el driver JDBC");
			System.err.println(e1.getMessage());
		} catch (SQLException e2) {
			System.err.println("ERROR: No connectat  a la BD " + url);
			System.err.println(e2.getMessage());
		}

		// Sets the search_path
		if (conn != null) {
			Statement statement = null;
			try {
				statement = conn.createStatement();
				statement.executeUpdate("SET search_path TO " + this.schema);
				// missatge de prova: verificació
				System.out.println("OK: connectat a l'esquema " + this.schema + " de la base de dades " + url
						+ " usuari: " + user + " password:" + passwd);
				System.out.println();
				//
			} catch (SQLException e) {
				System.err.println("ERROR: Unable to set search_path");
				System.err.println(e.getMessage());
			} finally {
				try {
					statement.close();
				} catch (SQLException e) {
					System.err.println("ERROR: Closing statement");
					System.err.println(e.getMessage());
				}
			}
		}

		return conn;
	}



	public void altaAutor() throws SQLException, IOException {
		try {

			Scanner sc = new Scanner(System.in);
			System.out.println("Introduce el id: ");
			int id = sc.nextInt();
			sc.nextLine();
			System.out.println("Introduce el nombre: ");
			String nom = sc.nextLine();
			System.out.println("Introduce la fecha de nacimiento: ");
			String fecha = sc.nextLine();
			//Date date = df.parse(fecha);
			System.out.println("Introduce la nacionalidad: ");
			String nacionalidad = sc.nextLine();
			System.out.println("Activo? Si(S) No(N): ");
			String activo = sc.nextLine();
			
			Statement st = conn.createStatement();
			st.executeUpdate("INSERT INTO autors(id_autor,nom,any_naixement,nacionalitat,actiu)VALUES('"+id+"','"+nom+"','"+fecha+"','"+nacionalidad+"','"+activo+"')");
			sc.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	// FET

	public void altaRevista() throws SQLException, NumberFormatException, IOException, ParseException {

		try {

			// FET 
			SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd");
			Scanner sc = new Scanner(System.in);
			System.out.println("Introduce el id: ");
			int id = sc.nextInt();
			sc.nextLine();
			System.out.println("Introduce el titulo: ");
			String nom = sc.nextLine();
			System.out.println("Introduce la fecha de publoicacion: ");
			String fecha = sc.nextLine();
			Date date = df.parse(fecha);
			
			Statement st = conn.createStatement();
			st.executeUpdate("INSERT INTO revistes(id_revista,titol,data_publicacio)VALUES('"+id+"','"+nom+"','"+fecha+"')");
			sc.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}



	public void altaArticle() throws SQLException, NumberFormatException, IOException, ParseException {

		try{
			SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd");
			Scanner sc = new Scanner(System.in);
			System.out.println("Introduce el id del articulo: ");
			int id_a = sc.nextInt();
			System.out.println("Introduce el id de la revista: ");
			int id_r = sc.nextInt();
			System.out.println("Introduce el id del autor: ");
			int id_autor = sc.nextInt();
			sc.nextLine();
			System.out.println("Introduce el titulo: ");
			String nom = sc.nextLine();
			System.out.println("Introduce la fecha de creación: ");
			String fecha_C = sc.nextLine();
			Date date_c = df.parse(fecha_C);
			System.out.println("Introduce la fecha de la ultima publicación: ");
			String fecha = sc.nextLine();
			Date date_p = df.parse(fecha);
			System.out.println("Publicable: ");
			String publi = sc.nextLine();
			
			Statement st = conn.createStatement();
			st.executeUpdate("INSERT INTO  articles (id_article,id_revista,id_autor,titol,data_creacio,ultima_modificacio,publicable) VALUES ('"+
			id_a+"','"+id_r+"','"+id_autor+"','"+nom+"','"+date_c+"','"+date_p+"','"+publi+"')");
			sc.close();
		}catch(SQLException e){
			e.printStackTrace();
		}

	}
	
	public void afegeixArticleARevista(Connection conn) throws SQLException {

		ResultSet rs = null;
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		try {
			rs = st.executeQuery("SELECT * FROM articles WHERE id_revista IS NULL");
			if (rs == null) {
				System.out.println("No hi ha articles pendents d'associar revistes. ");
			} else {
				while (rs.next()) {

					System.out.println("ID articulo: "+rs.getInt(1)+"\tID revista: "+rs.getInt(2)+"\tID autor: "+
					rs.getInt(3)+"\tTitol: "+rs.getString(4)+"\tFecha de creación: "+rs.getDate(5)+
					"\tUltima modificacion: "+rs.getDate(6)+"\tPublicable: "+rs.getString(7));

					System.out.println("Vol incorporar aquest article a una revista?");
					String resposta = br.readLine();

					if (resposta.equals("si")) {
						// TODO demana l'identificador de la revista
						// actualitza el camp
						// actualitza la fila
						System.out.println("Introduce el id:");
						String idRN = br.readLine();
						int id =Integer.parseInt(idRN);
						rs.updateInt("id_revista", id);
						rs.updateRow();

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actualitzarTitolRevistes(Connection conn) throws SQLException {
		
		ResultSet rs = null;
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		try {
			rs = st.executeQuery("SELECT titol FROM revistes");

			while (rs.next()) {
				
				System.out.println("Vol actualitzar el titol d_una revista?");
				String resposta = br.readLine();

				if (resposta.equals("si")) {
					System.out.println("Introduce el titulo nuevo:");
					String titulo = br.readLine();
					rs.updateString("titol", titulo);
					rs.updateRow();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void desassignaArticleARevista(Connection conn) throws SQLException, IOException {
		
		ResultSet rs = null;
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		
		rs = st.executeQuery("SELECT id_revista FROM articles");
		
		while (rs.next()) {
			
			System.out.println("Vol desvincular aquest article a la seva revista?");
			String resposta = br.readLine();

			if (resposta.equals("si")) {
				
				rs.updateNull(2);
				rs.updateRow();
			}
		}

	}

	
	public void mostraAutors() throws SQLException, IOException {

		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM autors");
		while(rs.next()){
			System.out.println("ID: "+rs.getString("id_autor")+"\tNombre: "+rs.getString("nom")+"\tAny de naixement: "+rs.getString("any_naixement")
			+"\tNacionalitat: "+rs.getString("nacionalitat")+"\tActiu: "+rs.getString("actiu"));
		}
	}


	public void mostraRevistes() throws SQLException, IOException {


		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM revistes");
		while(rs.next()){
			System.out.println("ID: "+rs.getString("id_revista")+"\tTitol: "+rs.getString("titol")+"\tData de publicació: "+rs.getString("data_publicacio"));
		}
		
	}


	public void mostraRevistesArticlesAutors() throws SQLException, IOException {

		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT r.titol,a.titol,au.nom FROM revistes r,articles a, autors au WHERE r.id_revista=a.id_revista and a.id_autor=au.id_autor;");
		while(rs.next()){
			System.out.println("Revista: "+rs.getString(1)+"\tArticle: "+rs.getString(2)+"\tAutor: "+rs.getString(3));
		}

	}

	public void sortir() throws SQLException {
		System.out.println("ADÉU!");
		conn.close();
	}
	
	public void carregaAutors() throws SQLException, NumberFormatException, IOException {

		
		String sql = "INSERT INTO  autors (id_autor,nom,any_naixement,nacionalitat,actiu) VALUES (?,?,?,?,?)";
		PreparedStatement pst = conn.prepareStatement(sql);
		
		BufferedReader br = new BufferedReader(new FileReader("autors.csv"));
		StringTokenizer tokken;
		String linea;
		
		while((linea = br.readLine()) != null){
			tokken = new StringTokenizer(linea,",");
			while(tokken.hasMoreTokens()){
				pst.clearParameters();
				pst.setInt(1, Integer.parseInt(tokken.nextToken()));
				pst.setString(2, tokken.nextToken());
				pst.setString(3, tokken.nextToken());
				pst.setString(4, tokken.nextToken());
				pst.setString(5, tokken.nextToken());
				
			}
		}
		
		
	}
}
