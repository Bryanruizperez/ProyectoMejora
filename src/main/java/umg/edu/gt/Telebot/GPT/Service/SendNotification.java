package umg.edu.gt.Telebot.GPT.Service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import umg.edu.gt.Telebot.GPT.Model.Client;

import java.sql.SQLException;


@Service
public class SendNotification {
    private static final Logger logger = LoggerFactory.getLogger(SendNotification.class);

    // Notificación al inicializar la caché
    public void notifyCacheInitialization() {
        logger.info("Inicializando caché de clientes...");
    }

    // Notificación cuando se accede a la caché para obtener un cliente
    public void notifyCacheHit(Long chatId) {
        logger.info("Cache hit: cliente encontrado en caché para chat ID {}", chatId);
    }

    // Notificación cuando el cliente no está en caché y se accede a la base de datos
    public void notifyCacheMiss(Long chatId) {
        logger.info("Cache miss: cliente no encontrado en caché para chat ID {}. Consultando base de datos...", chatId);
    }

    // Notificación cuando se almacena un cliente en caché
    public void notifyClientCached(Client client) {
        logger.info("Cliente con ID {} y nombre '{}' almacenado en caché.", client.getClientId(), client.getName());
    }

    // Notificación cuando se elimina un cliente de la caché
    public void notifyCacheEviction(Long chatId) {
        logger.info("Eliminando caché del cliente con chat ID {}", chatId);
    }
}