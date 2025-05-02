package com.kerware.simulateurreusine;

import com.kerware.simulateur.ICalculateurImpot;
import com.kerware.simulateur.SituationFamiliale;

/**
 * Adaptateur pour le nouveau simulateur d'impôt.
 * Permet de maintenir la compatibilité avec les tests existants
 * tout en utilisant la nouvelle implémentation réusinée.
 * Cette classe n'est pas conçue pour être étendue.
 */
public final class AdaptateurSimulateurReusine implements ICalculateurImpot {
    
    private final SimulateurReusine simulateur;
    private int revenusNetDecl1;
    private int revenusNetDecl2;
    private SituationFamiliale situationFamiliale;
    private int nbEnfantsACharge;
    private int nbEnfantsSituationHandicap;
    private boolean parentIsole;

    /**
     * Constructeur de l'adaptateur.
     */
    public AdaptateurSimulateurReusine() {
        this.simulateur = new SimulateurReusine();
    }

    /**
     * Définit le revenu net du premier déclarant.
     * @param rn le revenu net du premier déclarant
     */
    @Override
    public void setRevenusNetDeclarant1(int rn) {
        this.revenusNetDecl1 = rn;
    }

    /**
     * Définit le revenu net du second déclarant.
     * @param rn le revenu net du second déclarant
     */
    @Override
    public void setRevenusNetDeclarant2(int rn) {
        this.revenusNetDecl2 = rn;
    }

    /**
     * Définit la situation familiale du foyer fiscal.
     * @param sf la situation familiale (CELIBATAIRE, MARIE, PACSE, DIVORCE, VEUF)
     */
    @Override
    public void setSituationFamiliale(SituationFamiliale sf) {
        this.situationFamiliale = sf;
    }

    /**
     * Définit le nombre d'enfants à charge du foyer fiscal.
     * @param nbe le nombre d'enfants à charge
     */
    @Override
    public void setNbEnfantsACharge(int nbe) {
        this.nbEnfantsACharge = nbe;
    }

    /**
     * Définit le nombre d'enfants en situation de handicap.
     * @param nbesh le nombre d'enfants en situation de handicap
     */
    @Override
    public void setNbEnfantsSituationHandicap(int nbesh) {
        this.nbEnfantsSituationHandicap = nbesh;
    }

    /**
     * Définit si le contribuable est un parent isolé.
     * @param pi true si le contribuable est un parent isolé, false sinon
     */
    @Override
    public void setParentIsole(boolean pi) {
        this.parentIsole = pi;
    }

    /**
     * Calcule l'impôt sur le revenu net en utilisant les paramètres précédemment définis.
     * Cette méthode doit être appelée après avoir défini tous les paramètres nécessaires.
     */
    @Override
    public void calculImpotSurRevenuNet() {
        simulateur.calculerImpot(
            revenusNetDecl1,
            revenusNetDecl2,
            situationFamiliale,
            nbEnfantsACharge,
            nbEnfantsSituationHandicap,
            parentIsole
        );
    }

    /**
     * Obtient le revenu net du premier déclarant.
     * @return le revenu net du premier déclarant
     */
    @Override
    public int getRevenuNetDeclatant1() {
        return revenusNetDecl1;
    }

    /**
     * Obtient le revenu net du second déclarant.
     * @return le revenu net du second déclarant
     */
    @Override
    public int getRevenuNetDeclatant2() {
        return revenusNetDecl2;
    }

    /**
     * Obtient le montant de la contribution exceptionnelle sur les hauts revenus.
     * @return le montant de la contribution exceptionnelle
     */
    @Override
    public double getContribExceptionnelle() {
        return simulateur.getContributionExceptionnelle();
    }

    /**
     * Obtient le revenu fiscal de référence du foyer.
     * @return le revenu fiscal de référence
     */
    @Override
    public int getRevenuFiscalReference() {
        return (int) simulateur.getRevenuFiscalReference();
    }

    /**
     * Obtient le montant total de l'abattement appliqué.
     * @return le montant de l'abattement
     */
    @Override
    public int getAbattement() {
        return (int) simulateur.getAbattement();
    }

    /**
     * Obtient le nombre de parts du foyer fiscal.
     * @return le nombre de parts du foyer fiscal
     */
    @Override
    public double getNbPartsFoyerFiscal() {
        return simulateur.getNombrePartsFoyerFiscal();
    }

    /**
     * Obtient le montant de l'impôt avant application de la décote.
     * @return le montant de l'impôt avant décote
     */
    @Override
    public int getImpotAvantDecote() {
        return (int) simulateur.getImpotAvantDecote();
    }

    /**
     * Obtient le montant de la décote appliquée.
     * @return le montant de la décote
     */
    @Override
    public int getDecote() {
        return (int) simulateur.getDecote();
    }

    /**
     * Obtient le montant final de l'impôt sur le revenu net.
     * @return le montant final de l'impôt
     */
    @Override
    public int getImpotSurRevenuNet() {
        return (int) simulateur.getImpotNet();
    }
}