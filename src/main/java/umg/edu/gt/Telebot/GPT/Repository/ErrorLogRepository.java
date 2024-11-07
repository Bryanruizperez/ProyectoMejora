package umg.edu.gt.Telebot.GPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umg.edu.gt.Telebot.GPT.Model.ErrorLog;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
}
