package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UtenteDAO {
    
    public UtenteDTO getUtenteByMatricola(String matricola){
        Connection con = DB_Connection.getConnection();
        String query = "SELECT * FROM Utente WHERE id=?"
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, matricola);
        ResultSet rs = ps.executeQuery();

        if(rs.next()){
            return new UtenteDTO(rs.getString("matricola"), rs.getString("nome"), rs.getString("email"));
        }
    }

}
