package org.jargo;

import java.util.List;
import java.util.Optional;

public class JargoOptionalTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        // TODO: (tarrach): direct primitive
        new JargoRoundtripTestArguments("string present", """
            {"string":"present"}""",
            new TestValues.Rec.OptionalRecord(Optional.of("present"))),
        new JargoRoundtripTestArguments("string absent", """
            {}""",
            new TestValues.Rec.OptionalRecord(Optional.empty())),
        new JargoRoundtripTestArguments("string value present", """
            {"string":"present"}""",
            new TestValues.Rec.OptionalRecordValue(
                new TestValues.OptionalValue.OptionalString(
                    Optional.of("present"))
            )),
        new JargoRoundtripTestArguments("string value absent", """
            {}""",
            new TestValues.Rec.OptionalRecordValue(
                new TestValues.OptionalValue.OptionalString(
                    Optional.empty())
            ))
        // TODO: (tarrach): other record
        // TODO: (tarrach): enum
    );
  }
}
