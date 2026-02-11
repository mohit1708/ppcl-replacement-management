package com.ppcl.replacement.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
/*THis will contain PPCL way to connect to DB */
public class DBConnectionPool {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:oracle:thin:@80.225.211.27:1521/FREEPDB1");
        config.setUsername("replacement_app");
        config.setPassword("app123");
//        config.setJdbcUrl("jdbc:oracle:thin:@//192.168.0.5:1521/orcl");
//        config.setUsername("printer");
//        config.setPassword("FYTuvRsupk");

        // Pool tuning (sane defaults)
        //config.setMaximumPoolSize(10); // check in future and enable as per load
        //config.setMinimumIdle(2); // check in future and enable as per load
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);

        // Oracle-specific
        config.setDriverClassName("oracle.jdbc.OracleDriver");

        dataSource = new HikariDataSource(config);
    }

    private DBConnectionPool() {
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection(); // returns pooled connection
    }
}
