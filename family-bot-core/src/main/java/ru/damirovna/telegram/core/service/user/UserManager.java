package ru.damirovna.telegram.core.service.user;

import org.springframework.stereotype.Service;
import ru.damirovna.telegram.core.dal.LocationRepository;
import ru.damirovna.telegram.core.dal.UserRepository;
import ru.damirovna.telegram.core.dal.mappers.LocationRowMapper;
import ru.damirovna.telegram.core.dal.mappers.UserRowMapper;
import ru.damirovna.telegram.core.model.Location;
import ru.damirovna.telegram.core.model.User;
import ru.damirovna.telegram.core.service.BaseManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class UserManager extends BaseManager {
    private final UserRepository userRepository = new UserRepository(jdbcTemplate, new UserRowMapper());
    private final LocationRepository locationRepository = new LocationRepository(jdbcTemplate, new LocationRowMapper());

    public boolean saveNewUser(User user) throws SQLException {
        if (userRepository.findByChatId(user.getChatId()).isEmpty()) {
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        return userRepository.findAll();
    }

    public void saveLocation(User user, Location location) throws SQLException {
        Location locationDB = locationRepository.save(location);
        user.setLocation(locationDB);
        updateUser(user);
    }

    public void updateUser(User user) throws SQLException {
        Optional<User> userOptional = userRepository.findByChatId(user.getChatId());
        if (userOptional.isEmpty()) {
            saveNewUser(user);
            return;
        }
        try {
            userRepository.update(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<User> getUserByChatId(long chatId) {
        return userRepository.findByChatId(chatId);
    }
}
