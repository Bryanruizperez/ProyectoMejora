package umg.edu.gt.Telebot.GPT.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import umg.edu.gt.Telebot.GPT.Model.Client;
import umg.edu.gt.Telebot.GPT.Service.BotService;

import java.sql.SQLException;
import java.util.Map;

@RestController
public class BotController {

    @Autowired
    private BotService botService;

    // Endpoint para recibir actualizaciones de Telegram
    @PostMapping("/telegram")
    public void handleTelegramUpdate(@RequestBody Map<String, Object> update) throws SQLException {
        // Imprimir el contenido completo de la actualización para asegurarte de que está llegando algo
        System.out.println("Actualización recibida de Telegram: " + update);

        // Verificar que el objeto 'message' está presente en la actualización
        if (update.containsKey("message")) {
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            Long chatId = ((Number) ((Map<String, Object>) message.get("chat")).get("id")).longValue();
            String text = (String) message.get("text");

            // Imprimir el contenido del mensaje y el chatId
            System.out.println("Mensaje recibido: " + text);
            System.out.println("Chat ID: " + chatId);

            // Lógica para interactuar con el usuario
            if (text.equalsIgnoreCase("/start")) {
                botService.sendTelegramMessage(chatId, "¡Bienvenido! ¿Cómo te llamas?");
                botService.setAskingName(chatId, true);  // Pregunta el nombre
            } else if (botService.isAskingName(chatId)) {
                Client client = new Client(chatId, text);
                botService.setClient(chatId, client);  // Guardar el nombre del usuario
                botService.sendTelegramMessage(chatId, "¡Gracias! Tu nombre ha sido guardado.");
                botService.setAskingName(chatId, false);  // Ha respondido con el nombre
            } else {
                Client client = botService.getClientById(chatId);
                botService.sendTelegramMessage(chatId, "!Hola" + client.getName() + "!");
            }
        } else {
            System.out.println("La actualización no contiene un mensaje válido.");
        }
    }
}