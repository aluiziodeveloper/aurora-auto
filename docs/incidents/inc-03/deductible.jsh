// INC-03 after the fix: the same numbers as lesson 03-01, now through the domain types.
import br.com.aurora.shared.domain.Money;
import br.com.aurora.policy.domain.Deductible;

var repair = Money.of("1800.00");
var deductible = new Deductible(Money.of("2200.00"));
System.out.println("indemnity = " + deductible.applyTo(repair));
try {
    System.out.println(repair.minus(Money.of("2200.00")));
} catch (IllegalArgumentException e) {
    System.out.println("naive minus: " + e.getMessage());
}
/exit
