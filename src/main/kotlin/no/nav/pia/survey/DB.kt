package no.nav.pia.survey

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

internal fun createDataSource() =
    HikariDataSource().apply {
        jdbcUrl = MiljøVariabler.jdbcUrl
        maximumPoolSize = 10
        minimumIdle = 1
        idleTimeout = 100000
        connectionTimeout = 100000
        maxLifetime = 300000
    }

internal fun runMigration(dataSource: DataSource) =
    getFlyway(dataSource)
        .migrate()

private fun getFlyway(dataSource: DataSource) =
    Flyway.configure()
        .validateMigrationNaming(true)
        .dataSource(dataSource).load()
