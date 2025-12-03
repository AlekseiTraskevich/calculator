package org.acme;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Path("/")
public class CalculatorResource {
    @Inject
    Template index;

    private static final BigDecimal MAX = new BigDecimal("1000000000000.000000");
    private static final BigDecimal MIN = MAX.negate();


    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index.data("a", 0, "b", 0, "op", "add", "result", null, "error", null);
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

            BigDecimal calcResult = switch (op) {
                case "add" -> A.add(B);
                case "sub" -> A.subtract(B);
                default -> throw new IllegalArgumentException();
            };

            if (calcResult.compareTo(MAX) > 0 || calcResult.compareTo(MIN) < 0) {
                error = "Переполнение";
            } else {
                calcResult = calcResult.setScale(6, RoundingMode.HALF_UP);
                result = calcResult.stripTrailingZeros().toPlainString();
            }

        } catch (Exception e) {
            error = "Неверный ввод";
        }

        return index.data("a", aStr, "b", bStr, "op", op, "result", result, "error", error);
    }

    private BigDecimal parse(String s) {
        if (s == null) throw new NumberFormatException();
        String cleaned = s.trim().replace(',', '.');
        if (cleaned.matches("(?i).*e[+-]?\\d+.*")) throw new NumberFormatException();
        if (!cleaned.matches("[+-]?\\d+(\\.\\d{0,6})?")) throw new NumberFormatException();
        BigDecimal v = new BigDecimal(cleaned);
        if (v.scale() < 0) v = v.setScale(0, RoundingMode.HALF_UP);
        if (v.scale() > 6) throw new NumberFormatException();
        return v;
    }
}
