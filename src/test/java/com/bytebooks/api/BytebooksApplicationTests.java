package com.bytebooks.api;

import com.bytebooks.api.config.PruebaDeIntegracion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Antes de extender de PruebaDeIntegracion, este test solo pasaba si quien lo
 * corria exportaba MYSQLHOST, JWT_SECRET y compania a mano: en CI, o en una
 * terminal limpia, fallaba al resolver los placeholders sin default.
 */
class BytebooksApplicationTests extends PruebaDeIntegracion {

    @Test
    @DisplayName("el contexto de Spring levanta entero")
    void contextLoads() {
    }
}
