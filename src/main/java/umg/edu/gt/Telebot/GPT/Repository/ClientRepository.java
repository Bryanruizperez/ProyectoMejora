package umg.edu.gt.Telebot.GPT.Repository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import umg.edu.gt.Telebot.GPT.Model.Client;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


@Repository
public class ClientRepository {
    private static final Logger log = LoggerFactory.getLogger(ClientRepository.class);
    private static final String TABLENAME = "clients";
    private static DataSource dataSource;

    @Autowired  // Inyección automática del DataSource gestionado por Spring Boot
    public ClientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    // metodo para agregar un nuevo cliente
    public void add(@NotNull @Size(min=1,max = 100) String name,@NotNull Long chat_id) throws SQLException {
        if(name == null || name.trim().isEmpty() || chat_id == null ){
            throw new IllegalArgumentException("invalid input");
        }
        String sql = "INSERT INTO " + TABLENAME + " (name, chat_id) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);          // Vincula el nombre
            stmt.setLong(2, chat_id);         // Vincula el chat_id como Long
            stmt.executeUpdate();             // Ejecuta la consulta de inserción
        }
    }
    // metodo para obtener un cliente por ID
    public Client getById(Long chat_id) throws SQLException {
        String sql = "SELECT * FROM " + TABLENAME + " WHERE chat_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setLong(1, chat_id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Client client = new Client(rs.getLong("chat_id"), rs.getString("name"));
                log.info("client retrieved succesfully: {}",client);
                return client;
            }
        }
        log.warn("client not found with  Chat ID = {}",chat_id);
        return null;  // Si no se encuentra el cliente
    }
}