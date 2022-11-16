### Бот Кроха-Цербер для Telegram v1.0.5

-----------------------------------

Lead project: [Злата Занина](https://github.com/Zelginni)  
Java-maggot: [Сергей Сорокин](https://github.com/Iff-Nomatter)  

-----------------------------------

Этот бот предназначен для администрирования супергрупп в Telegram. Он был создан, чтобы заменить погибший Combot (ведь все знают, что алчность подобна смерти).

Бот написан с использованием следующих технологий:  
[Kotlin](https://kotlinlang.org/)  
[Gradle](https://gradle.org/)  
[Hibernate](https://hibernate.org/)  
[PostgreSQL](https://www.postgresql.org/)  
[Spring Boot](https://spring.io/projects/spring-boot)  
[Spring Security](https://spring.io/projects/spring-security)  
[Telegram Bot API](https://core.telegram.org/bots/api)   
[Flyway](https://flywaydb.org/)  

-----------------------------------

Для запуска бота необходимо:  
1. Внести в application.properties данные для подключения базы данных
2. Внести в application.properties данные для авторизации администратора
3. Внести в application.properties данные для авторизации бота в Telegram (их можно получить у [BotFather](https://t.me/BotFather))  
4. Собрать JAR бота и запустить его на сервере
5. Открыть в браузере страницу по адресу: ht<span>tp://а</span>дрес-сервера:порт-сервера/swagger-ui и авторизоваться там 
6. Получить ID чата, в котором будет работать бот
7. Активировать бота в чате через swagger
8. Настроить функционал бота, включая\выключая необходимые функции

------------------------------------
#### Функционал бота
Все команды можно вводить как через восклицательный знак в начале команды, так и через слэш (добавляя через собаку юзернейм бота), например:  
!warn или /warn@tinycerberus_bot  

Для удобства далее все команды будут указаны с восклицательного знака. 

1. !warn: Ответ этой командой на сообщение пользователя выдает ему предупреждение. Лимит варнов устанавливается через админку в swagger. По достижении лимита пользователю в чате выдается бан.

2. !statwarn: Выводит статистику варнов по всему чату. Если команда использована в ответ на сообщение пользователя, выводится детальная статистика по конкретному пользователю, включающая в себя авторов предупреждений.

3. !unwarn: Снимает одно предупреждение с пользователя.

4. !status: Проверочное сообщение, бот говорит, может ли он выполнять свои функции в этом чате.

5. !digest: Ответ этой командой на сообщение пользователя заносит ссылку на это сообщение в ежедневный дайджест. Дайджест будет собираться ботом и выводиться в установленное время (настройка bot.digest.cron в application.properties). После команды через пробел можно добавить краткое описание для записи в дайджесте. При отсутствии описания, бот возьмет текст изначального сообщения и обрежет его до 100 символов.

6. Баян. При появлении сообщения, содержащего слово "баян" бот хамски отвечает написавшему.