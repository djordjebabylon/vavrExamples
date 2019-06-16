import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import io.vavr.API;
import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Function8;
import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.junit.Test;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

public class VavrExamples {


    @Test//FunctionN types
    public void example1() {

        //Java 8 supports Function and ByFunction but VavrSupports FunctionN types that allows to have up to 8 params
        Function1<Integer, Integer> foo = (x) -> x + x;
        System.out.println(foo.apply(2));

        Function8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> bar = (x1 ,x2, x3, x4 ,x5, x6 ,x7, x8) ->
                x1 + x2 + x3 + x4 + x5+ x6 + x7+ x8;
        System.out.println(bar.apply(1,1,1,1,1,1,1,1));

    }

    @Test//Composed functions
    public void example2() {

        //You can make composed functions
        Function1<String, String> greeting = (s) -> "Hey " + s + "!";
        Function1<String, String> toUpperCase = (s) -> s.toUpperCase();
        Function1<String, String> withEmphasis = (s) -> s + "!!!!!";

        Function1<String, String> bigGreeting = greeting.compose(toUpperCase.compose(withEmphasis));
        System.out.println(bigGreeting.apply("djordje"));

    }

    @Test//Lifting
    public void example3() {

        //If a function within a composed function throws an exception we could prevent the exception and instead
        //return Option.none.
        //This is very useful when composing functions that use third-party libraries and can return exceptions.
        Function1<String, String> greeting = (s) -> "Hey " + s + "!";
        Function1<String, String> toUpperCase = (s) -> s.toUpperCase();
        Function1<String, String> withEmphasis = (s) -> {
            {
                if(s.isEmpty()) {
                    throw new IllegalArgumentException("Cannot be empty");
                }
            return s + "!!!!!";
            }
        };
        Function1<String, String> bigGreeting = greeting.compose(toUpperCase.compose(withEmphasis));

        //System.out.println(bigGreeting.apply(""));// Will throw the exception

        Function1<String, Option<String>> liftedGreeting = Function1.lift(bigGreeting);
        System.out.println(liftedGreeting.apply(""));

    }

    @Test//Partial application
    public void example4() {

        //We can partially apply a function by passing less parameters than the ones it requires.
        Function2<String, String, String> greet = (s1, s2) -> String.format("%s %s!", s1, s2);

        //the function greet requires two parameters but we apply only one
        Function1<String, String> spanishGreet = greet.apply("Hola");
        Function1<String, String> frenchGreet = greet.apply("Salut");

        System.out.println(spanishGreet.apply("Cecilia"));
        System.out.println(frenchGreet.apply("Cecile"));

    }

    @Test//Currying
    public void example5() {

        //Currying allows us to decompose a function of multiple arguments into a succession of functions of a single argument.
        Function3<Integer, Integer, Integer, Integer> baseFunction = (a, b, c) -> a + b + c;

        Function1<Integer, Function1<Integer, Integer>> part1 = baseFunction.curried().apply(2);
        Function1<Integer, Integer> part2 = part1.curried().apply(3);
        Integer part3 = part2.curried().apply(1);

        System.out.println(part3);
    }

    @Test//Memoization/Idempotency
    public void example6() {

        //If a function is called with the same parameters the result should be the same.
        //Memoization allows us to easily implement caching in a function.

        Function1<Integer, String> foo = Function1.of(this::aVeryExpensiveMethod).memoized();

        long startFirstExecution = System.currentTimeMillis();
        System.out.print(foo.apply(2));
        long endFirstExecution = System.currentTimeMillis();
        System.out.println(" in " + (endFirstExecution - startFirstExecution) + "ms");

        long startSecondExecution = System.currentTimeMillis();
        System.out.print(foo.apply(2));
        long endSecondExecution = System.currentTimeMillis();
        System.out.println(" in " + (endSecondExecution - startSecondExecution) + "ms");
    }

    private String aVeryExpensiveMethod(Integer number) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "returned " + (number + number);
    }

    @Test//Option
    public void example7() {

        //Option in vavr supports peek which allows us to perform an action of there's something
        Option<String> o1 = Option.of("something");
        o1.peek(System.out::println);

        //Option is serializable Optional is not

        //Also Option is interoperable with java 8 optional
        Option<String> o2 = Option.ofOptional(Optional.of("something"));

        //In vavr a call to Option.map() can result in Some(null) this could lead to a NullPointerException.
        //Some people don't like this but it actually forces you to pay attention to possible occurrences of
        // null and deal with them accordingly instead of unknowingly accepting them. The correct way to deal with
        // occurrences of null is to use flatMap
        Option<String> o3 = o1.map(s -> (String)null);
        o3.flatMap(o -> Option.of(o));

        /*
        Option is a wrapper of values, if used correctly, we can avoid null checks and also NullPointerException
        Please have a look at OptionalExamples.java
        */

    }

    @Test//Try
    public void example8() {
        //Try is an alternative way of exception handling which is much flexible than classic exception handling in Java
        Function1<Integer, Integer> something = (x) -> x * 2;
        Integer success = Try.of(() -> something.apply(2))
                .getOrElse(-1);
        System.out.println(success);

        //We can graciously provide alternative execution paths in case of errors
        Function1<Integer, Integer> somethingBad = (x) -> {throw new RuntimeException();};
        Integer failure = Try.of(() -> somethingBad.apply(2)).getOrElse(-1);
        System.out.println(failure);

        //If a method that we called return multiple exceptiond but we just want to react to one of them
        //we also can by using the recoverWith
        Function1<Integer, Integer> manyBadThings = (x) -> multipleExceptions();
        Integer recovered = Try.of(() -> manyBadThings.apply(2))
                .recoverWith(IllegalArgumentException.class, Try.of(() -> 777))
                .getOrElse(-1);
        System.out.println(recovered);

        //Combining Try.sequence and flatmap we can extract the values from a list of Try
        List<Try<String>> tries = List(Try.of(() -> "A"),Try.of(() -> "B"),Try.of(() -> "C"));
        Try<String> strings = Try.sequence(tries).flatMap((e) -> e.toTry());
    }

    private Integer multipleExceptions() throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        throw new IllegalArgumentException();
    }

    @Test//Lazy initialized values
    public void example9() {

        Lazy<Integer> lazyValue = Lazy.of(() -> {
            System.out.println("too lazy too print many times");
            return 123;
        });
        //regardless we are calling it multiple times, it is only computing once
        lazyValue.get();
        lazyValue.get();
        lazyValue.get();
    }

    @Test//Collections
    public void example10() {

        //Collections can be initialized in multiple ways
        List<Integer> i1 = List.of(1, 2, 3);
        List<Integer> i2 = API.List(1, 2, 3, 4);//Looks nice when statically imported

        //Drop will drop from left to right the amount of elements you want
        List<Integer> droppedValue = i1.drop(2);
        System.out.println(droppedValue);

        //take allows us to take the first x elements
        List<Integer> takenValues = i1.take(2);
        System.out.println(takenValues);

        //Tail will return everything but the first element
        List<Integer> tail = i2.tail();
        System.out.println(tail);

        //zipWithIndex creates indexes for the values in the collection
        List<String> i3 = List.of("A", "B", "C");
        System.out.println(i3.zipWithIndex());

        //We can convert to Java 8 collections using asJava()
        java.util.List<Integer> javaIntegers = i1.asJava();

        //Collections are immutable in vavr. Notice that there is no add method in list
        //instead there are append and prepend.
        List<Integer> i4 = i1.append(9);
        i1.prepend(3);


    }

    @Test//Tuples
    public void example11() {
        //Tuples are groups of elements. Java 8 supports pairs but vavr goes one step further and allows tuples
        Tuple.of("A");
        Tuple.of("A", 2);
        Tuple.of("A", 2, true);
        Tuple.of("A", 2, true, 0.1D);
        Tuple.of("A", 2, true, 0.1D, new Object());
        Tuple.of("A", 2, true, 0.1D, new Object(), 'x');
        Tuple.of("A", 2, true, 0.1D, new Object(), 'x', 111L);
        Tuple.of("A", 2, true, 0.1D, new Object(), 'x', 111L, 2.78F);//Max tupple is of 8 elements

        //The values of Tuples are acessed using the values _1, _2, etc... Maybe not the nicest :(
        Tuple2<String, String> person = Tuple.of("Djordje", "Programmer");
        System.out.println(person._1);
        System.out.println(person._2);
    }


    @Test//Painless Checked exceptions
    public void example12() {
        List<String> urls = API.List("zzz", "http://www.google.com", "xx");

        //This is a classic problem when using lambdas and checked exceptions
        urls.map(u -> {
            try {
                return new URI(u);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        });

        //In vavr we can prevent the checked exception like this
        List<URI> uris = urls.map(u -> API.unchecked(() -> new URI(u)).apply());
        System.out.println(uris);

    }

    @Test//Pattern matching
    public void example13() {

        Object a = 23;

        String value = Match(a).of(
                Case($(instanceOf(String.class)), "it's a word"),
                Case($(instanceOf(Integer.class)), "it's a number"));

        System.out.println(value);

    }
}
