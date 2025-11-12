package ru.damirovna.telegram.core.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.damirovna.telegram.core.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


@Repository
public class UserRepository extends BaseRepository<User> {

    private static final String INSERT_QUERY = "INSERT INTO public.users(\n" +
            "\tname, date_notification, chat_id, location_id, current_process)\n" +
            "\tVALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE public.users\n" +
            "\tSET  name=?, date_notification=?, location_id=?, current_process=?\n" +
            "\tWHERE chat_id=?";
    private static final String FIND_ALL_QUERY = "SELECT\n" +
            "\tu.*, l.*, w.* \n" +
            "FROM \n" +
            "\tusers u \n" +
            "\tLEFT OUTER JOIN locations l ON u.location_id = l.id\n" +
            "\tLEFT OUTER JOIN weather w ON w.location_id = u.location_id";

    private static final String FIND_USER_BY_CHAT_ID = "SELECT\n" +
            "\tu.*, l.*, w.* \n" +
            "FROM\n" +
            "\tusers u \n" +
            "\tLEFT OUTER JOIN locations l ON u.location_id = l.id\n" +
            "\tLEFT OUTER JOIN weather w ON w.location_id = u.location_id\n" +
            "WHERE\n" +
            "\tu.chat_id = ?";

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> findByChatId(Long chatId) {
        return findOne(FIND_USER_BY_CHAT_ID, chatId.toString());
    }

    public User save(User user) throws SQLException {
        int id = insert(
                INSERT_QUERY,
                user.getName(),
                user.getTimeForMessages(),
                user.getChatId(),
                (user.getLocation() != null) ? user.getLocation().getId() : null,
                user.getCurrentProcess()
        );
        user.setId(id);
        return user;
    }

    public void update(User user) throws SQLException {
        update(UPDATE_QUERY,
                user.getName(),
                (user.getTimeForMessages() != null) ? user.getTimeForMessages() : null,
                (user.getLocation() != null) ? user.getLocation().getId() : null,
                user.getCurrentProcess(),
                user.getChatId().toString());

    }

}
