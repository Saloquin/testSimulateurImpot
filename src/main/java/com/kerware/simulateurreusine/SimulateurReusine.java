package com.kerware.simulateurreusine;

import com.kerware.simulateur.SituationFamiliale;

/**
 * Simulateur d'impôt sur le revenu 2025 (revenus 2024)
 * Cette classe implémente le calcul de l'impôt sur le revenu en France
 * selon les règles fiscales en vigueur pour l'année 2025.
 */
public final class SimulateurReusine {
    
    // Constantes pour les tranches d'imposition
    private static final class TranchesImposition {
        static final double[] TAUX = {0.0, 0.11, 0.30, 0.41, 0.45};
        static final int[] LIMITES = {0, 11294, 28797, 82341, 177106, Integer.MAX_VALUE};
        static final int NB_TRANCHES = 5;
    }
    
    // Constantes pour la contribution exceptionnelle hauts revenus
    private static final class ContributionHautsRevenus {
        static final int[] LIMITES = {0, 250000, 500000, 1000000, Integer.MAX_VALUE};
        static final double[] TAUX_CELIBATAIRE = {0.0, 0.03, 0.04, 0.04};
        static final double[] TAUX_COUPLE = {0.0, 0.0, 0.03, 0.04};
        static final int NB_TRANCHES = 4;
    }
    
    // Constantes pour l'abattement
    private static final class Abattement {
        static final int MINIMUM = 495;
        static final int MAXIMUM = 14171;
        static final double TAUX = 0.1;
    }
    
    // Constantes pour la décote
    private static final class Decote {
        static final double SEUIL_DECLARANT_SEUL = 1929;
        static final double SEUIL_DECLARANT_COUPLE = 3191;
        static final double MAXIMUM_DECLARANT_SEUL = 873;
        static final double MAXIMUM_DECLARANT_COUPLE = 1444;
        static final double TAUX = 0.4525;
    }

    // Constantes pour les parts fiscales
    private static final class PartsFiscales {
        static final double DEMI_PART = 0.5;
        static final double PART_ENTIERE = 1.0;
        static final double DEUX_PARTS = 2.0;
        static final int NB_ENFANTS_MAX = 7;
        static final int SEUIL_ENFANTS_DEMI_PARTS = 2;
    }
    
    // Constante pour le plafonnement des effets du quotient familial
    private static final double PLAFOND_AVANTAGE_DEMI_PART = 1759;

    // Constantes pour les calculs et limites
    private static final class ConstantesCalcul {
        static final int NB_TRANCHES_IMPOT = 5;
        static final int NB_TRANCHES_CONTRIB = 4;
        static final int NOMBRE_ENFANTS_MAX = 7;
        static final double DEMI_PART = 0.5;
    }

    // État du foyer fiscal
    private FoyerFiscal foyerFiscal;
    private ResultatCalcul resultat;

    public SimulateurReusine() {
        this.foyerFiscal = new FoyerFiscal();
        this.resultat = new ResultatCalcul();
    }

    /**
     * Calcule l'impôt sur le revenu pour un foyer fiscal
     * @param revenuNetDeclarant1 Revenu net du premier déclarant
     * @param revenuNetDeclarant2 Revenu net du second déclarant
     * @param situationFamiliale Situation familiale du foyer
     * @param nombreEnfantsACharge Nombre d'enfants à charge
     * @param nombreEnfantsHandicapes Nombre d'enfants en situation de handicap
     * @param parentIsole Indique si le parent est isolé
     * @throws IllegalArgumentException si les paramètres sont invalides
     */
    public void calculerImpot(int revenuNetDeclarant1, int revenuNetDeclarant2,
                            SituationFamiliale situationFamiliale, int nombreEnfantsACharge,
                            int nombreEnfantsHandicapes, boolean parentIsole) {
        validerParametres(revenuNetDeclarant1, revenuNetDeclarant2, situationFamiliale,
                         nombreEnfantsACharge, nombreEnfantsHandicapes, parentIsole);
                         
        initialiserFoyerFiscal(revenuNetDeclarant1, revenuNetDeclarant2, situationFamiliale,
                              nombreEnfantsACharge, nombreEnfantsHandicapes, parentIsole);
                              
        calculerPartsImposition();
        calculerAbattements();
        calculerRevenuImposable();
        calculerImpotBrut();
        appliquerPlafonnementQuotientFamilial();
        calculerDecote();
        calculerContributionExceptionnelle();
        calculerImpotFinal();
    }

    private void validerParametres(int revenuNetDeclarant1, int revenuNetDeclarant2,
                                 SituationFamiliale situationFamiliale, int nombreEnfantsACharge,
                                 int nombreEnfantsHandicapes, boolean parentIsole) {
        if (revenuNetDeclarant1 < 0 || revenuNetDeclarant2 < 0) {
            throw new IllegalArgumentException("Le revenu net ne peut pas être négatif");
        }
        if (situationFamiliale == null) {
            throw new IllegalArgumentException("La situation familiale ne peut pas être null");
        }
        if (nombreEnfantsACharge < 0) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être négatif");
        }
        if (nombreEnfantsHandicapes < 0) {
            throw new IllegalArgumentException(
                "Le nombre d'enfants handicapés ne peut pas être négatif"
            );
        }
        if (nombreEnfantsHandicapes > nombreEnfantsACharge) {
            throw new IllegalArgumentException(
                "Le nombre d'enfants handicapés ne peut pas être supérieur au nombre d'enfants"
            );
        }
        if (nombreEnfantsACharge > ConstantesCalcul.NOMBRE_ENFANTS_MAX) {
            throw new IllegalArgumentException(
                "Le nombre d'enfants ne peut pas être supérieur à " + ConstantesCalcul.NOMBRE_ENFANTS_MAX
            );
        }
        if (parentIsole && (situationFamiliale == SituationFamiliale.MARIE 
            || situationFamiliale == SituationFamiliale.PACSE)) {
            throw new IllegalArgumentException("Un parent isolé ne peut pas être marié ou pacsé");
        }
        boolean estSeul = situationFamiliale == SituationFamiliale.CELIBATAIRE 
            || situationFamiliale == SituationFamiliale.DIVORCE
            || situationFamiliale == SituationFamiliale.VEUF;
        if (estSeul && revenuNetDeclarant2 > 0) {
            throw new IllegalArgumentException(
                "Un célibataire, divorcé ou veuf ne peut pas avoir de revenu pour le déclarant 2"
            );
        }
    }

    private void initialiserFoyerFiscal(int revenuNetDeclarant1, int revenuNetDeclarant2,
                                      SituationFamiliale situationFamiliale, int nombreEnfantsACharge,
                                      int nombreEnfantsHandicapes, boolean parentIsole) {
        foyerFiscal.revenuNetDeclarant1 = revenuNetDeclarant1;
        foyerFiscal.revenuNetDeclarant2 = revenuNetDeclarant2;
        foyerFiscal.situationFamiliale = situationFamiliale;
        foyerFiscal.nombreEnfantsACharge = nombreEnfantsACharge;
        foyerFiscal.nombreEnfantsHandicapes = nombreEnfantsHandicapes;
        foyerFiscal.parentIsole = parentIsole;
    }

    private void calculerPartsImposition() {
        // Calcul du nombre de parts pour les déclarants
        switch (foyerFiscal.situationFamiliale) {
            case CELIBATAIRE, DIVORCE, VEUF -> foyerFiscal.nombrePartsDeclarants = PartsFiscales.PART_ENTIERE;
            case MARIE, PACSE -> foyerFiscal.nombrePartsDeclarants = PartsFiscales.DEUX_PARTS;
            default -> throw new IllegalStateException("Situation familiale non gérée");
        }

        calculerPartsEnfants();

        // Parts supplémentaires
        double partsSupplementaires = 0.0;
        if (foyerFiscal.parentIsole && foyerFiscal.nombreEnfantsACharge > 0) {
            partsSupplementaires += PartsFiscales.DEMI_PART;
        }
        if (foyerFiscal.situationFamiliale == SituationFamiliale.VEUF 
            && foyerFiscal.nombreEnfantsACharge > 0) {
            partsSupplementaires += PartsFiscales.PART_ENTIERE;
        }
        partsSupplementaires += foyerFiscal.nombreEnfantsHandicapes * PartsFiscales.DEMI_PART;

        foyerFiscal.nombreParts += partsSupplementaires;
    }

    private void calculerPartsEnfants() {
        // Calcul du nombre de parts pour les enfants
        if (foyerFiscal.nombreEnfantsACharge <= PartsFiscales.SEUIL_ENFANTS_DEMI_PARTS) {
            double partsEnfants = foyerFiscal.nombreEnfantsACharge * ConstantesCalcul.DEMI_PART;
            foyerFiscal.nombreParts = foyerFiscal.nombrePartsDeclarants + partsEnfants;
        } else {
            foyerFiscal.nombreParts = foyerFiscal.nombrePartsDeclarants + PartsFiscales.PART_ENTIERE
                + (foyerFiscal.nombreEnfantsACharge - PartsFiscales.SEUIL_ENFANTS_DEMI_PARTS);
        }
    }

    private void calculerAbattements() {
        double abattementDecl1 = Math.round(foyerFiscal.revenuNetDeclarant1 * Abattement.TAUX);
        double abattementDecl2 = Math.round(foyerFiscal.revenuNetDeclarant2 * Abattement.TAUX);
        
        if (abattementDecl1 > Abattement.MAXIMUM) {
            abattementDecl1 = Abattement.MAXIMUM;
        }
        if (abattementDecl1 < Abattement.MINIMUM) {
            abattementDecl1 = Abattement.MINIMUM;
        }
        
        if (foyerFiscal.situationFamiliale == SituationFamiliale.MARIE 
            || foyerFiscal.situationFamiliale == SituationFamiliale.PACSE) {
            if (abattementDecl2 > Abattement.MAXIMUM) {
                abattementDecl2 = Abattement.MAXIMUM;
            }
            if (abattementDecl2 < Abattement.MINIMUM) {
                abattementDecl2 = Abattement.MINIMUM;
            }
        } else {
            abattementDecl2 = 0;
        }
        
        resultat.abattement = abattementDecl1 + abattementDecl2;
    }

    private void calculerRevenuImposable() {
        resultat.revenuFiscalReference = Math.max(
            foyerFiscal.revenuNetDeclarant1 + foyerFiscal.revenuNetDeclarant2 - resultat.abattement,
            0
        );
    }

    private double calculerImpotParTranches(double revenuImposable) {
        double impot = 0;
        int i = 0;
        do {
            if (revenuImposable >= TranchesImposition.LIMITES[i] 
                && revenuImposable < TranchesImposition.LIMITES[i + 1]) {
                double montantImposable = revenuImposable - TranchesImposition.LIMITES[i];
                impot += montantImposable * TranchesImposition.TAUX[i];
                break;
            } else {
                double montantImposable = TranchesImposition.LIMITES[i + 1] - TranchesImposition.LIMITES[i];
                impot += montantImposable * TranchesImposition.TAUX[i];
            }
            i++;
        } while (i < ConstantesCalcul.NB_TRANCHES_IMPOT);
        return impot;
    }

    private void calculerImpotBrut() {
        // Calcul de l'impôt des déclarants (pour le plafonnement du quotient familial)
        double revenuImposableDeclarants = resultat.revenuFiscalReference / foyerFiscal.nombrePartsDeclarants;
        double impotDeclarantsParPart = calculerImpotParTranches(revenuImposableDeclarants);
        resultat.impotBrut = impotDeclarantsParPart * foyerFiscal.nombrePartsDeclarants;
        resultat.impotBrut = Math.round(resultat.impotBrut);

        // Calcul de l'impôt du foyer fiscal
        double revenuImposableFoyer = resultat.revenuFiscalReference / foyerFiscal.nombreParts;
        double impotFoyerParPart = calculerImpotParTranches(revenuImposableFoyer);
        resultat.impotAvantDecote = impotFoyerParPart * foyerFiscal.nombreParts;
        resultat.impotAvantDecote = Math.round(resultat.impotAvantDecote);
    }

    private void appliquerPlafonnementQuotientFamilial() {
        double baisseImpot = resultat.impotBrut - resultat.impotAvantDecote;
        double ecartParts = foyerFiscal.nombreParts - foyerFiscal.nombrePartsDeclarants;
        double plafond = Math.round((ecartParts / ConstantesCalcul.DEMI_PART) * PLAFOND_AVANTAGE_DEMI_PART);

        if (baisseImpot >= plafond) {
            resultat.impotAvantDecote = resultat.impotBrut - plafond;
        }
    }

    private void calculerDecote() {
        resultat.decote = 0;
        if (foyerFiscal.nombrePartsDeclarants == 1) {
            calculerDecoteSeul();
        } else if (foyerFiscal.nombrePartsDeclarants == 2) {
            calculerDecoteCouple();
        }
        resultat.decote = Math.min(resultat.decote, resultat.impotAvantDecote);
    }

    private void calculerDecoteSeul() {
        if (resultat.impotAvantDecote < Decote.SEUIL_DECLARANT_SEUL) {
            double decoteTemp = Decote.MAXIMUM_DECLARANT_SEUL 
                - (resultat.impotAvantDecote * Decote.TAUX);
            resultat.decote = Math.round(decoteTemp);
        }
    }

    private void calculerDecoteCouple() {
        if (resultat.impotAvantDecote < Decote.SEUIL_DECLARANT_COUPLE) {
            double decoteTemp = Decote.MAXIMUM_DECLARANT_COUPLE 
                - (resultat.impotAvantDecote * Decote.TAUX);
            resultat.decote = Math.round(decoteTemp);
        }
    }

    private void calculerContributionExceptionnelle() {
        resultat.contributionExceptionnelle = 0;
        for (int i = 0; i < ConstantesCalcul.NB_TRANCHES_CONTRIB; i++) {
            calculerContributionTranche(i);
        }
        resultat.contributionExceptionnelle = Math.round(resultat.contributionExceptionnelle);
    }

    private void calculerContributionTranche(int indexTranche) {
        double limiteInferieure = ContributionHautsRevenus.LIMITES[indexTranche];
        double limiteSuperieure = ContributionHautsRevenus.LIMITES[indexTranche + 1];
        double[] tauxApplicables = foyerFiscal.nombrePartsDeclarants == 1 
            ? ContributionHautsRevenus.TAUX_CELIBATAIRE 
            : ContributionHautsRevenus.TAUX_COUPLE;

        if (resultat.revenuFiscalReference >= limiteInferieure 
            && resultat.revenuFiscalReference < limiteSuperieure) {
            double difference = resultat.revenuFiscalReference - limiteInferieure;
            resultat.contributionExceptionnelle += difference * tauxApplicables[indexTranche];
            return;
        }

        if (resultat.revenuFiscalReference >= limiteSuperieure) {
            double difference = limiteSuperieure - limiteInferieure;
            resultat.contributionExceptionnelle += difference * tauxApplicables[indexTranche];
        }
    }

    private void calculerImpotFinal() {
        resultat.impotNet = resultat.impotAvantDecote - resultat.decote + resultat.contributionExceptionnelle;
        resultat.impotNet = Math.round(resultat.impotNet);
    }

    /**
     * Obtient le revenu fiscal de référence.
     * @return Le revenu fiscal de référence
     */
    public double getRevenuFiscalReference() {
        return resultat.revenuFiscalReference;
    }

    /**
     * Obtient l'abattement total.
     * @return L'abattement total
     */
    public double getAbattement() {
        return resultat.abattement;
    }

    /**
     * Obtient le nombre de parts du foyer fiscal.
     * @return Le nombre de parts du foyer fiscal
     */
    public double getNombrePartsFoyerFiscal() {
        return foyerFiscal.nombreParts;
    }

    /**
     * Obtient l'impôt avant décote.
     * @return L'impôt avant décote
     */
    public double getImpotAvantDecote() {
        return resultat.impotAvantDecote;
    }

    /**
     * Obtient le montant de la décote.
     * @return Le montant de la décote
     */
    public double getDecote() {
        return resultat.decote;
    }

    /**
     * Obtient le montant de la contribution exceptionnelle.
     * @return Le montant de la contribution exceptionnelle
     */
    public double getContributionExceptionnelle() {
        return resultat.contributionExceptionnelle;
    }

    /**
     * Obtient l'impôt net final.
     * @return L'impôt net final
     */
    public double getImpotNet() {
        return resultat.impotNet;
    }

    /**
     * Classe interne représentant l'état d'un foyer fiscal
     */
    private static class FoyerFiscal {
        private int revenuNetDeclarant1;
        private int revenuNetDeclarant2;
        private SituationFamiliale situationFamiliale;
        private int nombreEnfantsACharge;
        private int nombreEnfantsHandicapes;
        private boolean parentIsole;
        private double nombreParts;
        private double nombrePartsDeclarants;
    }

    /**
     * Classe interne représentant les résultats du calcul d'impôt
     */
    private static class ResultatCalcul {
        private double revenuFiscalReference;
        private double abattement;
        private double impotBrut;
        private double impotAvantDecote;
        private double decote;
        private double contributionExceptionnelle;
        private double impotNet;
    }
}