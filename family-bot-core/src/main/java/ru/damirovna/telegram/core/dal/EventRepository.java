package ru.damirovna.telegram.core.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.damirovna.telegram.core.model.Event;

import java.sql.SQLException;
import java.util.List;

import static ru.damirovna.telegram.common.Constants.DATE_FORMATTER_FULL_DATE;

public class EventRepository extends BaseRepository {

    private static final String INSERT_QUERY = "INSERT INTO public.events(\n" +
            "\tparent_id, summary, location, start, \"end\", google_id)\n" +
            "\tVALUES (?, ?, ?, ?, ?, ?)";
    private static final String FIND_ALL_QUERY = "select * from events";

    public EventRepository(JdbcTemplate jdbc, RowMapper mapper) {
        super(jdbc, mapper);
    }

    public void save(Event event) throws SQLException {
        insert(INSERT_QUERY,
                event.getParentId(),
                event.getSummary(),
                event.getLocation(),
                DATE_FORMATTER_FULL_DATE.format(event.getStart().getTime()),
                DATE_FORMATTER_FULL_DATE.format(event.getEnd().getTime()),
                event.getGoogleId());

    }

    public List<Event> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

}
