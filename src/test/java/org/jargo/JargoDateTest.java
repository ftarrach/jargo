package org.jargo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

public class JargoDateTest {

  public static List<JargoRoundtripTestArguments> get() {
    Year year = Year.of(2023);
    YearMonth ym = year.atMonth(1);
    LocalDate ld = ym.atDay(2);
    LocalTime lt = LocalTime.of(0, 37, 53);
    LocalDateTime ldt = ld.atTime(lt);
    OffsetDateTime odt = OffsetDateTime.of(ldt, ZoneOffset.ofHours(2));
    Instant instant = odt.toInstant();
    return List.of(
        new JargoRoundtripTestArguments("offset date time", "\"2023-01-01T22:37:53Z\"", instant)
    );
  }
}
