package ru.damirovna.telegram.core.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class BaseManager {
    protected JdbcTemplate jdbcTemplate;

    public BaseManager() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:postgresql://localhost:5432/telegram", "postgres", "postgres");
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
