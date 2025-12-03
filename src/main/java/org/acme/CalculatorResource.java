package org.acme;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


@Path("/")
public class CalculatorResource {
    @Inject
    Template index;

    private static final BigDecimal MAX = new BigDecimal("1000000000000.000000");
    private static final BigDecimal MIN = MAX.negate();


    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index.data("a", 0, "b", 0, "op", "add", "result", "0", "error", null);
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance calc(@FormParam("a") String aStr,
                                 @FormParam("b") String bStr,
                                 @FormParam("op") String op) {

        String error = null;
        String result = null;

        try {
            BigDecimal A = parse(aStr);
            BigDecimal B = parse(bStr);


            if (A.compareTo(MAX) > 0 || A.compareTo(MIN) < 0 || B.compareTo(MAX) > 0 || B.compareTo(MIN) < 0) {
                error = "Число выходит за допустимый диапазон";
                return index.data("a", aStr, "b", bStr, "op", op, "result", result, "error", error);
            }

            BigDecimal calcResult;
            switch (op) {
                case "add" -> calcResult = A.add(B);
                case "sub" -> calcResult = A.subtract(B);
                case "mul" -> calcResult = A.multiply(B);
                case "div" -> {
                    if (B.compareTo(BigDecimal.ZERO) == 0) {
                        error = "Деление на ноль неприемлимо";
                        return index.data("a", aStr, "b", bStr, "op", op, "result", result, "error", error);
                    }
                    calcResult = A.divide(B, 6, RoundingMode.HALF_UP);
                }
                default -> throw new IllegalArgumentException();
            }

            if (calcResult.compareTo(MAX) > 0 || calcResult.compareTo(MIN) < 0) {
                error = "Переполнение";
            } else {
                calcResult = calcResult.setScale(6, RoundingMode.HALF_UP);

                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setGroupingSeparator(' ');
                symbols.setDecimalSeparator('.');

                DecimalFormat df = new DecimalFormat("#,##0.######", symbols);
                df.setGroupingUsed(true);
                result = df.format(calcResult);
            }

        } catch (Exception e) {
            error = "Неверный ввод";
        }

        return index.data("a", aStr, "b", bStr, "op", op, "result", result, "error", error);
    }

    private BigDecimal parse(String s) {
        if (s == null) throw new NumberFormatException();
        String cleaned = s.trim().replace(',', '.');
        // Проверка на экспоненциальную запись
        if (cleaned.matches("(?i).*e[+-]?\\d+.*")) throw new NumberFormatException();
        // либо все цифры без пробелов, либо корректные пробелы каждые 3 цифры
        if (!cleaned.matches("^[+-]?(\\d{1,3}( \\d{3})*|\\d+)(\\.\\d{0,6})?$")) throw new NumberFormatException();
        String normalized = cleaned.replace(" ", "");
        BigDecimal bigDec = new BigDecimal(normalized);
        if (bigDec.scale() < 0) bigDec = bigDec.setScale(0, RoundingMode.HALF_UP);
        if (bigDec.scale() > 6) throw new NumberFormatException();
        return bigDec;
    }
}
