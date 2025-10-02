package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import dto.DB_Connection;
import dto.ModConsegnaDTO;

public class ModConsegnaDAO {
    
     private Connection getConnection() throws SQLException {
        return DB_Connection.getConnection();
    }


    public void insertModConsegna(ModConsegnaDTO consegna) throws SQLException {

        String sql = """ 
            INSERT INTO ModConsegna 
            (ID_Consegna, ID_Annuncio, sedeUni, note, fasciaOraria, data)
            VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, consegna.getIdConsegna());
            ps.setString(2, consegna.getIdAnnuncio());
            ps.setString(3, consegna.getSedeUni());
            ps.setString(4, consegna.getNote());
            ps.setString(5,consegna.getFasciaOraria());
            ps.setDate(6, Date.valueOf(consegna.getData()));

            ps.executeUpdate();
        }
    }



    public ModConsegnaDTO getConsegnaById(String ID_Consegna) throws SQLException{

        String sql = """
                
            SELECT *
            FROM ModConsegna
            WHERE ID_Consegna = ?
                """;

        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, ID_Consegna);
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()) return null;
                String ID_Annuncio = rs.getString("ID_Annuncio");
                String sedeUni = rs.getString("sedeUni");
                String note = rs.getString("note");
                String fasciaOraria = rs.getString("fasciaOraria");
                LocalDate data = rs.getDate("data").toLocalDate();
                return new ModConsegnaDTO(ID_Consegna, ID_Annuncio, sedeUni, note, fasciaOraria, data);
            }
        }
    }



    
    public ModConsegnaDTO getConsegnaByAnnuncio(String ID_Annuncio) throws SQLException{

        String sql = """
                
            SELECT *
            FROM ModConsegna
            WHERE ID_Annuncio = ?
                """;

        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, ID_Annuncio);
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()) return null;
                String ID_Consegna = rs.getString("ID_Consegna");
                String sedeUni = rs.getString("sedeUni");
                String note = rs.getString("note");
                String fasciaOraria = rs.getString("fasciaOraria");
                LocalDate data = rs.getDate("data").toLocalDate();
                return new ModConsegnaDTO(ID_Consegna, ID_Annuncio, sedeUni, note, fasciaOraria, data);
            }
        }
    }


    public boolean updateModConsegna(ModConsegnaDTO consegna) throws SQLException {
        String sql = """
            UPDATE ModConsegna
            SET ID_Annuncio = ?, sedeUni = ?, note = ?, fasciaOraria = ?, data = ?
            WHERE ID_Consegna = ?
            """;
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, consegna.getIdAnnuncio());
            ps.setString(2, consegna.getSedeUni());
            ps.setString(3, consegna.getNote());
            ps.setString(4, consegna.getFasciaOraria());
            ps.setDate(5, Date.valueOf(consegna.getData()));
            ps.setString(6, consegna.getIdConsegna());
            return ps.executeUpdate() > 0;
        }
    }

    // Eliminazione per ID; true se cancellata almeno una riga
    public boolean deleteModConsegnaById(String ID_Consegna) throws SQLException {
        String sql = """
            DELETE FROM ModConsegna
            WHERE ID_Consegna = ?
            """;
        try(Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, ID_Consegna);
            return ps.executeUpdate() > 0;
        }
    }



}
