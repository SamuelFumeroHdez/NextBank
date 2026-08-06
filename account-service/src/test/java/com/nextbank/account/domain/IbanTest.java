package com.nextbank.account.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class IbanTest {

    @ParameterizedTest
    @DisplayName("acepta IBAN validos de distintos paises y longitudes")
    @ValueSource(strings = {
            "ES9121000418450200051332",      // Espana, 24
            "ES6621000418401234567891",
            "DE89370400440532013000",         // Alemania, 22
            "FR1420041010050500013M02606",    // Francia, 27, con letra en el BBAN
            "GB29NWBK60161331926819",         // Reino Unido, 22
            "NL91ABNA0417164300",             // Paises Bajos, 18 (el mas corto)
            "IT60X0542811101000000123456",    // Italia, 27
            "PT50000201231234567890154"       // Portugal, 25
    })
    void acceptsValidIbans(String value) {
        assertThat(new Iban(value).value()).isEqualTo(value);
    }

    @Test
    @DisplayName("normaliza espacios y minusculas")
    void normalizesSpacesAndLowercase() {
        Iban iban = new Iban("es91 2100 0418 4502 0005 1332");

        assertThat(iban.value()).isEqualTo("ES9121000418450200051332");
    }

    @Test
    @DisplayName("dos IBAN con distinto formato pero mismo valor son iguales")
    void equalityIgnoresFormatting() {
        Iban spaced = new Iban("ES91 2100 0418 4502 0005 1332");
        Iban compact = new Iban("ES9121000418450200051332");

        assertThat(spaced).isEqualTo(compact);
        assertThat(spaced.hashCode()).isEqualTo(compact.hashCode());
    }

    @ParameterizedTest
    @DisplayName("rechaza IBAN con formato invalido")
    @ValueSource(strings = {
            "1234567890123456789012",         // sin codigo de pais
            "E59121000418450200051332",       // un solo caracter de pais
            "ESAB21000418450200051332",       // digitos de control no numericos
            "ES91-2100-0418-4502",            // caracteres no alfanumericos
            "ES",                              // demasiado corto
            ""                                 // vacio
    })
    void rejectsMalformedIbans(String value) {
        assertThatThrownBy(() -> new Iban(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @DisplayName("rechaza IBAN con checksum incorrecto")
    @ValueSource(strings = {
            "ES9121000418450200051333",       // ultimo digito alterado
            "ES6621000418401234567890",
            "DE89370400440532013001",
            "NL91ABNA0417164301"
    })
    void rejectsInvalidChecksum(String value) {
        assertThatThrownBy(() -> new Iban(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checksum");
    }

    @Test
    @DisplayName("rechaza null")
    void rejectsNull() {
        assertThatThrownBy(() -> new Iban(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("detecta la transposicion de dos digitos")
    void detectsTransposedDigits() {
        // el mod-97 esta disenado para detectar este error de tecleo
        assertThatThrownBy(() -> new Iban("ES9121000418450200051323"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
