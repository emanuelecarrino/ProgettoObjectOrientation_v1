package dao.implement;

import dao.interf.ModConsegnaDAOinterf;
import dto.DB_Connection;
import dto.ModConsegnaDTO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ModConsegnaDAO implements ModConsegnaDAOinterf {
    
     private Connection getConnection() throws SQLException {
        return DB_Connection.getConnection();
    }


    @Override
    public void insertModConsegna(ModConsegnaDTO consegna) throws SQLException {

        String sql = """ 
            INSERT INTO ModConsegna 
            (ID_Consegna, FK_Annuncio, sedeUni, note, fasciaOraria, data)
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



    @Override
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
                String ID_Annuncio = rs.getString("FK_Annuncio");
                String sedeUni = rs.getString("sedeUni");
                String note = rs.getString("note");
                String fasciaOraria = rs.getString("fasciaOraria");
                LocalDate data = rs.getDate("data").toLocalDate();
                return new ModConsegnaDTO(ID_Consegna, ID_Annuncio, sedeUni, note, fasciaOraria, data);
            }
        }
    }



    
    @Override
    public ModConsegnaDTO getConsegnaByAnnuncio(String ID_Annuncio) throws SQLException{

        String sql = """
                
            SELECT *
            FROM ModConsegna
            WHERE FK_Annuncio = ?
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


    @Override
    public boolean updateModConsegna(ModConsegnaDTO consegna) throws SQLException {
        String sql = """
            UPDATE ModConsegna
            SET FK_Annuncio = ?, sedeUni = ?, note = ?, fasciaOraria = ?, data = ?
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

    
    @Override
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



    @Override
    public String getUltimoIdConsegna() throws SQLException {
        String sql = """
            SELECT ID_Consegna
            FROM ModConsegna
            ORDER BY ID_Consegna DESC
            LIMIT 1
            """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("ID_Consegna");
            }
            return null;
        }
    }


}
