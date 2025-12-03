package org.acme.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculationService {

    public static final BigDecimal MAX = new BigDecimal("1000000000000.000000");
    public static final BigDecimal MIN = MAX.negate();

    public static BigDecimal calculate(BigDecimal x, BigDecimal y, String op) throws Exception {

        BigDecimal calcResult;

        switch (op) {
            case "add" -> calcResult = x.add(y);
            case "sub" -> calcResult = x.subtract(y);
            case "mul" -> calcResult = x.multiply(y);
            case "div" -> {
                if (y.compareTo(BigDecimal.ZERO) == 0) {
                    throw new Exception("Деление на ноль неприемлимо");
                }
                calcResult = x.divide(y, 10, RoundingMode.HALF_UP);
            }
            default -> throw new IllegalArgumentException();
        }

        if (calcResult.compareTo(MAX) > 0 || calcResult.compareTo(MIN) < 0) {
            throw new Exception("Переполнение");
        }

        calcResult = calcResult.setScale(10, RoundingMode.HALF_UP);
        return calcResult;
    }
}
