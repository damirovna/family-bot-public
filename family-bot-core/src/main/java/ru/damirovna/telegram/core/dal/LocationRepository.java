package ru.damirovna.telegram.core.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.damirovna.telegram.core.model.Location;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class LocationRepository extends BaseRepository<Location> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM locations";
    private static final String FIND_BY_LAT_AND_LONG = "SELECT * FROM locations WHERE longitude = ? and latitude = ?";

    private static final String FIND_BY_NAME = "SELECT * FROM locations WHERE location_name = ?";
    private static final String INSERT_QUERY = "INSERT INTO locations(\n" +
            "\tlocation_name, longitude, latitude)\n" +
            "\tVALUES (?, ?, ?) returning id";

    public LocationRepository(JdbcTemplate jdbc, RowMapper<Location> mapper) {
        super(jdbc, mapper);
    }

    public List<Location> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Location save(Location location) throws SQLException {
        Optional<Location> findLocation = getLocationByName(location.getName());
        if (findLocation.isPresent()) {
            return findLocation.get();
        }
        int id = insert(
                INSERT_QUERY,
                location.getName(),
                location.getLongitude(),
                location.getLatitude()
        );
        location.setId(id);
        return location;
    }

    public Optional<Location> getLocationByName(String name) {
        return findOne(FIND_BY_NAME, name);
    }


}
