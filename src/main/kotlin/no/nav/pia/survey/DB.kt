package no.nav.pia.survey

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

internal fun createDataSource() =
    HikariDataSource().apply {
        jdbcUrl = MiljøVariabler.jdbcUrl
    }

internal fun runMigration(dataSource: DataSource) =
    getFlyway(dataSource)
        .migrate()

private fun getFlyway(dataSource: DataSource) =
    Flyway.configure()
        .validateMigrationNaming(true)
        .dataSource(dataSource).load()
