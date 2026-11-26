package br.com.aurora.assistance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class AssistanceApplicationTest {

    @Autowired
    ApplicationContext context;

    @Test
    void startsTheApplicationContext() {
        assertThat(context.getBean(AssistanceApplication.class)).isNotNull();
    }
}
