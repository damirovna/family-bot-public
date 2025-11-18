package ru.damirovna.telegram.core.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class BaseManager {
    protected JdbcTemplate jdbcTemplate;

    public BaseManager() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:postgresql://localhost:5432/telegram", "postgres", System.getenv("DB_PASSWORD"));
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
