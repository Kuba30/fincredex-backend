package com.example.fincredex.service.impl;

import com.example.fincredex.model.entities.Application;
import com.example.fincredex.model.entities.Scoring;
import com.example.fincredex.model.enums.Decision;
import com.example.fincredex.model.enums.Rating;
import com.example.fincredex.repository.ScoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ScoringCalculation {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING =
            RoundingMode.HALF_UP;

    private final ScoringRepository scoringRepository;


    // =========================================================
    // CREATE OR UPDATE SCORING
    // =========================================================

    @Transactional
    public Scoring calculateAndSave(Application application) {

        /*
         * If scoring already exists for this application,
         * update it.
         *
         * Otherwise create a new Scoring.
         */
        Scoring scoring =
                scoringRepository
                        .findByApplication_Id(application.getId())
                        .orElseGet(Scoring::new);

        // Calculate all values once
        BigDecimal newPayment =
                calculateNewPayment(application);

        BigDecimal debtLoad =
                calculateDebtLoad(application);

        BigDecimal dscr =
                calculateDscr(application);

        Rating rating =
                calculateRating(dscr, debtLoad);

        Decision decision =
                calculateDecision(rating);


        // Update scoring
        scoring.setApplication(application);

        scoring.setNewPayment(newPayment);

        scoring.setDebtLoad(debtLoad);

        scoring.setDscr(dscr);

        scoring.setRating(rating);

        scoring.setDecision(decision);

        scoring.setCreatedAt(
                LocalDateTime.now()
        );


        // Keep both sides of relationship synchronized
        application.setScoring(scoring);


        return scoringRepository.save(scoring);
    }


    // =========================================================
    // RECALCULATE
    // =========================================================

    @Transactional
    public Scoring recalculate(Application application) {

        /*
         * We DON'T delete the old scoring.
         *
         * calculateAndSave() finds the existing scoring
         * and updates it.
         */

        return calculateAndSave(application);
    }


    // =========================================================
    // DEBT LOAD
    // =========================================================

    /*
     * debt_load =
     * currentPayments / monthlyRevenue
     */
    public BigDecimal calculateDebtLoad(
            Application application
    ) {

        BigDecimal currentPayments =
                application
                        .getReport()
                        .getCurrentPayments();

        BigDecimal monthlyRevenue =
                application
                        .getReport()
                        .getMonthlyRevenue();


        if (
                monthlyRevenue == null ||
                        monthlyRevenue.compareTo(
                                BigDecimal.ZERO
                        ) == 0
        ) {
            return BigDecimal.ZERO;
        }


        return currentPayments.divide(
                monthlyRevenue,
                SCALE,
                ROUNDING
        );
    }


    // =========================================================
    // NEW MONTHLY PAYMENT
    // =========================================================

    /*
     * new_payment =
     *
     * loanAmount * monthlyRate
     * -------------------------
     * 1 - (1 + monthlyRate)^-term
     */
    public BigDecimal calculateNewPayment(
            Application application
    ) {

        BigDecimal loanAmount =
                application.getLoanAmount();

        BigDecimal annualRate =
                application.getInterestRate();

        int termMonths =
                application.getTermMonths();


        // Annual % → monthly decimal rate
        BigDecimal monthlyRate =
                annualRate
                        .divide(
                                BigDecimal.valueOf(100),
                                10,
                                ROUNDING
                        )
                        .divide(
                                BigDecimal.valueOf(12),
                                10,
                                ROUNDING
                        );


        /*
         * 0% interest:
         *
         * payment = loan / months
         */
        if (
                monthlyRate.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {

            return loanAmount.divide(
                    BigDecimal.valueOf(termMonths),
                    SCALE,
                    ROUNDING
            );
        }


        double rate =
                monthlyRate.doubleValue();

        double factor =
                Math.pow(
                        1 + rate,
                        termMonths
                );


        BigDecimal factorBD =
                BigDecimal.valueOf(factor);


        BigDecimal numerator =
                loanAmount
                        .multiply(monthlyRate)
                        .multiply(factorBD);


        BigDecimal denominator =
                factorBD.subtract(
                        BigDecimal.ONE
                );


        return numerator.divide(
                denominator,
                SCALE,
                ROUNDING
        );
    }


    // =========================================================
    // DSCR
    // =========================================================

    /*
     * DSCR =
     *
     * monthlyRevenue
     * ---------------------------
     * currentPayments + newPayment
     */
    public BigDecimal calculateDscr(
            Application application
    ) {

        BigDecimal monthlyRevenue =
                application
                        .getReport()
                        .getMonthlyRevenue();

        BigDecimal currentPayments =
                application
                        .getReport()
                        .getCurrentPayments();

        BigDecimal newPayment =
                calculateNewPayment(application);


        BigDecimal totalPayments =
                currentPayments.add(
                        newPayment
                );


        if (
                totalPayments.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {
            return BigDecimal.ZERO;
        }


        return monthlyRevenue.divide(
                totalPayments,
                SCALE,
                ROUNDING
        );
    }


    // =========================================================
    // RATING
    // =========================================================

    public Rating calculateRating(
            BigDecimal dscr,
            BigDecimal debtLoad
    ) {

        if (
                dscr.compareTo(
                        new BigDecimal("1.5")
                ) >= 0
                        &&
                        debtLoad.compareTo(
                                new BigDecimal("0.3")
                        ) <= 0
        ) {

            return Rating.A;

        } else if (
                dscr.compareTo(
                        new BigDecimal("1.25")
                ) >= 0
                        &&
                        debtLoad.compareTo(
                                new BigDecimal("0.4")
                        ) <= 0
        ) {

            return Rating.B;

        } else if (
                dscr.compareTo(
                        new BigDecimal("1.0")
                ) >= 0
                        &&
                        debtLoad.compareTo(
                                new BigDecimal("0.5")
                        ) <= 0
        ) {

            return Rating.C;

        } else if (
                dscr.compareTo(
                        new BigDecimal("0.75")
                ) >= 0
                        &&
                        debtLoad.compareTo(
                                new BigDecimal("0.6")
                        ) <= 0
        ) {

            return Rating.D;

        } else {

            return Rating.E;
        }
    }



    public Decision calculateDecision(
            Rating rating
    ) {

        return switch (rating) {

            case A, B ->
                    Decision.APPROVE;

            case C ->
                    Decision.REVIEW;

            case D, E ->
                    Decision.REJECT;
        };
    }
}