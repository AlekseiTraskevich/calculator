package org.acme;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.DTO.CalcState;
import org.acme.Service.CalculationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import static org.acme.Service.CalculationService.MAX;
import static org.acme.Service.CalculationService.MIN;


@Path("/")
public class CalculatorResource {
    @Inject
    Template index;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index.data(new CalcState().toMap());
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance calc(@FormParam("a") String aStr,
                                 @FormParam("b") String bStr,
                                 @FormParam("c") String cStr,
                                 @FormParam("d") String dStr,
                                 @FormParam("opAB") String opAB,
                                 @FormParam("opBC") String opBC,
                                 @FormParam("opCD") String opCD,
                                 @FormParam("roundMode") String roundMode) {

        String error = null;
        String result = null;
        String roundResult = null;
        BigDecimal bigResult;
        BigDecimal bigRoundResult;


        try {
            BigDecimal a = parse(aStr);
            BigDecimal b = parse(bStr);
            BigDecimal c = parse(cStr);
            BigDecimal d = parse(dStr);

            BigDecimal bigResultOpAB;
            BigDecimal bigResultOpBC;
            BigDecimal bigResultOpCD;

            bigResultOpBC = CalculationService.calculate(b, c, opBC);
            if ((opAB.equals("add") || opAB.equals("sub")) && (opCD.equals("mul") || opCD.equals("div"))) {
                bigResultOpCD = CalculationService.calculate(bigResultOpBC, d, opCD);
                bigResultOpAB = CalculationService.calculate(a, bigResultOpCD, opAB);
                bigResult = bigResultOpAB;
            } else {
                bigResultOpAB = CalculationService.calculate(a, bigResultOpBC, opAB);
                bigResultOpCD = CalculationService.calculate(bigResultOpAB, d, opCD);
                bigResult = bigResultOpCD;
            }
        } catch (Exception e) {
            error = e.getMessage();
            return index.data(new CalcState(aStr, bStr, cStr, dStr, opAB, opBC, opCD, result, error, roundResult, roundMode));

        }

//        bigRoundResult = bigResult;
        bigRoundResult = switch (roundMode) {
            case "math" -> bigResult.setScale(0, RoundingMode.HALF_UP);
            case "bank" -> bigResult.setScale(0, RoundingMode.HALF_EVEN);
            case "down" -> bigResult.setScale(0, RoundingMode.DOWN);
            default -> bigResult.setScale(0, RoundingMode.HALF_UP); //math
        };
        roundResult = formatedStringFromBigDec(bigRoundResult);
        result = formatedStringFromBigDec(bigResult);

        return index.data(new CalcState(aStr, bStr, cStr, dStr, opAB, opBC, opCD, result, error, roundResult, roundMode));
    }


    private BigDecimal parse(String s) throws NumberFormatException {
        if (s == null || s.isEmpty())
            throw new NumberFormatException("заполните все поля");
        s = s.trim().replace(',', '.');
        // Проверка на экспоненциальную запись
        if (s.matches("(?i).*e[+-]?\\d+.*"))
            throw new NumberFormatException("эксп. запись неприемлима");
        // либо все цифры без пробелов, либо корректные пробелы каждые 3 цифры
        if (!s.matches("^[+-]?(\\d{1,3}( \\d{3})*|\\d+)(\\.\\d{0,6})?$"))
            throw new NumberFormatException("неверный формат");
        String normalized = s.replace(" ", "");
        BigDecimal bigDec = new BigDecimal(normalized);

        if (bigDec.compareTo(MAX) > 0 || bigDec.compareTo(MIN) < 0) {
            throw new NumberFormatException("Введенное число выходит за допустимый диапазон");
        }

        if (bigDec.scale() < 0) bigDec = bigDec.setScale(0, RoundingMode.HALF_UP);
        if (bigDec.scale() > 6) throw new NumberFormatException("слишком много цифр после запятой");
        return bigDec;
    }


    private String formatedStringFromBigDec(BigDecimal bigDecimal) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.######", symbols);
        decimalFormat.setGroupingUsed(true);
        return decimalFormat.format(bigDecimal);
    }
}
