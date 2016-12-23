package streamDemoPK;

import EmpPK.*;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.*;

public class StreamSamples {
  public static void main(String[] args) {
    //reduceExamples();
    //forEachExamples();
    mapExamples();
    //filterExamples();
    //combinedExamples();
    //lazyEvaluationExamples();
    //reduceExamples();
    //collectExamples();
    //limitExamples();
    //sortedExamples();
    //matchExamples();
    //parallelExamples();
    //stringJoinerExamples();
  }
  
  public static void forEachExamples() {
    List<Employee> googlers = EmployeeSamples.getGooglers();
    googlers.stream().forEach(System.out::println);
    googlers.stream().forEach(e -> e.setSalary(e.getSalary() * 11/10));  // Redo in parallel with delay in setSalary
    googlers.stream().forEach(System.out::println);
    
    Consumer<Employee> giveRaise = e -> {
      System.out.printf("%s earned $%,d before raise.%n",
                         e.getFullName(), e.getSalary());
      e.setSalary(e.getSalary() * 11/10);
      System.out.printf("%s will earn $%,d after raise.%n",
                        e.getFullName(), e.getSalary());
    };
    googlers.stream().forEach(giveRaise);
    List<Employee> sampleEmployees = EmployeeSamples.getSampleEmployees();
    sampleEmployees.stream().forEach(giveRaise);
  }
  
  private static <T> void printStreamAsList(Stream<T> s, String message) {
    System.out.printf("%s: %s.%n", 
                      message, s.collect(Collectors.toList()));
  }
  
  public static void mapExamples() {
    List<Double> nums = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
    printStreamAsList(nums.stream(), "Original nums");
    printStreamAsList(nums.stream().map(n -> n * n), "Squares");
    printStreamAsList(nums.stream().map(n -> n * n).map(Math::sqrt), 
                      "Square roots of the squares");
   
    Integer[] ids = { 1, 2, 4, 8 };
    printStreamAsList(Stream.of(ids), "IDs");
    printStreamAsList(Stream.of(ids).map(EmployeeSamples::findGoogler)
                                    .map(Person::getFullName),
                      "Names of Googlers with given IDs");
  }
  
  public static void filterExamples() {
    Integer[] nums = { 1, 2, 3, 4, 5, 6 };
    printStreamAsList(Stream.of(nums), "Original nums");
    printStreamAsList(Stream.of(nums).filter(n -> n%2 == 0), "Even nums");
    printStreamAsList(Stream.of(nums).filter(n -> n>3), "Nums > 3");
    printStreamAsList(Stream.of(nums).filter(n -> n%2 == 0)
                                     .filter(n -> n>3), 
                      "Even nums > 3");
    
    Integer[] ids = { 16, 8, 4, 2, 1 };
    printStreamAsList(Stream.of(ids).map(EmployeeSamples::findGoogler) 
                                    .filter(e -> e != null)
                                    .filter(e -> e.getSalary() > 500000),
                      "Googlers with salaries over $500K");
    
  }
  
  public static void combinedExamples() {
    Integer[] ids = { 16, 8, 4, 2, 1 };
    System.out.printf("First Googler with salary over $500K: %s%n", 
                      Stream.of(ids).map(EmployeeSamples::findGoogler) 
                                    .filter(e -> e != null)
                                    .filter(e -> e.getSalary() > 500000)
                                    .findFirst()
                                    .orElse(null));
  }
  
  public static void lazyEvaluationExamples() {
    // Same as combinedExamples, but with print statements
    
    Function<Integer,Employee> findGoogler = 
      n -> { System.out.println("Finding Googler with ID " + n);
             return(EmployeeSamples.findGoogler(n));
           };
    Predicate<Employee> checkForNull = 
      e -> { System.out.println("Checking for null");
             return(e != null);
           };
    Predicate<Employee> checkSalary = 
      e -> { System.out.println("Checking if salary > $500K");
             return(e.getSalary() > 500000);
           };
    Integer[] ids = { 16, 8, 4, 2, 1 };
    System.out.printf("First Googler with salary over $500K: %s%n", 
                      Stream.of(ids).map(findGoogler)
                                    .filter(checkForNull)
                                    .filter(checkSalary)
                                    .findFirst()
                                    .orElse(null));
  }
  
  public static void reduceExamples() {
    List<String> letters = Arrays.asList("a", "b", "c", "d");
    String concat = letters.stream().reduce("", String::concat);
    System.out.printf("Concatenation of %s is %s.%n", 
                      letters, concat);
    String reversed = letters.stream().reduce("", (s1,s2) -> s2+s1);
    System.out.printf("Reversed concatenation of %s is %s.%n", 
                      letters, reversed);
    String upperReversed = 
      letters.stream().reduce("", (s1,s2) -> s2.toUpperCase() + s1.toUpperCase());
    System.out.printf("Uppercase reversed concatenation of %s is %s.%n", 
                      letters, upperReversed);

    Employee poorest = new Employee("None", "None", -1, -1);
    BinaryOperator<Employee> richer = (e1, e2) -> {
      return(e1.getSalary() >= e2.getSalary() ? e1 : e2);
    };
    List<Employee> googlers = EmployeeSamples.getGooglers();
    Employee richestGoogler = googlers.stream().reduce(poorest, richer);
    System.out.printf("Richest Googler is %s.%n", richestGoogler);

    List<Integer> nums2 = Arrays.asList(1, 2, 3, 4);
    int sum1 = nums2.stream().reduce(0, Integer::sum);
    System.out.printf("Sum of %s is %s.%n", nums2, sum1);
    int sum2 = nums2.stream().reduce(Integer::sum).get();
    System.out.printf("Sum of %s is %s.%n", nums2, sum2);

    int product = nums2.stream().reduce(1, (n1, n2) -> n1 * n2);
    System.out.printf("Product of %s is %s.%n", nums2, product);
    
    int sum4 = nums2.stream().map(EmployeeSamples::findGoogler)
                             .map(Employee::getSalary)
                             .reduce(0, Integer::sum);
    System.out.printf("Combined salaries of Googlers with IDs %s is $%,d.%n", 
                      nums2, sum4);
    int sum5 = nums2.stream().map(EmployeeSamples::findGoogler)
                             .mapToInt(Employee::getSalary)
                             .sum();
    System.out.printf("Combined salaries of Googlers with IDs %s is $%,d.%n", 
                      nums2, sum5);
  }
  
  public static void collectExamples() {
    List<Integer> ids = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16); // and imagine 1000 more ids
    List<Employee> emps = 
      ids.stream().map(EmployeeSamples::findGoogler)
                  .collect(Collectors.toList());
    System.out.printf("Googlers with ids %s: %s.%n", 
                      ids, emps);
    String lastNames = ids.stream().map(EmployeeSamples::findGoogler)
                                   .map(Employee::getLastName)
                                   .collect(Collectors.joining(", "))
                                   .toString(); 
    
    List<Employee> googlers = EmployeeSamples.getGooglers();
    System.out.printf("Last names of Googlers with ids %s: %s.%n", 
                      ids, lastNames);
    Set<String> firstNames =
      googlers.stream().map(Employee::getFirstName)
                       .collect(Collectors.toSet());
    Stream.of("Larry", "Harry", "Peter", "Deiter", "Eric", "Barack")
            .forEach(s -> System.out.printf
                              ("%s is a Googler? %s.%n",
                               s,
                               firstNames.contains(s) ? "Yes" : "No"));
    TreeSet<String> firstNames2 =
      googlers.stream()
              .map(Employee::getFirstName)
              .collect(Collectors.toCollection(TreeSet::new));
      Stream.of("Larry", "Harry", "Peter", "Deiter", "Eric", "Barack")
            .forEach(s -> System.out.printf
                              ("%s is a Googler? %s.%n",
                               s,
                               firstNames2.contains(s) ? "Yes" : "No"));
    Map<Boolean,List<Employee>> richTable =
      googlers.stream()
              .collect(Collectors.partitioningBy(e -> e.getSalary() > 1000000));
    System.out.printf("Googlers with salaries over $1M: %s.%n", richTable.get(true));
    System.out.printf("Destitute Googlers: %s.%n", richTable.get(false));
    
    Map<String,List<Emp>> officeTable =
      EmpSamples.getSampleEmps().stream().collect(Collectors.groupingBy(Emp::getOffice));
    System.out.printf("Emps in Mountain View: %s.%n", officeTable.get("Mountain View"));
    System.out.printf("Emps in NY: %s.%n", officeTable.get("New York"));
    System.out.printf("Emps in Zurich: %s.%n", officeTable.get("Zurich"));
  }
  
  public static void limitExamples() {
    List<Employee> googlers = EmployeeSamples.getGooglers();
    // Calls getFirstName only 6 times
    List<String> emps = 
      googlers.stream()
              .map(Person::getFirstName)
              .limit(8)
              .skip(2)
              .collect(Collectors.toList());
    System.out.printf("Names of 6 Googlers: %s.%n", 
                      emps);
  }
  
  public static void sortedExamples() {
    List<Integer> ids = Arrays.asList(9, 11, 10, 8);
    List<Employee> emps1 = 
      ids.stream().map(EmployeeSamples::findGoogler)
                  .sorted((e1, e2) -> e1.getLastName()
                                        .compareTo(e2.getLastName()))
                  .collect(Collectors.toList());
    System.out.printf("Googlers with ids %s sorted by last name: %s.%n", 
                      ids, emps1);
    List<Employee> emps2 = 
      ids.stream().map(EmployeeSamples::findGoogler)
                  .sorted((e1, e2) -> e1.getSalary() - e2.getSalary())
                  .collect(Collectors.toList());
    System.out.printf("Googlers with ids %s sorted by salary: %s.%n", 
                      ids, emps2);
    
    List<Employee> sampleEmployees = EmployeeSamples.getSampleEmployees();
    // Using limit does not short-circuit the sorting
    List<Employee> emps3 = 
      sampleEmployees.stream()
                     .sorted(Person::firstNameComparer)
                     .limit(2)
                     .collect(Collectors.toList());
    System.out.printf("Employees sorted by first name: %s.%n", 
                      emps3);
    
    // min is faster than sorting and choosing the first
    Employee alphabeticallyFirst = 
      ids.stream().map(EmployeeSamples::findGoogler)
                  .min((e1, e2) -> e1.getLastName()
                                     .compareTo(e2.getLastName()))
                  .get();
    System.out.printf("Googler from %s with earliest last name: %s.%n", 
                      ids, alphabeticallyFirst);
    Employee richest = 
      ids.stream().map(EmployeeSamples::findGoogler)
                  .max((e1, e2) -> e1.getSalary() - 
                                   e2.getSalary())
                  .get();
    System.out.printf("Richest Googler from %s: %s.%n", 
                      ids, richest);
    List<Integer> ids2 = 
      Arrays.asList(9, 10, 9, 10, 9, 10);
    List<Employee> emps4 = 
      ids2.stream().map(EmployeeSamples::findGoogler)
                   .distinct()
                   .collect(Collectors.toList());
    System.out.printf("Unique Googlers from %s: %s.%n", 
                      ids2, emps4);
  }
  
  public static void matchExamples() {
    List<Employee> googlers = EmployeeSamples.getGooglers();
    boolean isNobodyPoor = 
      googlers.stream().noneMatch(e -> e.getSalary() < 200000);
    Predicate<Employee> megaRich = e -> e.getSalary() > 7000000;
    boolean isSomeoneMegaRich = googlers.stream().anyMatch(megaRich);
    boolean isEveryoneMegaRich = googlers.stream().allMatch(megaRich);
    long numberMegaRich = googlers.stream().filter(megaRich).count();
    System.out.printf("Nobody poor? %s.%n", isNobodyPoor);
    System.out.printf("Someone mega rich? %s.%n", isSomeoneMegaRich);
    System.out.printf("Everyone mega rich? %s.%n", isEveryoneMegaRich);
    System.out.printf("Number mega rich: %s.%n", numberMegaRich);
  }
  
  public static void parallelExamples() {
    List<Employee> googlers = EmployeeSamples.getGooglers();
    System.out.print("Serial version [11 entries]:");
    timingTest(googlers.stream());
    int numProcessorsOrCores = 
      Runtime.getRuntime().availableProcessors();
    System.out.printf("Parallel version on %s-core machine:",
                      numProcessorsOrCores);
    timingTest(googlers.stream().parallel());  // or googlers.parallelStream()
    System.out.print("Serial version [4 entries]:");
    timingTest(googlers.stream().limit(4));
    System.out.printf("Parallel version on %s-core machine:",
                      numProcessorsOrCores);
    timingTest(googlers.stream().parallel().limit(4));
    int[] nums = {1,2,3,4};
    int sum1 = 0;
    for(int num: nums) {
      sum1 += num;
    }
    int sum2 = IntStream.of(nums).parallel().sum();
    System.out.printf("Sum: loop=%s, reduce=%s.%n", sum1, sum2);
  }
  
  private static void timingTest(Stream<Employee> testStream) {
    long startTime = System.nanoTime();
    testStream.forEach(e -> doSlowOp());
    long endTime = System.nanoTime();
    System.out.printf(" %.3f seconds.%n", 
                      deltaSeconds(startTime, endTime));
  }
  
  private static double deltaSeconds(long startTime, long endTime) {
    return((endTime - startTime) / 1000000000);
  }
  
  // Simulate a time-consuming operation
  private static void doSlowOp() {
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException ie) {
      // Nothing to do here. 
    }
  }

  
  // To support the tiny StringJoiner example inside the collect examples.
  
  public static void stringJoinerExamples() {
    StringJoiner joiner1 = new StringJoiner(", ");
    String result1 =
      joiner1.add("Java").add("Lisp").add("Ruby").toString();
    System.out.println(result1);
    String result2 =
      String.join(", ", "Java", "Lisp", "Ruby");
    System.out.println(result2);
  }
  
  public static void testBreak() {
    List<Employee> googlers = EmployeeSamples.getGooglers();
    // Continues to the end of the Googlers, regardless of the value of Math.random
    googlers.stream().forEach(e -> {
      System.out.println(e);
      if (Math.random() > 0.5) {
        System.out.println("Continuing");
      } else {
        System.out.println("Stopping");
        return;
      }
    });
  }
}
