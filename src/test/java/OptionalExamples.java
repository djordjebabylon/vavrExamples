import io.vavr.Tuple2;
import io.vavr.control.Option;

import static io.vavr.API.*;
import static io.vavr.Patterns.*;

public class OptionalExamples {

    private Exec exec = new Exec();

    //NO NULL CHECKS
    //instead of
    public String legacyNullCheck(String nullableValue) {
        if (nullableValue != null) {
            return nullableValue.toLowerCase();
        }
        return nullableValue;
    }

    //with vavr
    public String vavrNullCheck(String nullableValue) {
        return Option.of(nullableValue)
                .map(String::toLowerCase)
                .getOrElse(nullableValue);
    }


    //------------------------------------------------------------------------
    //NO NULL CONDITIONAL EXECUTION
    //instead of
    public void legacyNullConditionalExecution(String nullableValue) {
        if (nullableValue != null) {
            exec.methodOne(nullableValue);
        }
    }

    // with vavr
    public void vavrNullConditionalExecution(String nullableValue) {
        Option.of(nullableValue)
                .forEach(exec::methodOne);
    }


    //------------------------------------------------------------------------
    //NO NULL CONDITIONAL EXECUTION WITH SAME METHOD
    // instead of
    public void legacyConditionalExecutionOfTheSameMethod(String nullableValue) {
        if (nullableValue == null) {
            exec.methodOne("default");
        } else {
            exec.methodOne(nullableValue);
        }
    }

    // with vavr
    public void vavrConditionalExecutionOfTheSameMethod(String nullableValue) {
        Option.of(nullableValue)
                .orElse(Option.of("default"))
                .forEach(exec::methodOne);
    }

    //------------------------------------------------------------------------
    //CONDITIONAL EXECUTION WITH DIFFERENT METHODS
    // instead of
    public void legacyConditionalExecutionOfDifferentMethods(String nullableValue) {
        if (nullableValue == null) {
            exec.methodOne("default");
        } else {
            exec.methodTwo(nullableValue);
        }
    }

    // with vavr
    public void vavrConditionalExecutionOfDifferentMethods(String nullableValue) {
        Option.of(nullableValue)
                .peek(exec::methodTwo)
                .onEmpty(() -> exec.methodOne("default"));
    }


    //------------------------------------------------------------------------
    // CONDITIONAL EXCEPTION THROWING
    // instead of
    public void legacyConditionalException(String nullableValue) {
        if (nullableValue != null) {
            exec.methodOne(nullableValue);
        } else {
            throw new RuntimeException("don't like nulls");
        }
    }

    // with vavr
    public void vavrConditionalException(String nullableValue) {
        Option.of(nullableValue)
                .peek(exec::methodOne)
                .getOrElseThrow(() -> new RuntimeException("don't like nulls"));
    }

    //------------------------------------------------------------------------

    //COMPLEX CONDITIONAL LOGIC
    // instead of
    public String legacyComplexAndConditional(String nullableValue) {
        if (nullableValue != null && nullableValue.startsWith("ONE")) {
            return "1st condition";
        } else {
            return "default";
        }
    }

    // with vavr
    public String vavrComplexAndConditional(String nullableValue) {
        return Option.of(nullableValue)
                .filter(given -> given.startsWith("ONE"))
                .map(ignore -> "1st condition")
                .getOrElse("default");
    }

    //------------------------------------------------------------------------
    //COMPLEX NESTED CONDITIONS
    // instead of
    public void legacyNestedConditionWithMultipleValues(String nullableValueA, String nullableValueB) {
        if (nullableValueA == null && nullableValueB == null) {
            exec.methodOne("default");
        } else if (nullableValueA == null) {  // nullableValueB != null
            exec.methodOne(nullableValueB);
        } else if (nullableValueB == null) {  // nullableValueA != null
            exec.methodTwo(nullableValueA);
        } else { // nullableValueA != null && nullableValueB != null
            exec.methodThree("default");
        }
    }

    // with vavr
    public void vavrNestedConditionWithMultipleValues(String nullableValueA, String nullableValueB) {
        Match(new Tuple2<>(Option.of(nullableValueA), Option.of(nullableValueB))).of(
                Case($Tuple2($None(), $None()), () -> run(() -> exec.methodOne("default"))),
                Case($Tuple2($None(), $Some($())), () -> run(() -> exec.methodOne(nullableValueB))),
                Case($Tuple2($Some($()), $None()), () -> run(() -> exec.methodTwo(nullableValueA))),
                Case($Tuple2($Some($()), $Some($())), () -> run(() -> exec.methodThree("default")))
        );
    }

    //------------------------------------------------------------------------
    //COMPLEX NESTED CONDITIONS with return
    // instead of
    public String legacyNestedConditionsWithReturn(String nullableValue) {
        if (nullableValue == null) {
            return "default";
        } else if (nullableValue.startsWith("ONE")) {
            return nullableValue.concat("123");
        } else if (nullableValue.startsWith("TWO")) {
            return nullableValue.concat("456");
        } else {
            return nullableValue.concat("789");
        }
    }

    // do it the functional way
    public String vavrNestedConditionsWithReturn(String nullableValue) {
        return Match(Option.of(nullableValue)).of(
                Case($None(), () -> "default"),
                Case($Some($(v -> v.startsWith("ONE"))), () -> nullableValue.concat("123")),
                Case($Some($(v -> v.startsWith("TWO"))), () -> nullableValue.concat("456")),
                Case($Some($()), () -> nullableValue.concat("789"))
        );
    }

    //------------------------------------------------------------------------
    //COMPLEX CONDITIONS with executions
    // instead of
    public void legacyNestedConditionsWithExecution(String nullableValue) {
        if (nullableValue == null) {
            exec.methodOne("default");
        } else if (nullableValue.startsWith("ONE")) {
            exec.methodOne(nullableValue);
        } else if (nullableValue.startsWith("TWO")) {
            exec.methodTwo(nullableValue);
        } else {
            exec.methodThree(nullableValue);
        }
    }

    // with vavr
    public void vavrNestedConditionsWithExecution(String nullableValue) {
        Match(Option.of(nullableValue)).of(
                Case($None(), () -> run(() -> exec.methodOne("default"))),
                Case($Some($(v -> v.startsWith("ONE"))), () -> run(() -> exec.methodOne(nullableValue))),
                Case($Some($(v -> v.startsWith("TWO"))), () -> run(() -> exec.methodTwo(nullableValue))),
                Case($Some($()), () -> run(() -> exec.methodThree(nullableValue)))
        );
    }
}
