package umg.edu.gt.Telebot.GPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umg.edu.gt.Telebot.GPT.Model.ChatMessage;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

}
