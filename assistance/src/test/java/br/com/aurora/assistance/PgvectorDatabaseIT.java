package br.com.aurora.assistance;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Testcontainers
class PgvectorDatabaseIT {

    // The same image as the postgres service in compose.yaml.
    static final DockerImageName PGVECTOR =
            DockerImageName.parse("pgvector/pgvector:0.8.7-pg18-trixie").asCompatibleSubstituteFor("postgres");

    // And the same init script, so the test database gets the vector extension like the real one.
    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(PGVECTOR)
            .withCopyFileToContainer(
                    MountableFile.forHostPath("../docker/postgres/init.sql"), "/docker-entrypoint-initdb.d/init.sql");

    @Test
    void createsTheVectorExtension() throws SQLException {
        assertThat(query("SELECT extversion FROM pg_extension WHERE extname = 'vector'"))
                .containsExactly("0.8.7");
    }

    @Test
    void ordersEmbeddingsByCosineDistance() throws SQLException {
        try (Connection connection = postgres.createConnection("");
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TEMPORARY TABLE note (text text, embedding vector(3))");
            statement.execute("INSERT INTO note VALUES ('rear bumper', '[1,0,0]'), ('windshield', '[0,1,0]'),"
                    + " ('front bumper', '[0.9,0.1,0]')");
            ResultSet rows = statement.executeQuery("SELECT text FROM note ORDER BY embedding <=> '[1,0,0]'");
            List<String> texts = new ArrayList<>();
            while (rows.next()) {
                texts.add(rows.getString(1));
            }
            assertThat(texts).containsExactly("rear bumper", "front bumper", "windshield");
        }
    }

    private static List<String> query(String sql) throws SQLException {
        try (Connection connection = postgres.createConnection("");
                Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery(sql)) {
            List<String> values = new ArrayList<>();
            while (rows.next()) {
                values.add(rows.getString(1));
            }
            return values;
        }
    }
}
